package dk.in2isoft.onlineobjects.core;

import java.util.List;

import org.apache.log4j.Logger;

import dk.in2isoft.onlineobjects.core.exceptions.ExplodingClusterFuckException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Application;

public class ApplicationConsistencyChecker implements ConsistencyChecker {
	
	private static Logger log = Logger.getLogger(ApplicationConsistencyChecker.class);

	private ModelService modelService;
	private SecurityService securityService;

	@Override
	public void check() throws ModelException, SecurityException, ExplodingClusterFuckException {

		Query<Application> query = Query.of(Application.class);
		List<Application> apps = modelService.list(query);
		boolean found = false;
		for (Application application : apps) {
			if ("setup".equals(application.getName())) {
				found = true;
			}
		}
		if (!found) {
			log.warn("No setup application present!");
			Application setup = new Application();
			setup.setName("setup");
			modelService.createItem(setup, securityService.getAdminPrivileged());
			modelService.commit();
			log.info("Created setup application");
		}
		modelService.commit();
	}

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
	
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
}
