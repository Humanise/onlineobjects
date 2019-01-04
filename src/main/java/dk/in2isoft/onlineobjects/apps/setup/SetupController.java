package dk.in2isoft.onlineobjects.apps.setup;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.eclipse.jdt.annotation.Nullable;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import com.google.common.collect.Lists;

import dk.in2isoft.commons.lang.HTMLWriter;
import dk.in2isoft.commons.lang.Mapper;
import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.in2igui.data.ItemData;
import dk.in2isoft.in2igui.data.ListData;
import dk.in2isoft.in2igui.data.ListWriter;
import dk.in2isoft.in2igui.data.Option;
import dk.in2isoft.onlineobjects.apps.setup.perspectives.InternetAddressPerspective;
import dk.in2isoft.onlineobjects.apps.setup.perspectives.SchedulerStatusPerspective;
import dk.in2isoft.onlineobjects.apps.setup.perspectives.UserPerspective;
import dk.in2isoft.onlineobjects.core.Ability;
import dk.in2isoft.onlineobjects.core.Path;
import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.SearchResult;
import dk.in2isoft.onlineobjects.core.UserSession;
import dk.in2isoft.onlineobjects.core.exceptions.ContentNotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.ExplodingClusterFuckException;
import dk.in2isoft.onlineobjects.core.exceptions.IllegalRequestException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Client;
import dk.in2isoft.onlineobjects.model.EmailAddress;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.Image;
import dk.in2isoft.onlineobjects.model.InternetAddress;
import dk.in2isoft.onlineobjects.model.LogEntry;
import dk.in2isoft.onlineobjects.model.Person;
import dk.in2isoft.onlineobjects.model.Privilege;
import dk.in2isoft.onlineobjects.model.Property;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.model.annotations.Appearance;
import dk.in2isoft.onlineobjects.modules.index.IndexDescription;
import dk.in2isoft.onlineobjects.modules.index.IndexManager;
import dk.in2isoft.onlineobjects.modules.onlinepublisher.PublisherPerspective;
import dk.in2isoft.onlineobjects.modules.scheduling.JobInfo;
import dk.in2isoft.onlineobjects.modules.surveillance.LiveLogEntry;
import dk.in2isoft.onlineobjects.modules.surveillance.LogQuery;
import dk.in2isoft.onlineobjects.modules.surveillance.RequestInfo;
import dk.in2isoft.onlineobjects.ui.Request;
import dk.in2isoft.onlineobjects.util.Dates;
import dk.in2isoft.onlineobjects.util.Messages;

public class SetupController extends SetupControllerBase {
	
	@Override
	public void unknownRequest(Request request) throws IOException,EndUserException {
		if (!securityService.isAdminUser(request.getSession())) {
			request.redirectFromBase("/service/authentication/?redirect=/app/setup/&action=appAccessDenied");
		} else {
			String path = request.getLocalPathAsString();
			if (path.endsWith("gui") || path.endsWith("/")) {
				showGui(request);
			} else {
				super.unknownRequest(request);
			}
		}
	}
	
