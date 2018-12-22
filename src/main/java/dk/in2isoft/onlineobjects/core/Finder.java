package dk.in2isoft.onlineobjects.core;

public class Finder {
	
	private ModelService modelService;
	private SecurityService securityService;

	public RelationQuery relations(Privileged privileged) {
		return new RelationQuery(modelService, securityService).as(privileged);
	}
	
	// Wiring...
	
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
	
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
}