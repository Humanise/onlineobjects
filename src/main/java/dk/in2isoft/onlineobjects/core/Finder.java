package dk.in2isoft.onlineobjects.core;

public class Finder {

	private ModelService modelService;
	private SecurityService securityService;

	public RelationQuery relations(Operator operator) {
		return new RelationQuery(modelService, securityService, operator.getOperation()).as(operator);
	}

	// Wiring...

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
}