	@Path
	public void listUsers(Request request) throws IOException,EndUserException {
		User publicUser = securityService.getPublicUser();
		Privileged admin = securityService.getAdminPrivileged();
		Privileged privileged = request.getSession();
		int page = request.getInt("page");
		int pageSize = 40;
		Query<User> query = Query.of(User.class).withWords(request.getString("search")).withPaging(page, pageSize);
		SearchResult<User> result = modelService.search(query);
		
		ListWriter writer = new ListWriter(request);
		
		writer.startList();
		writer.window(result.getTotalCount(), pageSize, page);
		writer.startHeaders();
		writer.header("Name");
		writer.header("Username");
		writer.header("Person");
		writer.header("E-mail");
		writer.header("Status");
		writer.header("Access");
		writer.header("Public",1);
		writer.header("Self",1);
		writer.header("Admin",1);
		writer.endHeaders();
		for (User user : result.getList()) {
			Person person = modelService.getChild(user, Person.class, privileged);
			EmailAddress email = memberService.getUsersPrimaryEmail(user, privileged);
			writer.startRow().withId(user.getId()).withKind("user");
			writer.startCell().withIcon(user.getIcon()).text(user.getName()).endCell();
			writer.cell(user.getUsername());
			writer.startCell();
			if (person!=null) {
				writer.withIcon(person.getIcon()).text(person.getFullName());
			}
			writer.endCell();
			writer.startCell();
			if (email!=null) {
				writer.withIcon(email.getIcon());
				writer.text(email.getAddress());
			}
			writer.endCell();
			writer.startCell();
			// Status
			writer.endCell();
			writer.cell(user.getUpdated().toString());
			writer.startCell().startIcons();
			if (securityService.canView(user, publicUser)) {
				writer.icon("monochrome/view");
			}
			if (securityService.canModify(user, publicUser)) {
				writer.icon("monochrome/edit");
			}
			if (securityService.canDelete(user, publicUser)) {
				writer.icon("monochrome/delete");
			}
			writer.endIcons().endCell();
			writer.startCell().startIcons();
			if (securityService.canView(user, user)) {
				writer.icon("monochrome/view");
			}
			if (securityService.canModify(user, user)) {
				writer.icon("monochrome/edit");
			}
			if (securityService.canDelete(user, user)) {
				writer.icon("monochrome/delete");
			}
			writer.endIcons().endCell();
			writer.startCell().startIcons();
			if (securityService.canView(user, admin)) {
				writer.icon("monochrome/view");
			}
			if (securityService.canModify(user, admin)) {
				writer.icon("monochrome/edit");
			}
			if (securityService.canDelete(user, admin)) {
				writer.icon("monochrome/delete");
			}
			writer.endIcons().endCell();
			writer.endRow();
		}
		writer.endList();
	}
	
	@Path
	public void listUsersObjects(Request request) throws IOException,EndUserException {
		long id = request.getLong("userId");
		String type = request.getString("type");
		
		User user = modelService.get(User.class, id, request.getSession());
		if (user==null) {
			return;
		}
		if ("info".equals(type)) {
			listUserInfo(request, user);
			return;
		} else if ("clients".equals(type)) {
			listUsersClients(request, user);
			return;
		} else {
			listUsersObjects(request, type, user);
		}
	}

	private void listUsersObjects(Request request, String type, User user) throws IOException {
		Class<? extends Entity> typeClass = modelService.getEntityClass(type);
		if (typeClass==null) {
			return;
		}
		int page = request.getInt("page");
		Query<? extends Entity> query = Query.of(typeClass).as(user).withPaging(page, 30);
		SearchResult<? extends Entity> result = modelService.search(query);

		User publicUser = securityService.getPublicUser();

		ListWriter writer = new ListWriter(request);
		
		writer.startList();
		writer.window(result.getTotalCount(), 30, page);
		writer.startHeaders();
		writer.header("Name",40);
		writer.header("Type");
		writer.header("ID");
		writer.header("Private grants",1);
		writer.header("Public grants",1);
		writer.endHeaders();
		for (Entity entity : result.getList()) {
			Privilege privilege = securityService.getPrivilege(entity.getId(), user);
			Privilege publicPrivilege = securityService.getPrivilege(entity.getId(), publicUser);
			writer.startRow();
			writer.startCell().withIcon(entity.getIcon()).startWrap().text(entity.getName()).endWrap().endCell();
			writer.startCell().text(entity.getClass().getSimpleName()).endCell();
			writer.cell(entity.getId());
			writer.startCell().nowrap();
			if (privilege.isView()) {
				writer.icon("monochrome/view");
			}
			if (privilege.isAlter()) {
				writer.icon("monochrome/edit");
			}
			if (privilege.isDelete()) {
				writer.icon("monochrome/delete");
			}			
			writer.endCell();
			writer.startCell().nowrap();
			if (publicPrivilege!=null) {
				if (publicPrivilege.isView()) {
					writer.icon("monochrome/view");
				}
				if (publicPrivilege.isAlter()) {
					writer.icon("monochrome/edit");
				}
				if (publicPrivilege.isDelete()) {
					writer.icon("monochrome/delete");
				}
			}
			writer.endCell();
			writer.endRow();
		}
		writer.endList();
	}

