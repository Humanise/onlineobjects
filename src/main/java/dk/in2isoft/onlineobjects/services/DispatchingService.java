package dk.in2isoft.onlineobjects.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import dk.in2isoft.commons.xml.XSLTUtil;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.core.exceptions.ContentNotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.IllegalRequestException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.modules.dispatch.Responder;
import dk.in2isoft.onlineobjects.modules.surveillance.SurveillanceService;
import dk.in2isoft.onlineobjects.ui.ErrorRenderer;
import dk.in2isoft.onlineobjects.ui.Request;
import dk.in2isoft.onlineobjects.util.Messages;

public class DispatchingService {

	private static Logger log = LogManager.getLogger(DispatchingService.class);
	
	private ModelService modelService;
	private SecurityService securityService;
	private SurveillanceService surveillanceService;
	private ConfigurationService configurationService;
	private ConversionService conversionService;

	private List<Responder> responders;
		
	public boolean doFilter(HttpServletRequest servletRequest, HttpServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {

		if (configurationService.isSimulateSlowRequest()) {
			try {
				Thread.sleep(Math.round(Math.random()*1000+1000));
			} catch (InterruptedException ignore) {}
		}
		if (configurationService.isSimulateSporadicServerError()) {
			if (Math.random() > 0.5) {
				servletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return true;
				
			}
		}
		
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		boolean handled = false;
		Request request = Request.get(servletRequest, servletResponse);
		request.setOperationProvider(modelService);
		Boolean shouldCommit = null;		
		
		try {
			securityService.ensureUserSession(request);
		} catch (SecurityException ex) {
			displayError(request, ex);
			return true;
		}
		
		for (Responder responder : responders) {
			if (!handled && responder.applies(request)) {
				handled = true;
				try {
					shouldCommit = responder.dispatch(request, chain);
				} catch (EndUserException e) {
					shouldCommit = false;
					surveillanceService.survey(e,request);
					displayError(request, e);
				}
			}
		}
		if (shouldCommit!=null) {
			if (shouldCommit) {
				request.commit();
			} else {
				request.rollBack();
			}
		}
		stopWatch.stop();
		surveillanceService.survey(request);
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
		
		return handled;
	}


	public static void pushFile(HttpServletResponse response, File file) throws IOException {

		HeaderUtil.setModified(file, response);
		HeaderUtil.setOneWeekCache(response);
		String mimeType = HeaderUtil.getMimeType(file);
		response.setContentLength((int) file.length());
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
		ErrorRenderer renderer = new ErrorRenderer(ex,request,configurationService, conversionService);
		try {
			if (ex instanceof ContentNotFoundException) {
				logError(request, ex);
				surveillanceService.surveyNotFound(request);
			} else {
				logError(request, ex);
			}
			HttpServletResponse response = request.getResponse();
			if (ex instanceof SecurityException) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				renderer.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			} else if (ex instanceof ContentNotFoundException) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				renderer.setStatus(HttpServletResponse.SC_NOT_FOUND);
			} else if (ex instanceof IllegalRequestException) {
				response.addHeader("Reason", ((IllegalRequestException) ex).getCode());
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				renderer.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			} else {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				renderer.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
				XSLTUtil.applyXSLT(renderer, request);
			}
		} catch (EndUserException e) {
			logError(request, e);
		} catch (IOException e) {
			logError(request, e);
		}
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
	
	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}
}
