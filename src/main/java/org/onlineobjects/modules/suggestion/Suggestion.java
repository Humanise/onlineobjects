package org.onlineobjects.modules.suggestion;

import java.util.Map;

import dk.in2isoft.onlineobjects.ui.data.SimpleEntityPerspective;

public class Suggestion {

	private String description;
	private SimpleEntityPerspective target;
	private SimpleEntityPerspective entity;
	private String action;
	private Map<String, Object> data;
	private Double strength;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public SimpleEntityPerspective getTarget() {
		return target;
	}

	public void setTarget(SimpleEntityPerspective target) {
		this.target = target;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	public Double getStrength() {
		return strength;
	}

	public void setStrength(Double strength) {
		this.strength = strength;
	}

	public SimpleEntityPerspective getEntity() {
		return entity;
	}

	public void setEntity(SimpleEntityPerspective entity) {
		this.entity = entity;
	}
}