	private void listUserInfo(Request request, User user) throws IOException, ModelException {
		
		UserSession privileged = request.getSession();
		Person person = memberService.getUsersPerson(user, privileged);
		EmailAddress email = memberService.getUsersPrimaryEmail(user, privileged);
		ListWriter writer = new ListWriter(request);
		
		writer.startList();
		writer.startHeaders();
		writer.header("Key",40);
		writer.header("Value");
		writer.endHeaders();
		writer.startRow().cell("ID").cell(user.getId()).endRow();
		writer.startRow().cell("Password").cell((Strings.isNotBlank(user.getPassword()) ? "Yes" : "No")).endRow();
		writer.startRow().cell("Secret").cell(user.getPropertyValue(Property.KEY_AUTHENTICATION_SECRET)).endRow();
		modelService.getChildren(user, Client.class, user).forEach(client -> {
			writer.startRow().cell("Client: "+client.getName()).cell(client.getUUID() + " / " + client.getPropertyValue(Property.KEY_AUTHENTICATION_SECRET)).endRow();
		});
		if (email != null) {
			writer.startRow().cell("Primary email").cell(email.getAddress()).endRow();
			Date emailRequestTime = email.getPropertyDateValue(Property.KEY_EMAIL_CONFIRMATION_REQUEST_TIME);
			writer.startRow().cell("Confirmation sent:").startCell();
			if (emailRequestTime != null) {
				writer.text(emailRequestTime).text(" ~ ").text(Dates.formatDurationFromNow(emailRequestTime));				
			} else {
				writer.text("Unknown");
			}
			writer.endCell().endRow();
			Date confirmationTime = email.getPropertyDateValue(Property.KEY_CONFIRMATION_TIME);
			if (confirmationTime!=null) {
				writer.startRow().cell("Primary email confirmed").startCell();
				writer.text(confirmationTime.toString());				
				writer.text(" ~ ").text(Dates.formatDurationFromNow(confirmationTime));
				writer.endCell().endRow();
				
			}

		}
		if (person!=null) {
			writer.startRow().cell("Person").cell(person.getFullName()).endRow();
			List<EmailAddress> emails = modelService.getChildren(person, EmailAddress.class, privileged);
			if (!emails.isEmpty()) {
				writer.startRow().cell("E-mails").startCell();
				for (Iterator<EmailAddress> i = emails.iterator(); i.hasNext();) {
					EmailAddress mail = i.next();
					writer.text(mail.getAddress());
					if (i.hasNext()) writer.text(", ");
				}
				writer.endCell().endRow();
			}
		}
		Image image = modelService.getChild(user, Image.class, privileged);
		if (image != null) {
			writer.startRow().cell("Profile image").cell(image.getName()).endRow();
		}
		writer.startRow().cell("Terms acceptance").startCell();
		Date termsAcceptanceTime = user.getPropertyDateValue(Property.KEY_TERMS_ACCEPTANCE_TIME);
		if (termsAcceptanceTime != null) {
			writer.text(termsAcceptanceTime).text(" ~ ").text(Dates.formatDurationFromNow(termsAcceptanceTime));
		}
		writer.endCell().endRow();
		writer.endList();
	}

	private void listUsersClients(Request request, User user) throws IOException, ModelException {
		ListWriter writer = new ListWriter(request);
		
		writer.startList();
		writer.startHeaders();
		writer.header("Name");
		writer.header("ID");
		writer.header("Secret");
		writer.header("Version");
		writer.endHeaders();
		modelService.getChildren(user, Client.class, user).forEach(client -> {
			String secret = client.getPropertyValue(Property.KEY_AUTHENTICATION_SECRET);
			String version = client.getPropertyValue(Property.KEY_CLIENT_VERSION);
			String build = client.getPropertyValue(Property.KEY_CLIENT_BUILD);
			String platform = client.getPropertyValue(Property.KEY_CLIENT_PLATFORM);
			String platformVersion = client.getPropertyValue(Property.KEY_CLIENT_PLATFORM_VERSION);
			String hardwareVersion = client.getPropertyValue(Property.KEY_CLIENT_HARDWARE_VERSION);
			String hardware = client.getPropertyValue(Property.KEY_CLIENT_HARDWARE);
			writer.startRow();
			writer.cell(client.getName());
			writer.startCell().startWrap().text(client.getUUID()).endWrap().endCell();
			writer.startCell().startWrap().text(secret).endWrap().endCell();
			writer.startCell().text("Version: " + version + " (" + build + ")");
			writer.lineBreak().text("Platform: " + platform + " / " + platformVersion);
			writer.lineBreak().text("Hardware: " + hardware + " / " + hardwareVersion);
			writer.endCell();
			writer.endRow();
		});
		writer.endList();
	}

