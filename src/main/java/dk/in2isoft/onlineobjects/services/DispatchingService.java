package dk.in2isoft.onlineobjects.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StopWatch;

import dk.in2isoft.commons.http.HeaderUtil;
import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.commons.util.StackTraceUtil;
import dk.in2isoft.commons.xml.HTML;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.NotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.modules.dispatch.Responder;
import dk.in2isoft.onlineobjects.modules.surveillance.SurveillanceService;
import dk.in2isoft.onlineobjects.ui.Request;
import dk.in2isoft.onlineobjects.util.Messages;

public class DispatchingService {

	private static Logger log = LogManager.getLogger(DispatchingService.class);
	
	private ModelService modelService;
	private SecurityService securityService;
	private SurveillanceService surveillanceService;
	private ConfigurationService configurationService;

	private List<Responder> responders;
		
	public boolean doFilter(HttpServletRequest servletRequest, HttpServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {

		if (configurationService.isSimulateSlowRequest()) {
			try {
				Thread.sleep(Math.round(Math.random()*1000+1000));
			} catch (InterruptedException ignore) {}
		}
		if (shouldSimulateError(servletRequest)) {
			servletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return true;			
		}		
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		boolean handled = false;
		Request request = Request.get(servletRequest, servletResponse);
		request.setOperationProvider(modelService);
		Boolean shouldRollback = false;		
		
		try {
			securityService.ensureUserSession(request);
		} catch (SecurityException ex) {
			request.commit();
			displayError(request, ex);
			return true;
		}
		
		for (Responder responder : responders) {
			if (!handled && responder.applies(request)) {
				handled = true;
				try {
					responder.dispatch(request, chain);
				} catch (EndUserException e) {
					shouldRollback = true;
					surveillanceService.survey(e,request);
					displayError(request, e);
				}
			}
		}
		if (shouldRollback) {
			request.rollBack();
		} else {
			request.commit();
		}
		stopWatch.stop();
		surveillanceService.survey(request);
		checkSessions(request);
		return handled;
	}


	private boolean shouldSimulateError(HttpServletRequest servletRequest) {
		if (configurationService.isSimulateSporadicServerError()) {
			if (!isDeveloperApp(servletRequest)) {
				if (Math.random() > 0.5) {
					return true;
				}
			}
		}
		return false;
	}


	private boolean isDeveloperApp(HttpServletRequest servletRequest) {
		String serverName = servletRequest.getServerName();
		return (serverName != null && serverName.startsWith("developer."));
	}


	private void checkSessions(Request request) {
		if (configurationService.isDevelopmentMode()) {
			if (securityService.isPublicUser(request)) {
				HttpSession session = request.getRequest().getSession(false);
				if (session != null) {
					if (session.isNew()) {
						log.warn("Public user started session");
					} else {
						log.warn("Public user has session");
					}
				}
			}
		}
	}


	public static void pushFile(HttpServletResponse response, File file) throws IOException {

		HeaderUtil.setModified(file, response);
		HeaderUtil.setOneWeekCache(response);
		String mimeType = HeaderUtil.getMimeType(file);
		response.setContentLength((int) file.length());
		if ("text/javascript".equals(mimeType) || "text/css".equals(mimeType)) {
			response.setCharacterEncoding(Strings.UTF8);
		}
		try {
			ServletOutputStream out = response.getOutputStream();
			if (mimeType != null) {
				response.setContentType(mimeType);
			}
			try (FileInputStream in = new FileInputStream(file)) {
				IOUtils.copy(in, out);
			}
		} catch (FileNotFoundException e) {
			throw new IOException("File: " + file.getPath() + " not found!");
		}
	}

	public void displayError(Request request, Exception ex) {
		ex = EndUserException.findUserException(ex);
		try {
			if (ex instanceof NotFoundException) {
				surveillanceService.surveyNotFound(request);
			}
			logError(request, ex);
			HttpServletResponse response = request.getResponse();
			int statusCode = getStatusCode(ex);
			response.setStatus(statusCode);
			if (ex instanceof EndUserException) {
				String code = ((EndUserException) ex).getCode();
				if (Strings.isNotBlank(code)) {
					response.addHeader("Reason", code);
				}
			}
			String accept = request.getRequest().getHeader("Accept");
			if (accept != null && accept.contains("application/json") && !accept.contains("text/html")) {
				response.setContentType("application/json");
				response.setCharacterEncoding(Strings.UTF8);
				Map<String, String> resp = new HashMap<>();
				if (ex instanceof EndUserException) {
					String code = ((EndUserException) ex).getCode();
					if (code!=null) {
						Locale locale = request.getRequest().getLocale();
						if (locale == null || locale.getLanguage()!="en" && locale.getLanguage()!="da") {
							locale = new Locale("en");
						}
						Messages msg = new Messages(EndUserException.class);
						resp.put("code", code);
						resp.put("message", msg.get(code, locale));
					} else {
						resp.put("message", ex.getMessage());
					}
				} else {
					resp.put("message", ex.getMessage());
				}
				response.getWriter().write(Strings.toJSON(resp));
			} else {
				renderError(ex, statusCode, request);
			}
		} catch (IOException e) {
			logError(request, e);
		}
	}


	private int getStatusCode(Exception ex) {
		if (ex instanceof EndUserException) {
			return ((EndUserException) ex).getHttpStatusCode();
		}
		return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
	}


	private void renderError(Exception ex, int statusCode, Request request) throws IOException {
		HttpServletResponse response = request.getResponse();
		response.setContentType("text/html");
		response.setCharacterEncoding(Strings.UTF8);
		PrintWriter out = response.getWriter();
		String msg = ex.getMessage();
		out.print("<!DOCTYPE html>"
				+ "<html>"
				+ "<head>"
				+ "<title>" + HTML.escape(msg) + "</title>"
				+ "<link rel=\"stylesheet\" href=\"/core/css/error.css\" type=\"text/css\" media=\"screen\" title=\"front\" charset=\"utf-8\" />"
				+ "<link href=\"https://fonts.googleapis.com/css2?family=Inter:wght@400;600&display=swap\" rel=\"stylesheet\">"
				+ "</head>"
				+ "<body class=\"oo_body oo_body-light	\">"
				+ "<div class=\"error\">"
				+ "<h1 class=\"error_title\">" + HTML.escape(msg) + "</h1>"
				+ "<p class=\"error_status\" onclick=\"document.getElementById('trace').style.display='block'\">" + statusCode + "</p>");
		if (configurationService.isDevelopmentMode()) {
			out.print("<textarea class=\"error_trace\" id=\"trace\">"
					+ HTML.escape(StackTraceUtil.getStackTrace(ex))
					+ "</textarea>");
		}
		out.print("</div>"
				+ "</body>"
				+ "</html>");
		;
	}


	private void logError(Request request, Exception ex) {
		HttpServletRequest httpServletRequest = request.getRequest();
		String query = httpServletRequest.getQueryString();
		String url = httpServletRequest.getRequestURL().toString() + (query == null ? "" : "?" + query);
		log.error(ex.getMessage() + " - " + url, ex);
	}
	
	// Wiring...
	
	public void setResponders(List<Responder> responders) {
		this.responders = responders;
	}
	
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
		
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
	
	public void setSurveillanceService(SurveillanceService surveillanceService) {
		this.surveillanceService = surveillanceService;
	}
	
	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}
}
