package dk.in2isoft.onlineobjects.services;

import java.util.List;

import dk.in2isoft.onlineobjects.core.ConsistencyChecker;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;

public class ConsistencyService {
	
	private List<ConsistencyChecker> consistencyCheckers;
	private ConfigurationService configurationService;

	public void check() throws EndUserException {
		if (configurationService.isDevelopmentMode()) {
			//return;
		}
		for (ConsistencyChecker consistencyChecker : consistencyCheckers) {
			consistencyChecker.check();
		}
	}
	
	public void setConsistencyCheckers(List<ConsistencyChecker> consistencyCheckers) {
		this.consistencyCheckers = consistencyCheckers;
	}
	
	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}
}