	@Path
	public UserPerspective loadUser(Request request) throws IOException,EndUserException {
		try {Thread.sleep(1000);} catch (InterruptedException e) {}
		Long id = request.getLong("id");
		User user = modelService.get(User.class, id, request.getSession());
		if (user==null) {
			throw new ContentNotFoundException("User not found (id="+id+")");
		}
		User publicUser = securityService.getPublicUser();
		UserPerspective perspective = new UserPerspective();
		perspective.setUsername(user.getUsername());
		perspective.setName(user.getName());
		EmailAddress usersPrimaryEmail = memberService.getUsersPrimaryEmail(user, request.getSession());
		if (usersPrimaryEmail != null) {
			perspective.setEmail(usersPrimaryEmail.getAddress());
		}
		perspective.setPublicView(securityService.canView(user, publicUser));
		perspective.setAbilities(Ability.convert(user.getPropertyValues(Property.KEY_ABILITY)));
		return perspective;
	}
	
	@Path
	public void deleteUser(Request request) throws EndUserException {
		UserSession privileged = request.getSession();
		Long id = request.getId();
		User user = modelService.getRequired(User.class, id, privileged);
		memberService.deleteMember(user, privileged);
	}
	
	@Path
	public void sendPasswordReset(Request request) throws EndUserException {
		Long id = request.getId();
		User user = modelService.getRequired(User.class, id, request.getSession());
		if (!passwordRecoveryService.sendRecoveryMail(user)) {
			throw new EndUserException("Unable to send recovery mail");
		}
	}
	
	@Path
	public void sendEmailConfirmation(Request request) throws EndUserException {
		Long id = request.getId();
		User user = modelService.getRequired(User.class, id, request.getSession());
		memberService.sendEmailConfirmation(user, request.getSession());
	}

	@Path
	public void checkHealth(Request request) throws EndUserException {
		Long id = request.getId();
		User user = modelService.getRequired(User.class, id, request.getSession());
		memberService.scheduleHealthCheck(user);
	}

	@Path
	public void saveUser(Request request) throws IOException,EndUserException {
		UserPerspective perspective = request.getObject("user", UserPerspective.class);
		if (perspective==null) {
			throw new IllegalRequestException("No user provider");
		}
		User user = modelService.get(User.class, perspective.getId(), request.getSession());
		if (user==null) {
			throw new ContentNotFoundException("User not found (id="+perspective.getId()+")");
		}
		if (securityService.canChangeUsername(user)) {
			user.setUsername(perspective.getUsername());			
		}
		if (Strings.isNotBlank(perspective.getEmail())) {
			memberService.changePrimaryEmail(user, perspective.getEmail(), request.getSession());
		}
		Set<Ability> abilities = perspective.getAbilities();
		user.removeProperties(Property.KEY_ABILITY);
		if (abilities!=null) {
			for (Ability ability : abilities) {
				user.addProperty(Property.KEY_ABILITY, ability.name());
			}
		}
		user.setName(perspective.getName());
		modelService.update(user, request.getSession());
		if (securityService.isAdminUser(user)) {
			modelService.grantPrivileges(user, user, true, true, false, securityService.getAdminPrivileged());
			securityService.grantPublicView(user, perspective.isPublicView(), request.getSession());
		} else if (securityService.isPublicUser(user)) {
			securityService.makePublicVisible(user, request.getSession());
			// TODO: Does it make sense to grant administrator privileges?
			modelService.grantPrivileges(user, securityService.getAdminPrivileged(), true, true, false, securityService.getAdminPrivileged());
		} else {
			modelService.grantPrivileges(user, user, true, true, true, securityService.getAdminPrivileged());
			securityService.grantPublicView(user, perspective.isPublicView(), request.getSession());
		}
	}
	
	@Path
	public void listJobLog(Request request) throws IOException {
		ListWriter writer = new ListWriter(request);
		writer.startList();
		writer.startHeaders();
		writer.header("Time",10).header("Text").header("Name").header("Group");
		writer.endHeaders();
		List<LiveLogEntry> liveLog = schedulingService.getLiveLog();
		for (LiveLogEntry entry : liveLog) {
			writer.startRow();
			writer.startCell().text(Dates.formatTime(entry.getDate(), request.getLocale())).endCell();
			writer.startCell().text(entry.getTitle()).endCell();
			writer.startCell().text(entry.getName()).endCell();
			writer.startCell().text(entry.getGroup()).endCell();
			writer.endRow();
		}
		writer.endList();
	}
	
