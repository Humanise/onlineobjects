package dk.in2isoft.onlineobjects.modules.dispatch;

import java.io.File;
import java.io.IOException;

import javax.servlet.FilterChain;

import org.springframework.beans.factory.InitializingBean;

import dk.in2isoft.commons.http.HeaderUtil;
import dk.in2isoft.onlineobjects.services.ConfigurationService;
import dk.in2isoft.onlineobjects.services.DispatchingService;
import dk.in2isoft.onlineobjects.ui.Request;

public class In2iGuiResponder implements Responder, InitializingBean {

	//private static Logger log = LogManager.getLogger(In2iGuiResponder.class);
	private ConfigurationService configurationService;
	private String huiPath;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		this.huiPath = configurationService.getFile("hui").getPath();
	}
	
	public boolean applies(Request request) {
		String[] path = request.getFullPath();
		return path.length > 0 && path[0].equals("hui");
	}
	
	public void dispatch(Request request, FilterChain chain) throws IOException {

		String[] path = request.getFullPath();
		StringBuilder file = new StringBuilder();
		file.append(huiPath);
		for (int i = 1; i < path.length; i++) {
			file.append(File.separatorChar);
			file.append(path[i]);
		}
		File fileObj = new File(file.toString());
		if (fileObj.exists()) {
			DispatchingService.pushFile(request.getResponse(), fileObj);
		} else {
			HeaderUtil.setNotFound(request.getResponse());
		}
	}
	
	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}
}
