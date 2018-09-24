package dk.in2isoft.onlineobjects.modules.surveillance;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.ui.Request;

public class SurveillanceService {
	
	private RequestList longestRunningRequests;
	private ConcurrentLinkedQueue<String> exceptions;
	private ConcurrentLinkedQueue<LiveLogEntry> logEntries;
	private RequestList requestsNotFound;
	private final Logger auditLog = LogManager.getLogger("audit");

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
}