	@Path
	public void listJobs(Request request) throws SecurityException, IOException {
		Messages msg = new Messages(this);
		Locale locale = request.getLocale();
		PeriodFormatter pf = new PeriodFormatterBuilder().appendHours().appendSeparator(":").appendMinutes().appendSeparator(":").appendSeconds().toFormatter();
		boolean active = schedulingService.isRunning();
		
		ListWriter writer = new ListWriter(request);
		writer.startList();
		writer.startHeaders();
		writer.header("Name",10).header("Group",10).header("Status",15).header("",10).header("Timing",10).header("Latest",10).header("Next",10).header("State").header("", 1);
		writer.endHeaders();
		long now = System.currentTimeMillis();
		List<JobInfo> jobList = schedulingService.getJobList();
		for (JobInfo status : jobList) {
			Map<String,Object> data = Mapper.<String,Object>build("group", status.getGroup()).add("name", status.getName()).add("status", status.getTriggerState()).add("running", new Boolean(status.isRunning())).get();
			boolean paused = "PAUSED".equals(status.getTriggerState());
			
			writer.startRow().withId(status.getGroup()+"-"+status.getName()).withData(data);
			writer.startCell();
			if (status.isRunning()) {
				writer.withIcon("status/running");
			} else {
				if (paused) {
					writer.withIcon("status/paused");
				} else {
					writer.withIcon("status/waiting");
				}
			}
			writer.text(status.getName()).endCell();
			writer.startCell().text(status.getGroup()).endCell();
			writer.startCell();
			if (status.isRunning()) {
				writer.text(pf.print(new Period(status.getCurrentRunTime()).toPeriod()));
			} else {
				if (paused) {
					writer.text("Paused");
				} else if (status.getNextRun()!=null && active) {
					writer.text(pf.print(new Period(status.getNextRun().getTime()-now)));
				} else {
					writer.text(msg.get("job_waiting",locale));
				}
			}
			writer.endCell();
			writer.startCell();
			if (status.isRunning()) {
				writer.progress(status.getProgress());
			}
			writer.endCell();
			writer.startCell().text(status.getTriggerTiming()).endCell();
			writer.startCell().text(Dates.formatTime(status.getLatestRun(), locale)).endCell();
			writer.startCell().text(Dates.formatTime(status.getNextRun(), locale)).endCell();
			writer.startCell().text(status.getTriggerState()).endCell();
			writer.startCell();
			if (!"BLOCKED".equals(status.getTriggerState())) {
				writer.startIcons().startActionIcon("monochrome/play").withData("play").endIcon().endIcons();
			}
			writer.endCell();
			writer.endRow();
		}
		writer.endList();
	}

	@Path
	public void startJob(Request request) throws SecurityException, IOException {
		schedulingService.runJob(request.getString("name"), request.getString("group"));
	}

	@Path
	public void stopJob(Request request) throws SecurityException, IOException {
		schedulingService.stopJob(request.getString("name"), request.getString("group"));
	}

	@Path
	public void pauseJob(Request request) throws SecurityException, IOException {
		schedulingService.pauseJob(request.getString("name"), request.getString("group"));
	}
	
	@Path
	public void resumeJob(Request request) throws SecurityException, IOException {
		schedulingService.resumeJob(request.getString("name"), request.getString("group"));
	}

	@Path
	public void toggleScheduler(Request request) throws SecurityException, IOException {
		if (request.isSet("active")) {
			schedulingService.setActive(request.getBoolean("active"));
		} else {
			schedulingService.toggle();
		}
	}

	@Path
	public SchedulerStatusPerspective getSchedulerStatus(Request request) {
		SchedulerStatusPerspective status = new SchedulerStatusPerspective();
		status.setRunning(schedulingService.isRunning());
		return status;
	}
	
