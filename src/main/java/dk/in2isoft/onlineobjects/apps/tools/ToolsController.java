package dk.in2isoft.onlineobjects.apps.tools;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNull;
import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.in2igui.FileBasedInterface;
import dk.in2isoft.in2igui.data.ItemData;
import dk.in2isoft.in2igui.data.ListData;
import dk.in2isoft.in2igui.data.ListDataRow;
import dk.in2isoft.in2igui.data.ListObjects;
import dk.in2isoft.in2igui.data.ListWriter;
import dk.in2isoft.onlineobjects.core.Path;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.SearchResult;
import dk.in2isoft.onlineobjects.core.UserSession;
import dk.in2isoft.onlineobjects.core.exceptions.BadRequestException;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.NotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.EmailAddress;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.Image;
import dk.in2isoft.onlineobjects.model.InternetAddress;
import dk.in2isoft.onlineobjects.model.Invitation;
import dk.in2isoft.onlineobjects.model.Person;
import dk.in2isoft.onlineobjects.model.PhoneNumber;
import dk.in2isoft.onlineobjects.model.Property;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.model.Word;
import dk.in2isoft.onlineobjects.modules.images.ImageImporter;
import dk.in2isoft.onlineobjects.modules.importing.DataImporter;
import dk.in2isoft.onlineobjects.modules.language.WordByInternetAddressQuery;
import dk.in2isoft.onlineobjects.modules.networking.InternetAddressInfo;
import dk.in2isoft.onlineobjects.ui.Request;


public class ToolsController extends ToolsControllerBase {

	public List<Locale> getLocales() {
		return null;
	}

	@Override
	public void unknownRequest(Request request) throws IOException, EndUserException {
		String[] localPath = request.getLocalPath();
		if (localPath.length==0) {
			request.getResponse().sendRedirect("images/");
		} else if (request.testLocalPathFull("images")) {
			FileBasedInterface ui = new FileBasedInterface(getFile("web","images.gui.xml"), huiService);
			ui.render(request.getRequest(), request.getResponse());
		} else if (request.testLocalPathFull("persons")) {
			FileBasedInterface ui = new FileBasedInterface(getFile("web","persons.gui.xml"), huiService);
			ui.render(request.getRequest(), request.getResponse());
		} else if (request.testLocalPathFull("bookmarks")) {
			FileBasedInterface ui = new FileBasedInterface(getFile("web","bookmarks.gui.xml"), huiService);
			ui.render(request.getRequest(), request.getResponse());
		} else {
			super.unknownRequest(request);
		}
	}

	@Path(exactly="uploadImage")
	public void importImage(Request request) throws IOException, EndUserException {
		DataImporter dataImporter = importService.createImporter();
		dataImporter.setListener(new ImageImporter(modelService,imageService));
		dataImporter.importMultipart(this, request);
	}

	@Path
	public ListObjects listImages(Request request) throws EndUserException {
		String text = request.getString("text");
		String tag = request.getString("tag");
		ListObjects list = new ListObjects();
		Query<Image> query = new Query<Image>(Image.class).as(request.getSession());
		query.withWords(text);
		if (Strings.isNotBlank(tag)) {
			query.withCustomProperty(Property.KEY_COMMON_TAG, tag);
		}
		List<Image> persons = modelService.list(query, request);

		for (Image image : persons) {
			ListDataRow row = new ListDataRow();
			row.addColumn("id", image.getId());
			row.addColumn("name", image.getName());
			row.addColumn("size", image.getWidth()+"x"+image.getHeight());
			row.addColumn("width", image.getWidth());
			row.addColumn("height", image.getHeight());
			list.addRow(row);
		}
		return list;
	}

	@Path
	public Map<String,Object> getImage(Request request) throws ModelException, BadRequestException {
		Long id = request.getId();
		Map<String,Object> data = new HashMap<String, Object>();
		Image image = modelService.get(Image.class, id, request);
		data.put("image", image);
		data.put("name", image.getName());
		data.put("description", image.getPropertyValue(Image.PROPERTY_DESCRIPTION));
		data.put("tags", image.getPropertyValues(Property.KEY_COMMON_TAG));
		return data;
	}

	@Path
	public void updateImage(Request request) throws EndUserException {
		Long id = request.getId();
		String name = request.getString("name");
		String description = request.getString("description");
		List<String> tags = request.getStrings("tags");
		Image image = modelService.get(Image.class, id, request);
		image.setName(name);
		image.overrideFirstProperty(Image.PROPERTY_DESCRIPTION, description);
		image.overrideProperties(Property.KEY_COMMON_TAG, tags);
		modelService.update(image, request);
	}

