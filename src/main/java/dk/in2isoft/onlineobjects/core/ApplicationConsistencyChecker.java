package dk.in2isoft.onlineobjects.core;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import dk.in2isoft.onlineobjects.core.exceptions.ExplodingClusterFuckException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Application;

public class ApplicationConsistencyChecker implements ConsistencyChecker {
	
	private static Logger log = LogManager.getLogger(ApplicationConsistencyChecker.class);

	private ModelService modelService;

	@Override
	public void check() throws ModelException, SecurityException, ExplodingClusterFuckException {
		Operator operator = modelService.newAdminOperator();
		Query<Application> query = Query.of(Application.class);
		List<Application> apps = modelService.list(query, operator);
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
			modelService.create(setup, operator);
			log.info("Created setup application");
		}
		operator.commit();
	}

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
	
}