	@Path
	public void getSurveillanceList(Request request) throws IOException, ModelException {
		String kind = request.getString("kind");
		if ("longestRunningRequests".equals(kind)) {
			ListData data = new ListData();
			List<RequestInfo> requests = surveillanceService.getLongestRunningRequests();
			data.addHeader("URI");
			data.addHeader("Hits");
			data.addHeader("Average");
			data.addHeader("Max");
			data.addHeader("Min");
			data.addHeader("Total");
			
			for (RequestInfo info : requests) {
				data.newRow();
				data.addCell(info.getUri());
				data.addCell(String.valueOf(info.getCounts()));
				data.addCell(String.valueOf(info.getAverageRunningTime()));
				data.addCell(String.valueOf(info.getMaxRunningTime()));
				data.addCell(String.valueOf(info.getMinRunningTime()));
				data.addCell(localizationService.formatMilis(info.getTotalRunningTime()));
			}
			request.sendObject(data);
		} else if ("liveLog".equals(kind)) {
			ListWriter writer = new ListWriter(request);
			
			writer.startList();
			
			writer.startHeaders().header("Time").header("Title").header("Details").endHeaders();
			
			Locale locale = request.getLocale();
			List<dk.in2isoft.onlineobjects.modules.surveillance.LiveLogEntry> entries = surveillanceService.getLogEntries();
			Collections.reverse(entries);
			for (dk.in2isoft.onlineobjects.modules.surveillance.LiveLogEntry entry : entries) {
				writer.startRow();
				writer.startCell().text(Dates.formatTime(entry.getDate(), locale)).endCell();
				writer.startCell().text(entry.getTitle()).endCell();
				writer.startCell().text(entry.getDetails()).endCell();
				writer.endRow();
			}
			writer.endList();
		} else if ("log".equals(kind)) {
			ListWriter writer = new ListWriter(request);
			
			int page = request.getInt("page");
			int size = 100;

			LogQuery query = new LogQuery().withPage(page).withSize(size);
			SearchResult<LogEntry> result = modelService.search(query);
			writer.startList();
			writer.window(result.getTotalCount(), size, page);
			
			writer.startHeaders().header("Time").header("Level").header("User").header("Type").endHeaders();
			
			Locale locale = request.getLocale();

			SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss",locale);
			for (LogEntry entry : result.getList()) {
				writer.startRow();
				writer.startCell().text(format.format(entry.getTime())).endCell();
				writer.startCell().text(entry.getLevel()).endCell();
				writer.startCell();
				if (entry.getSubject() != null) {
					@Nullable
					User user = modelService.get(User.class, entry.getSubject(), request.getSession());
					if (user!=null) {
						writer.text(user.getUsername());
					} else {
						writer.text("Not found: " + entry.getSubject());
					}
				}
				writer.endCell();
				writer.startCell().text(entry.getType()).endCell();
				writer.endRow();
			}
			writer.endList();
			
		} else {
			ListData data = new ListData();
			Collection<String> exceptions = surveillanceService.getLatestExceptions();
			List<String> reversed = Lists.newArrayList(exceptions);
			Collections.reverse(reversed);
			data.addHeader("Exception");
			for (String string : reversed) {
				data.newRow();
				data.addCell(string);
			}
			request.sendObject(data);
		}
	}

	@Path
	public void sendSurveillanceReport(Request request) throws EndUserException {
		surveillanceService.sendReport();
	}
	
	@Path
	public void changeAdminPassword(Request request) throws EndUserException {
		throw new IllegalRequestException("This is deprecated!");
	}
	
	@Path
	public ListData listEntities(Request request) throws SecurityException, ClassNotFoundException, IOException {
		User publicUser = securityService.getPublicUser();
		int page = request.getInt("page");
		String clazz = request.getString("type");
		String text = request.getString("text");
		Class<? extends Entity> className = modelService.getEntityClass(clazz);
		ListData list = new ListData();
		list.addHeader("Name");
		list.addHeader("Type");
		list.addHeader("Public view");
		list.addHeader("Public modify");
		list.addHeader("Public delete");
		if (className!=null) {
			Query<? extends Entity> query = Query.of(className).withWords(text).withPaging(page, 50);
			SearchResult<? extends Entity> result = modelService.search(query);
			list.setWindow(result.getTotalCount(), 50, page);
			for (Entity entity : result.getList()) {
				String kind = entity.getClass().getSimpleName().toLowerCase();
				list.newRow(entity.getId(),kind);
				list.addCell(entity.getName(), entity.getIcon());
				list.addCell(entity.getType());
				list.addCell(securityService.canView(entity, publicUser));
				list.addCell(securityService.canModify(entity, publicUser));
				list.addCell(securityService.canDelete(entity, publicUser));
			}
		}
		return list;
	}
	
