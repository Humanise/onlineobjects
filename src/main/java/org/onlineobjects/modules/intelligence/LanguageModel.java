package org.onlineobjects.modules.intelligence;

public class LanguageModel {

	private String provider;
	private String id;
	private String description;

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

	public static LanguageModel of(String provider, String id, String description) {
		var model = new LanguageModel();
		model.provider = provider;
		model.id = id;
		model.description = description;
		return model;
	}
}
