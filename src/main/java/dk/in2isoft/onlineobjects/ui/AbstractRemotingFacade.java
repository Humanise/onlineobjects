package dk.in2isoft.onlineobjects.ui;

import dk.in2isoft.onlineobjects.core.ModelService;

public abstract class AbstractRemotingFacade {

	protected ModelService modelService;

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	public ModelService getModelService() {
		return modelService;
	}
}