	@Path
	public Collection<ItemData> getClasses(Request request) {
		Collection<Class<? extends Entity>> classes = modelService.getEntityClasses();
		Collection<ItemData> items = Lists.newArrayList();
		for (Class<?> clazz : classes) {
			ItemData data = new ItemData();
			data.setValue(clazz.getSimpleName());
			data.setText(clazz.getSimpleName());
			Appearance annotation = clazz.getAnnotation(Appearance.class);
			if (annotation!=null) {
				data.setIcon(annotation.icon());
			} else {
				data.setIcon("monochrome/round_question");
			}
			items.add(data);
		}
		return items;
	}
	
	@Path
	public EntityInfo getEntityInfo(Request request) {
		long id = request.getLong("id");
		EntityInfo info = new EntityInfo();
		info.setId(id);
		Privilege privilege = securityService.getPrivilege(id,securityService.getPublicUser());
		if (privilege!=null) {
			info.setPublicAlter(privilege.isAlter());
			info.setPublicDelete(privilege.isDelete());
			info.setPublicView(privilege.isView());
		}
		return info;
	}
	
	@Path
	public void updateEntityInfo(Request request) throws ModelException, SecurityException {
		EntityInfo info = request.getObject("data", EntityInfo.class);
		Entity entity = modelService.get(Entity.class, info.getId(), request.getSession());
		securityService.grantPublicView(entity, info.isPublicView(), request.getSession());
	}
	
	@Path
	public List<Image> listImages(Request request) throws EndUserException {
		String text = request.getString("text");
		String tag = request.getString("tag");
		Query<Image> query = new Query<Image>(Image.class).withPaging(0, 50).orderByCreated().descending();
		query.withWords(text);
		if (Strings.isNotBlank(tag)) {
			query.withCustomProperty(Property.KEY_COMMON_TAG, tag);
		}
		return modelService.list(query);
	}

	@Path
	public List<ItemData> getImageTags(Request request) throws EndUserException {
		Map<String, Integer> properties = modelService.getProperties(Property.KEY_COMMON_TAG, Image.class, null);
		List<ItemData> items = Lists.newArrayList();
		for (Entry<String, Integer> itemData : properties.entrySet()) {
			ItemData data = new ItemData();
			data.setValue(itemData.getKey());
			data.setText(itemData.getKey());
			data.setBadge(itemData.getValue().toString());
			data.setIcon("common/folder");
			items.add(data);
		}
		return items;
	}

	@Path
	public void listPublishers(Request request) throws EndUserException, IOException {
		
		List<InternetAddress> sites = onlinePublisherService.getSites(request.getSession());
		
		ListWriter writer = new ListWriter(request);
		writer.startList();
		writer.startHeaders().header("Name").header("Address").endHeaders();
		for (InternetAddress address : sites) {
			writer.startRow().withId(address.getId());
			writer.startCell().withIcon(address.getIcon()).text(address.getName()).endCell();
			writer.startCell().text(address.getAddress()).endCell();
			writer.endRow();
		}
		writer.endList();
	}
	
	@Path
	public void savePublisher(Request request) throws IOException,EndUserException {
		PublisherPerspective perspective = request.getObject("publisher", PublisherPerspective.class);
		Privileged privileged = request.getSession();
		onlinePublisherService.createOrUpdatePublisher(perspective, privileged);
	}

	@Path
	public PublisherPerspective loadPublisher(Request request) throws IOException,EndUserException {
		return onlinePublisherService.getPublisherPerspective(request.getLong("id"), request.getSession());
	}

	@Path
	public void deletePublisher(Request request) throws IOException,EndUserException {
		onlinePublisherService.deletePublisher(request.getLong("id"), request.getSession());
	}

	@Path
	public void listInternetAddresses(Request request) throws ModelException, IOException {
		int page = request.getInt("page");
		int size = 50;
		
		SearchResult<InternetAddress> result = modelService.search(Query.after(InternetAddress.class).withPaging(page, 50));
		
		ListWriter writer = new ListWriter(request);
		writer.startList();
		writer.window(result.getTotalCount(), size, page);
		writer.startHeaders().header("Name",40).header("Address").endHeaders();
		for (InternetAddress address : result.getList()) {
			writer.startRow().withId(address.getId());
			writer.startCell().withIcon(address.getIcon()).text(address.getName()).endCell();
			writer.startCell().startWrap().text(address.getAddress()).endWrap().endCell();
			writer.endRow();
		}
		writer.endList();
	}

