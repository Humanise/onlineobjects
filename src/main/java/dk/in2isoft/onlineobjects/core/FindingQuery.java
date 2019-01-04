package dk.in2isoft.onlineobjects.core;

import java.util.List;

public class FindingQuery<T> extends Query<T> {

	private ModelService modelService;
	
	public FindingQuery(Class<T> clazz, Privileged privileged, ModelService modelService) {
		super(clazz);
		this.modelService = modelService;
		as(privileged);
	}

	public List<T> list() {
		return modelService.list(this);
	}
}