	@Path
	public void listPersons(Request request) throws EndUserException, IOException {
		String subset = request.getString("subset");
		if ("invitations".equals(subset)) {
			listInvitations(request);
			return;
		}
		String text = request.getString("text");
		UserSession session = request.getSession();
		Query<Person> query = new Query<Person>(Person.class).as(session);
		query.withWords(text);
		List<Person> persons = modelService.list(query, request);

		ListWriter out = new ListWriter(request);
		out.startList();
		out.startHeaders().header("Name").header("Addresses").endHeaders();
		for (Person person : persons) {
			out.startRow().withId(person.getId());
			out.startCell().text(person.getFullName()).endCell();
			Long addressCount = modelService.count(Query.after(InternetAddress.class).to(person).as(session), request);
			out.startCell().text(addressCount).endCell();
			out.endRow();
		}
		out.endList();
	}

	@Path
	public void listInvitations(Request request) throws IOException, ModelException, NotFoundException {

		User user = getUser(request);
		List<Invitation> invitations = modelService.getChildren(user, Invitation.class, request);

		ListWriter out = new ListWriter(request);
		out.startList();
		out.startHeaders().header("Date").header("Code").header("State").header("Person").header("E-mail").endHeaders();
		for (Iterator<Invitation> i = invitations.iterator(); i.hasNext();) {
			Invitation invitation = i.next();
			DateTime created = new DateTime(invitation.getCreated().getTime());
			Person invited = modelService.getChild(invitation, Person.class, request);
			EmailAddress email = invited==null ? null : (EmailAddress) modelService.getChild(invited, EmailAddress.class, request);

			out.startRow().withId(invitation.getId()).withKind("invitation");
			out.startCell().text(created.toString("d/M-yyyy HH:mm")).endCell();
			out.startCell().text(invitation.getCode()).endCell();
			out.startCell().text(invitation.getState()).endCell();
			out.startCell().text(invited!=null ? invited.getName() : "-- no person --").endCell();
			out.startCell().text(email!=null ? email.getAddress(): "-- no email --").endCell();
			out.endRow();
		}
		out.endList();
	}

	@Path
	public Map<String,Object> loadPerson(Request request) throws ModelException, NotFoundException, BadRequestException {
		Long id = request.getId();

		Map<String,Object> data = new HashMap<String, Object>();
		Person person = modelService.getRequired(Person.class, id, request);
		data.put("person", person);
		List<EmailAddress> emails = modelService.getChildren(person, EmailAddress.class, request);
		data.put("emails", emails);
		List<PhoneNumber> phones = modelService.getChildren(person, PhoneNumber.class, request);
		data.put("phones", phones);
		// TODO Use personpespective
		return data;
	}

	@Path
	public void savePerson(Request request) throws EndUserException {
		PersonPerspective perspective = request.getObject("data", PersonPerspective.class);
		if (perspective==null) {
			throw new BadRequestException("Invalid data");
		}
		Person dummy = perspective.getPerson();
		Person person;
		if (dummy.getId()>0) {
			person = modelService.get(Person.class, dummy.getId(), request);
		} else {
			person = new Person();
		}
		person.setGivenName(dummy.getGivenName());
		person.setAdditionalName(dummy.getAdditionalName());
		person.setFamilyName(dummy.getFamilyName());
		person.setNamePrefix(dummy.getNamePrefix());
		person.setNameSuffix(dummy.getNameSuffix());
		modelService.createOrUpdate(person, request);
		personService.updateDummyEmailAddresses(person, perspective.getEmails(), request);
		personService.updateDummyPhoneNumbers(person, perspective.getPhones(), request);
	}

	@Path
	public Invitation createInvitation(Request request) throws EndUserException {
		String name = request.getString("name", "No name");
		String email = request.getString("email", "No email");
		String message = request.getString("message");
		User user = getUser(request);
		return invitationService.createAndSendInvitation(name, email, message, user, request);
	}

	private @NonNull User getUser(Request request) throws ModelException, NotFoundException {
		return modelService.getRequired(User.class, request.getSession().getIdentity(), request);
	}