	@Path
	public InternetAddressPerspective getInternetAddressesInfo(Request request) throws IOException,EndUserException {
		Long id = request.getLong("id");
		InternetAddressPerspective perspective = new InternetAddressPerspective();
		InternetAddress address = modelService.get(InternetAddress.class, id, request.getSession());
		if (address==null) {
			throw new ContentNotFoundException("The address could not be found, id = "+id);
		}
		String content = address.getPropertyValue(Property.KEY_INTERNETADDRESS_CONTENT);
		perspective.setContent(content);
		perspective.setId(address.getId());
		perspective.setTitle(address.getName());
		perspective.setRendering(buildRendering(address,content));
		return perspective;
	}
	
	private String buildRendering(InternetAddress address, String content) {
		HTMLWriter html = new HTMLWriter();
		
		html.startH1().text(address.getName()).endH1();
		html.startP().startA().withHref(address.getAddress()).text(address.getAddress()).endA().endP();
		if (Strings.isNotBlank(content)) {
			String[] lines = StringUtils.split(content, "\n");
			html.startDiv().withClass("body");
			for (int i = 0; i < lines.length; i++) {
				html.startP().text(lines[i]).endP();
			}
			html.endDiv();
		} else {
			html.startP().text("No content").endP();
		}
		return html.toString();
	}
	
	@Path
	public List<ItemData> getIndexOptions(Request request) {
		List<ItemData> options = Lists.newArrayList();
		indexService.getIndexers().forEach(indexer -> {
			indexer.getIndexInstances().forEach(name -> {
				ItemData item = new ItemData();
				item.setText(name.getName());
				item.setValue(Strings.toJSON(name));
				item.setKind("index");
				options.add(item);
			});			
		});
		return options;
	}

	@Path
	public void getIndexDocuments(Request request) throws IOException, EndUserException {
		IndexDescription desc = request.getObject("name", IndexDescription.class);
		int page = request.getInt("page");
		int count = request.getInt("count");
		count = 30;
		if (desc == null) {
			return;
		}
		IndexManager manager = indexService.getIndex(desc.getName());
		if (manager==null) {
			throw new IllegalRequestException("No index manager width the name '"+desc.getName()+"'");
		}
		SearchResult<Document> result;
		try {
			result = manager.getDocuments(page,count);
		} catch (ExplodingClusterFuckException e) {
			return;
		}

		ListWriter writer = new ListWriter(request);
		List<Document> list = result.getList();
		writer.startList();
		writer.window(result.getTotalCount(), count, page);
		writer.startHeaders();
		writer.header("ID", 30);
		writer.header("Type");
		writer.header("Text");
		writer.endHeaders();
		for (Document document : list) {
			writer.startRow().cell(document.get("id")).cell(document.get("type")).cell(document.get("text"));
			writer.endRow();			
		}
		writer.endList();
	}
	
	@Path
	public void getIndexStatistics(Request request) throws IOException, EndUserException {
		IndexDescription desc = request.getObject("name", IndexDescription.class);
		if (desc == null) {
			return;
		}
		IndexManager manager = indexService.getIndex(desc.getName());
		if (manager==null) {
			throw new IllegalRequestException("No index manager width the name '" + desc.getName() + "'");
		}
		ListWriter writer = new ListWriter(request);
		writer.startList();
		writer.startHeaders();
		writer.header("Property", 30);
		writer.header("Value");
		writer.endHeaders();
		writer.startRow().cell("Index count").cell(manager.getDocumentCount()).endRow();
		writer.startRow().cell("Database count").cell(indexService.getObjectCount(desc)).endRow();
		writer.endList();
	}
	
	@Path
	public void createMember(Request request) throws IOException, EndUserException {
		UserSession session = request.getSession();
		String username = request.getString("username", "No username");
		String password = request.getString("password", "No password");
		String fullName = request.getString("name", "No full name");
		String email = request.getString("email", "No e-mail");
		if (username != null) {
			username = username.toLowerCase();
		}
		memberService.createMember(session, username, password, fullName, email);
	}
	
	@Path
	public List<Option> abilityOptions(Request request) {
		List<Option> options = new ArrayList<>();
		for (Ability ability : Ability.values()) {
			options.add(new ItemData(ability.name(), ability.name()));
		}
		return options;
	}
}
