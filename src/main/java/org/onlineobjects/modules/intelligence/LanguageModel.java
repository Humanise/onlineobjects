package org.onlineobjects.modules.intelligence;

import java.util.HashMap;
import java.util.Map;

public class LanguageModel {

	private String provider;
	private String id;
	private String description;
	private Map<String,String> parameters = new HashMap<>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public LanguageModel withParameter(String key, String value) {
		parameters.put(key, value);
		return this;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public static LanguageModel of(String provider, String id, String description) {
		var model = new LanguageModel();
		model.provider = provider;
		model.id = id;
		model.description = description;
		return model;
	}
}
