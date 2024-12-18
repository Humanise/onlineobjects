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

public class CoreFilesResponder implements Responder {

	private static Logger log = LogManager.getLogger(CoreFilesResponder.class);
	
	private ConfigurationService configurationService;
	
	public boolean applies(Request request) {
		String[] path = request.getFullPath();
		return path.length > 0 && path[0].equals("core");
	}
	
	public void dispatch(Request request, FilterChain chain) throws IOException {
		String[] path = request.getFullPath();

		request.getResponse().addHeader("Access-Control-Allow-Origin", "*");
		String[] filePath = new String[] { "core", "web" };
		pushCoreFile((String[]) ArrayUtils.addAll(filePath, ArrayUtils.subarray(path, 1, path.length)),request.getResponse());
	}
	
	private boolean pushCoreFile(String[] path, HttpServletResponse response) {
		boolean success = false;
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
				success = true;
			} catch (Exception e) {
				log.error(e.toString(), e);
			}
		}
		return success;
	}
	
	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}
}
