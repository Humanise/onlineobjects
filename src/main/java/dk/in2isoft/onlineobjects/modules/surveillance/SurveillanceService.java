package dk.in2isoft.onlineobjects.modules.surveillance;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.model.LogEntry;
import dk.in2isoft.onlineobjects.model.LogLevel;
import dk.in2isoft.onlineobjects.model.LogType;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.services.ConfigurationService;
import dk.in2isoft.onlineobjects.services.EmailService;
import dk.in2isoft.onlineobjects.ui.Request;

public class SurveillanceService {
	
	private RequestList longestRunningRequests;
	private ConcurrentLinkedQueue<String> exceptions;
	private ConcurrentLinkedQueue<LiveLogEntry> logEntries;
	private RequestList requestsNotFound;
	private final Logger auditLog = LogManager.getLogger("audit");
	private final Logger requestLog = LogManager.getLogger("requests");
	private ModelService modelService;
	private EmailService emailService;
	private ConfigurationService configurationService;

	public SurveillanceService() {
		longestRunningRequests = new RequestList();
		requestsNotFound = new RequestList();
		exceptions = new ConcurrentLinkedQueue<String>();
		logEntries = new ConcurrentLinkedQueue<LiveLogEntry>();
	}
	
	public void sendReport() throws EndUserException {
		String body = getReportBody();
		sendMailToMonitors("OnlineObjects report", body);
	}

	private void sendMailToMonitors(String subject, String body) throws EndUserException {
		String[] mails = getMontorMails();
		for (String mail : mails) {
			emailService.sendMessage(subject, body, mail);				
		}
	}

	private String getReportBody() throws ModelException {
		Operator operator = modelService.newAdminOperator();
		StringBuilder body = new StringBuilder();
		LogQuery query = new LogQuery().withSize(100);
		List<LogEntry> list = modelService.list(query, operator);
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.ENGLISH);
		for (LogEntry entry : list) {
			body.append(format.format(entry.getTime()));
			body.append(" | ");
			body.append(StringUtils.rightPad(entry.getLevel().toString(), 8));
			body.append(" | ");
			String username = "-";
			if (entry.getSubject() != null) {
				@Nullable
				User user = modelService.get(User.class, entry.getSubject(), operator);
				if (user!=null) {
					username = user.getUsername();
				} else {
					username = "?: " + entry.getSubject();
				}
			}
			body.append(StringUtils.rightPad(username, 20));
			body.append(" | ");
			body.append(entry.getType());
			body.append("\n");
		}
		operator.commit();
		return body.toString();
	}

	private String[] getMontorMails() {
		String string = configurationService.getMonitoringMails();
		if (Strings.isNotBlank(string)) {
			return string.split("[;, ]+");
		}
		return new String[]{};
	}
	
	public void logInfo(String title,String details) {
		LiveLogEntry entry = new LiveLogEntry();
		entry.setTitle(title);
		entry.setDetails(details);
		logEntries.add(entry);
		if (logEntries.size()>60) {
			logEntries.poll();
		}
	}
	
	public void log(Privileged user, LogType type) {
		Operator operator = modelService.newAdminOperator();
		LogEntry entry = new LogEntry();
		entry.setSubject(user.getIdentity());
		entry.setTime(new Date());
		entry.setType(type);
		entry.setLevel(LogLevel.info);
		modelService.create(entry, operator);
		operator.commit();
	}

	public void log(LogType startup) {
		Operator operator = modelService.newAdminOperator();
		LogEntry entry = new LogEntry();
		entry.setLevel(LogLevel.info);
		entry.setTime(new Date());
		entry.setType(LogType.startUp);
		modelService.create(entry, operator);
		operator.commit();
	}

	public void logSignUp(User user) {
		audit().info("New member created with username={}", user.getUsername());
		log(user, LogType.signUp);
	}

	public void survey(Request request) {
		if (!request.getRequest().getRequestURI().startsWith("/service/image")) {
			this.longestRunningRequests.register(request);
		}
		Map<String,Object> entry = new HashMap<>();
		entry.put("time", System.currentTimeMillis());
		entry.put("domain", request.getDomainName());
		entry.put("app", request.getApplication()==null ? "none" : request.getApplication());
		entry.put("duration", request.getRunningTime());
		entry.put("agent", request.getRequest().getHeader("User-Agent"));
		entry.put("path", request.getRequest().getRequestURI());
		requestLog.info(Strings.toJSON(entry));
	}
	
	public void surveyNotFound(Request request) {
		this.requestsNotFound.register(request);
	}
	
	public void survey(Exception e, Request request) {
		Throwable known = getKnownException(e);
		String trace = ExceptionUtils.getFullStackTrace(known);
		trace = request.getRequest().getRequestURI()+"\n"+trace;
		exceptions.add(trace);
		if (exceptions.size()>60) {
			exceptions.poll();
		}
	}
	
	private Throwable getKnownException(Throwable root) {
		Throwable cause = root;
		while (cause!=null) {
			if (cause instanceof EndUserException) {
				return cause;
			}
			cause = cause.getCause();
		}
		
		return root;
	}
	
	public ImmutableList<RequestInfo> getLongestRunningRequests() {
		return ImmutableList.copyOf(longestRunningRequests.getSet());
	}
	
	public Collection<String> getLatestExceptions() {
		return exceptions;
	}
	
	public List<LiveLogEntry> getLogEntries() {
		return Lists.newArrayList(logEntries);
	}

	public void audit(String msg) {
		auditLog.info(msg);
	}

	public Logger audit() {
		return auditLog;
	}
	
	// Wiring...
	
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}
	
	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}
	
}
