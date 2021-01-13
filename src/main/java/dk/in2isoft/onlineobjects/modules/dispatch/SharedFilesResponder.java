package dk.in2isoft.onlineobjects.modules.dispatch;

import java.io.File;
import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dk.in2isoft.onlineobjects.services.ConfigurationService;
import dk.in2isoft.onlineobjects.services.DispatchingService;
import dk.in2isoft.onlineobjects.ui.Request;

public class SharedFilesResponder implements Responder {

	private static Logger log = LogManager.getLogger(SharedFilesResponder.class);
	
	private ConfigurationService configurationService;
	
	public boolean applies(Request request) {
		return request.testLocalPathStart("favicon.ico");
	}
	
	public void dispatch(Request request, FilterChain chain) throws IOException {
		String[] filePath = new String[] { "shared", "web" };
		String[] full = (String[]) ArrayUtils.addAll(filePath, request.getLocalPath());
		push(full,request.getResponse());
	}
	
	private void push(String[] path, HttpServletResponse response) {
		StringBuilder filePath = new StringBuilder();
		filePath.append(configurationService.getBasePath());
		filePath.append(File.separator);
		filePath.append("WEB-INF");
		for (int i = 0; i < path.length; i++) {
			filePath.append(File.separator);
			filePath.append(path[i]);
		}
		File file = new File(filePath.toString());
		if (file.exists()) {
			try {
				DispatchingService.pushFile(response, file);
			} catch (Exception e) {
				log.error(e.toString(), e);
			}
		}
	}
	
	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}
}
