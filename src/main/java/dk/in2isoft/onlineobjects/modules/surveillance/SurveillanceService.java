package dk.in2isoft.onlineobjects.modules.surveillance;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.LogEntry;
import dk.in2isoft.onlineobjects.model.LogLevel;
import dk.in2isoft.onlineobjects.model.LogType;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.ui.Request;

public class SurveillanceService {
	
	private RequestList longestRunningRequests;
	private ConcurrentLinkedQueue<String> exceptions;
	private ConcurrentLinkedQueue<LiveLogEntry> logEntries;
	private RequestList requestsNotFound;
	private final Logger auditLog = LogManager.getLogger("audit");
	private ModelService modelService;

	public SurveillanceService() {
		longestRunningRequests = new RequestList();
		requestsNotFound = new RequestList();
		exceptions = new ConcurrentLinkedQueue<String>();
		logEntries = new ConcurrentLinkedQueue<LiveLogEntry>();
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
		LogEntry entry = new LogEntry();
		entry.setSubject(user.getIdentity());
		entry.setTime(new Date());
		entry.setType(type);
		entry.setLevel(LogLevel.info);
		modelService.create(entry);
	}

	public void log(LogType startup) {
		Session session = modelService.newSession();
		LogEntry entry = new LogEntry();
		entry.setLevel(LogLevel.info);
		entry.setTime(new Date());
		entry.setType(LogType.startUp);
		modelService.create(entry, session);
		modelService.commit(session);
	}

	public void logSignUp(User user) {
		audit().info("New member created with username={}", user.getUsername());
		log(user, LogType.signUp);
	}

	public void survey(Request request) {
		if (!request.getRequest().getRequestURI().startsWith("/service/image")) {
			this.longestRunningRequests.register(request);
		}
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


}