	@Path
	public void deletePerson(Request request) throws ModelException, NotFoundException, SecurityException, BadRequestException {
		Long id = request.getId();
		Person person = modelService.getRequired(Person.class, id, request);
		List<EmailAddress> mails = modelService.getChildren(person, EmailAddress.class, request);
		for (EmailAddress mail : mails) {
			modelService.delete(mail, request);
		}
		List<PhoneNumber> phones = modelService.getChildren(person, PhoneNumber.class, request);
		for (PhoneNumber phone : phones) {
			modelService.delete(phone, request);
		}
		modelService.delete(person, request);
	}

	@Path
	public ListData listPrivateBookmarks(Request request) throws EndUserException {
		String search = request.getString("search");
		String tag = request.getString("tag");
		Long wordId = request.getLong("word",null);
		int page = request.getInt("page");
		Query<InternetAddress> query = new Query<InternetAddress>(InternetAddress.class).as(request.getSession()).withWords(search);
		query.withPaging(page, 30);
		if (Strings.isNotBlank(tag)) {
			query.withCustomProperty(Property.KEY_COMMON_TAG, tag);
		}
		if (wordId!=null) {
			Word word = modelService.get(Word.class, wordId, request);
			if (word!=null) {
				query.to(word);
			}
		}
		SearchResult<InternetAddress> result = modelService.search(query, request);

		List<InternetAddress> addresses = result.getList();
		ListData list = new ListData();
		list.setWindow(result.getTotalCount(), 30, page);
		list.addHeader("Titel");
		list.addHeader("Adresse");
		for (InternetAddress address : addresses) {
			Map<String,String> data = Maps.newHashMap();
			data.put("address", address.getAddress());
			list.newRow(address.getId(), "internetAddress", data);
			list.addCell(address.getName(),"common/internet");
			list.addCell(address.getAddress(),"monochrome/globe");
		}
		return list;
	}

	@Path
	public InternetAddressInfo getInternetAddress(Request request) throws ModelException {
		Long id = request.getLong("id", null);
		if (id!=null) {
			InternetAddress address = modelService.get(InternetAddress.class, id, request);
			if (address!=null) {
				InternetAddressInfo info = new InternetAddressInfo();
				info.setId(address.getId());
				info.setName(address.getName());
				info.setAddress(address.getAddress());
				info.setDescription(address.getPropertyValue(Property.KEY_COMMON_DESCRIPTION));
				info.setTags(address.getPropertyValues(Property.KEY_COMMON_TAG));
				return info;
			}
		}
		return null;
	}

	@Path
	public void saveInternetAddress(Request request) throws ModelException, SecurityException, BadRequestException, NotFoundException {
		InternetAddressInfo info = request.getObject("data", InternetAddressInfo.class);
		if (info==null) {
			throw new BadRequestException("Malformed data");
		}
		InternetAddress address;
		if (info.getId()!=null) {
			address = modelService.getRequired(InternetAddress.class, info.getId(), request);
		} else {
			address = new InternetAddress();
		}
		address.setAddress(info.getAddress());
		address.setName(info.getName());
		address.overrideFirstProperty(Property.KEY_COMMON_DESCRIPTION, info.getDescription());
		address.overrideProperties(Property.KEY_COMMON_TAG, info.getTags());
		modelService.createOrUpdate(address, request);
	}

	@Path
	public void addInternetAddress(Request request) throws EndUserException {
		try {
			informationService.addInternetAddress(request.getString("url"), request);
		} catch (IllegalArgumentException e) {
			throw new BadRequestException("Unable to create address");
		}
	}


	@Path
	public List<ItemData> getImageTags(Request request) throws EndUserException {
		return getTags(Image.class, request);
	}

	@Path
	public List<ItemData> getInternetAddressTagCloud(Request request) throws EndUserException {
		return getTags(InternetAddress.class, request);
	}

	private List<ItemData> getTags(Class<? extends Entity> type, Request request) {
		Map<String, Integer> properties = modelService.getProperties(Property.KEY_COMMON_TAG, type,request);
		List<ItemData> list = Lists.newArrayList();
		for (Entry<String,Integer> entry : properties.entrySet()) {
			ItemData item = new ItemData();
			item.setText(entry.getKey());
			item.setValue(entry.getKey());
			item.setKind("tag");
			item.setBadge(entry.getValue().toString());
			item.setIcon("common/folder");
			list.add(item);
		}
		return list;
	}

	@Path
	public List<ItemData> getInternetAddressWordCloud(Request request) throws ModelException {
		WordByInternetAddressQuery query = new WordByInternetAddressQuery(request);
		return modelService.list(query, request);
	}

	@Override
	public boolean isAllowed(Request request) {
		return !securityService.isPublicUser(request.getSession());
	}
}
