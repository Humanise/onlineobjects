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
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.Path;
import dk.in2isoft.onlineobjects.core.Path.Method;
import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.SearchResult;
import dk.in2isoft.onlineobjects.core.UserStatisticsQuery;
import dk.in2isoft.onlineobjects.core.UserStatisticsQuery.UserStatistic;
import dk.in2isoft.onlineobjects.core.View;
import dk.in2isoft.onlineobjects.core.exceptions.BadRequestException;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.ExplodingClusterFuckException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.NotFoundException;
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
		
	@Path(exactly = "applications.gui")
	@View(ui = "applications.gui.xml")
	public void applications(Request request) {}
	
	@Path(exactly = "images.gui")
	@View(ui = "images.gui.xml")
	public void images(Request request) {}
	
	@Path(expression = "/")
	@View(ui = "index.gui.xml")
	public void index(Request request) {}
	
	@Path(exactly = "indices.gui")
	@View(ui = "indices.gui.xml")
	public void indices(Request request) {}
	
	@Path(exactly = "integration.gui")
	@View(ui = "integration.gui.xml")
	public void integration(Request request) {}
	
	@Path(exactly = "internetaddresses.gui")
	@View(ui = "internetaddresses.gui.xml")
	public void internetaddresses(Request request) {}
	
	@Path(exactly = "model.gui")
	@View(ui = "model.gui.xml")
	public void model(Request request) {}
	
	@Path(exactly = "scheduler.gui")
	@View(ui = "scheduler.gui.xml")
	public void scheduler(Request request) {}
	
	@Path(exactly = "settings.gui")
	@View(ui = "settings.gui.xml")
	public void settings(Request request) {}
	
	@Path(exactly = "surveillance.gui")
	@View(ui = "surveillance.gui.xml")
	public void surveillance(Request request) {}
	
	@Path(exactly = "users.gui")
	@View(ui = "users.gui.xml")
	public void users(Request request) {}

	
	
	@Path(expression = "/settings/data", method = Method.GET)
	public Object settingsData(Request request) throws IOException,EndUserException {
		return Map.of(
			"wordsRequestsPerSecond", wordsLoadManager.getRequestsPerSecond(),
			"wordsTimeout", wordsLoadManager.getTimeout()
		);
	}

	@Path(expression = "/settings/data", method = Method.POST)
	public void saveSettingsData(Request request) throws IOException,EndUserException {
		double rate = request.getDouble("wordsRequestsPerSecond");
		long timeout = request.getLong("wordsTimeout");
		if (rate <= 0 || timeout < 1) {
			throw new BadRequestException("Rate must be positive and timeout above 1");
		}
		wordsLoadManager.setTimeout(timeout);
		wordsLoadManager.setRequestsPerSecond(rate);
	}

	@Path
	public void flushCache(Request request) throws IOException,EndUserException {
		cacheService.flushToDisk();
	}

	@Path
	public void listUsers(Request request) throws IOException,EndUserException {
		User publicUser = securityService.getPublicUser();
		Privileged admin = securityService.getAdminPrivileged();
		boolean showPrivileges = request.getBoolean("showPrivileges");
		int page = request.getInt("page");
		int pageSize = 40;
		Query<User> query = Query.of(User.class).withWords(request.getString("search")).withPaging(page, pageSize);
		SearchResult<User> result = modelService.search(query, request);
		
		List<UserStatisticsQuery.UserStatistic> list = modelService.list(new UserStatisticsQuery(securityService), request);
		
		ListWriter writer = new ListWriter(request);
		
		writer.startList();
		writer.window(result.getTotalCount(), pageSize, page);
		writer.startHeaders();
		writer.header("Username");
		writer.header("Person");
		writer.header("E-mail");
		writer.header("Objects");
		writer.header("Latest");
		if (showPrivileges) {
			writer.header("Public",1);
			writer.header("Self",1);
		}
		writer.endHeaders();
		for (User user : result.getList()) {
			UserStatistic statistic = getStatistics(list, user);
			Person person = modelService.getChild(user, Person.class, request);
			EmailAddress email = memberService.getUsersPrimaryEmail(user, request);
			writer.startRow().withId(user.getId()).withKind("user");
			writer.startCell().withIcon(user.getIcon()).text(user.getUsername()).endCell();
			writer.startCell();
			if (person!=null) {
				writer.withIcon(person.getIcon()).text(person.getFullName());
			}
			writer.endCell();
			writer.startCell();
			if (email!=null) {
				writer.withIcon(email.getIcon());
				writer.text(email.getAddress());

				Date confirmationTime = email.getPropertyDateValue(Property.KEY_CONFIRMATION_TIME);
				if (confirmationTime!=null) {			
					writer.text(" ~ ").text(Dates.getDaysFromNow(confirmationTime) + " days");
				} else {
					writer.startIcons();
					writer.icon("common/warning");
					writer.endIcons();
				}
			}
			writer.endCell();
			writer.startCell();
			if (statistic != null) {
				writer.text(statistic.entityCount);
			}
			writer.endCell();

			writer.startCell();
			if (statistic != null) {
				writer.text(statistic.latestModification);
			}
			writer.endCell();
			if (showPrivileges) {
				writer.startCell().startIcons();
				if (securityService.canView(user, publicUser, request)) {
					writer.icon("monochrome/view");
				}
				if (securityService.canModify(user, publicUser, request)) {
					writer.icon("monochrome/edit");
				}
				if (securityService.canDelete(user, publicUser, request)) {
					writer.icon("monochrome/delete");
				}
				writer.endIcons().endCell();
				writer.startCell().startIcons();
				if (securityService.canView(user, user, request)) {
					writer.icon("monochrome/view");
				}
				if (securityService.canModify(user, user, request)) {
					writer.icon("monochrome/edit");
				}
				if (securityService.canDelete(user, user, request)) {
					writer.icon("monochrome/delete");
				}
				writer.endIcons().endCell();
				writer.startCell().startIcons();
				if (securityService.canView(user, admin, request)) {
					writer.icon("monochrome/view");
				}
				if (securityService.canModify(user, admin, request)) {
					writer.icon("monochrome/edit");
				}
				if (securityService.canDelete(user, admin, request)) {
					writer.icon("monochrome/delete");
				}
				writer.endIcons().endCell();
			}
			writer.endRow();
		}
		writer.endList();
	}
	
	private UserStatistic getStatistics(List<UserStatistic> list, User user) {
		for (UserStatistic stat : list) {
			if (stat.userId == user.getId()) {
				return stat;
			}
		}
		return null;
	}

	@Path
	public void listUsersObjects(Request request) throws IOException,EndUserException {
		long id = request.getLong("userId");
		String type = request.getString("type");
		
		User user = modelService.get(User.class, id, request);
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
		SearchResult<? extends Entity> result = modelService.search(query, request);

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
			Privilege privilege = securityService.getPrivilege(entity.getId(), user, request);
			Privilege publicPrivilege = securityService.getPrivilege(entity.getId(), publicUser, request);
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
		
		Person person = memberService.getUsersPerson(user, request);
		EmailAddress email = memberService.getUsersPrimaryEmail(user, request);
		ListWriter writer = new ListWriter(request);
		
		writer.startList();
		writer.startHeaders();
		writer.header("Key",40);
		writer.header("Value");
		writer.endHeaders();
		writer.startRow().cell("ID").cell(user.getId()).endRow();
		writer.startRow().cell("Password").cell((Strings.isNotBlank(user.getPassword()) ? "Yes" : "No")).endRow();
		writer.startRow().cell("Secret").cell(user.getPropertyValue(Property.KEY_AUTHENTICATION_SECRET)).endRow();
		modelService.getChildren(user, Client.class, request).forEach(client -> {
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
			List<EmailAddress> emails = modelService.getChildren(person, EmailAddress.class, request);
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
		Image image = modelService.getChild(user, Image.class, request);
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
		writer.header("Created");
		writer.endHeaders();
		modelService.getChildren(user, Client.class, request).forEach(client -> {
			String secret = client.getPropertyValue(Property.KEY_AUTHENTICATION_SECRET);
			String version = client.getPropertyValue(Property.KEY_CLIENT_VERSION);
			String build = client.getPropertyValue(Property.KEY_CLIENT_BUILD);
			String platform = client.getPropertyValue(Property.KEY_CLIENT_PLATFORM);
			String platformVersion = client.getPropertyValue(Property.KEY_CLIENT_PLATFORM_VERSION);
			String hardwareVersion = client.getPropertyValue(Property.KEY_CLIENT_HARDWARE_VERSION);
			String hardware = client.getPropertyValue(Property.KEY_CLIENT_HARDWARE);
			writer.startRow().withKind("client").withId(client.getId());
			writer.cell(client.getName());
			writer.startCell().startWrap().text(client.getUUID()).endWrap().endCell();
			writer.startCell().startWrap().text(secret).endWrap().endCell();
			writer.startCell().text("Version: " + version + " (" + build + ")");
			writer.lineBreak().text("Platform: " + platform + " / " + platformVersion);
			writer.lineBreak().text("Hardware: " + hardware + " / " + hardwareVersion);
			writer.endCell();
			writer.cell(Dates.formatDurationFromNow(client.getCreated()));
			writer.endRow();
		});
		writer.endList();
	}

	@Path
	public UserPerspective loadUser(Request request) throws IOException,EndUserException {
		Long id = request.getLong("id");
		User user = modelService.get(User.class, id, request);
		if (user==null) {
			throw new NotFoundException("User not found (id="+id+")");
		}
		User publicUser = securityService.getPublicUser();
		UserPerspective perspective = new UserPerspective();
		perspective.setUsername(user.getUsername());
		perspective.setName(user.getName());
		EmailAddress usersPrimaryEmail = memberService.getUsersPrimaryEmail(user, request);
		if (usersPrimaryEmail != null) {
			perspective.setEmail(usersPrimaryEmail.getAddress());
		}
		perspective.setPublicView(securityService.canView(user, publicUser, request));
		perspective.setAbilities(Ability.convert(user.getPropertyValues(Property.KEY_ABILITY)));
		return perspective;
	}
	
	@Path
	public void deleteClient(Request request) throws EndUserException {
		Long id = request.getId();
		Client client = modelService.getRequired(Client.class, id, request);
		modelService.delete(client, request);
	}

	@Path
	public void logUserOut(Request request) throws EndUserException {
		Long id = request.getId();
		User user = modelService.getRequired(User.class, id, request);
		List<Client> list = modelService.list(Query.after(Client.class).as(user), request);
		for (Client client : list) {
			modelService.delete(client, request);
		}
	}

	@Path
	public void deleteUser(Request request) throws EndUserException {
		Long id = request.getId();
		User user = modelService.getRequired(User.class, id, request);
		memberService.deleteMember(user, request);
	}
	
	@Path
	public void sendPasswordReset(Request request) throws EndUserException {
		Long id = request.getId();
		User user = modelService.getRequired(User.class, id, request);
		if (!passwordRecoveryService.sendRecoveryMail(user, request)) {
			throw new EndUserException("Unable to send recovery mail");
		}
	}
	
	@Path
	public void sendEmailConfirmation(Request request) throws EndUserException {
		Long id = request.getId();
		User user = modelService.getRequired(User.class, id, request);
		memberService.sendEmailConfirmation(user, request);
	}

	@Path
	public void checkHealth(Request request) throws EndUserException {
		Long id = request.getId();
		User user = modelService.getRequired(User.class, id, request);
		memberService.scheduleHealthCheck(user);
	}

	@Path
	public void saveUser(Request request) throws IOException,EndUserException {
		UserPerspective perspective = request.getObject("user", UserPerspective.class);
		if (perspective==null) {
			throw new BadRequestException("No user provider");
		}
		User user = modelService.get(User.class, perspective.getId(), request);
		if (user==null) {
			throw new NotFoundException("User not found (id="+perspective.getId()+")");
		}
		if (securityService.canChangeUsername(user)) {
			user.setUsername(perspective.getUsername());			
		}
		if (Strings.isNotBlank(perspective.getEmail())) {
			memberService.changePrimaryEmail(user, perspective.getEmail(), request);
		}
		Set<Ability> abilities = perspective.getAbilities();
		user.removeProperties(Property.KEY_ABILITY);
		if (abilities!=null) {
			for (Ability ability : abilities) {
				user.addProperty(Property.KEY_ABILITY, ability.name());
			}
		}
		user.setName(perspective.getName());
		modelService.update(user, request);
		Operator admin = request.as(securityService.getAdminPrivileged());
		if (securityService.isAdminUser(user)) {
			modelService.grantPrivileges(user, user, true, true, false, admin);
			securityService.grantPublicView(user, perspective.isPublicView(), request);
		} else if (securityService.isPublicUser(user)) {
			securityService.makePublicVisible(user, request);
			// TODO: Does it make sense to grant administrator privileges?
			modelService.grantPrivileges(user, securityService.getAdminPrivileged(), true, true, false, admin);
		} else {
			modelService.grantPrivileges(user, user, true, true, true, admin);
			securityService.grantPublicView(user, perspective.isPublicView(), request);
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
			Map<String,Object> data = Mapper.<String,Object>build("group", status.getGroup()).add("name", status.getName()).add("status", status.getTriggerState()).add("running", Boolean.valueOf(status.isRunning())).get();
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
				data.addCell(String.valueOf(info.getAverageRunningTime()/1000000));
				data.addCell(String.valueOf(info.getMaxRunningTime()/1000000));
				data.addCell(String.valueOf(info.getMinRunningTime()/1000000));
				data.addCell(localizationService.formatMilis(info.getTotalRunningTime()/1000000));
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
			SearchResult<LogEntry> result = modelService.search(query, request);
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
					User user = modelService.get(User.class, entry.getSubject(), request);
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
			SearchResult<? extends Entity> result = modelService.search(query, request);
			list.setWindow(result.getTotalCount(), 50, page);
			for (Entity entity : result.getList()) {
				String kind = entity.getClass().getSimpleName().toLowerCase();
				list.newRow(entity.getId(),kind);
				list.addCell(entity.getName(), entity.getIcon());
				list.addCell(entity.getType());
				list.addCell(securityService.canView(entity, publicUser, request));
				list.addCell(securityService.canModify(entity, publicUser, request));
				list.addCell(securityService.canDelete(entity, publicUser, request));
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
		Privilege privilege = securityService.getPrivilege(id,securityService.getPublicUser(), request);
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
		Entity entity = modelService.get(Entity.class, info.getId(), request);
		securityService.grantPublicView(entity, info.isPublicView(), request);
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
		return modelService.list(query, request);
	}

	@Path
	public List<ItemData> getImageTags(Request request) throws EndUserException {
		Map<String, Integer> properties = modelService.getProperties(Property.KEY_COMMON_TAG, Image.class, request);
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
		
		List<InternetAddress> sites = onlinePublisherService.getSites(request);
		
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
		onlinePublisherService.createOrUpdatePublisher(perspective, request);
	}

	@Path
	public PublisherPerspective loadPublisher(Request request) throws IOException,EndUserException {
		return onlinePublisherService.getPublisherPerspective(request.getLong("id"), request);
	}

	@Path
	public void deletePublisher(Request request) throws IOException,EndUserException {
		onlinePublisherService.deletePublisher(request.getLong("id"), request);
	}

	@Path
	public void listInternetAddresses(Request request) throws ModelException, IOException {
		int page = request.getInt("page");
		int size = 50;
		
		Query<InternetAddress> query = Query.after(InternetAddress.class).withPaging(page, 50);
		SearchResult<InternetAddress> result = modelService.search(query, request);
		
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
		InternetAddress address = modelService.get(InternetAddress.class, id, request);
		if (address==null) {
			throw new NotFoundException("The address could not be found, id = "+id);
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
			indexer.getIndexInstances(request).forEach(name -> {
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
			throw new BadRequestException("No index manager width the name '"+desc.getName()+"'");
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
			throw new BadRequestException("No index manager width the name '" + desc.getName() + "'");
		}
		ListWriter writer = new ListWriter(request);
		writer.startList();
		writer.startHeaders();
		writer.header("Property", 30);
		writer.header("Value");
		writer.endHeaders();
		writer.startRow().cell("Index count").cell(manager.getDocumentCount()).endRow();
		writer.startRow().cell("Database count").cell(indexService.getObjectCount(desc, request)).endRow();
		writer.endList();
	}
	
	@Path
	public void createMember(Request request) throws IOException, EndUserException {
		String username = request.getString("username", "No username");
		String password = request.getString("password", "No password");
		String fullName = request.getString("name", "No full name");
		String email = request.getString("email", "No e-mail");
		if (username != null) {
			username = username.toLowerCase();
		}
		memberService.createMember(request, username, password, fullName, email);
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
