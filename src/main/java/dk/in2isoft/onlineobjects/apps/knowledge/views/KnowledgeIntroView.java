package dk.in2isoft.onlineobjects.apps.knowledge.views;

import java.util.Locale;

import dk.in2isoft.commons.jsf.AbstractView;
import dk.in2isoft.onlineobjects.services.ConfigurationService;
import dk.in2isoft.onlineobjects.ui.Request;

public class KnowledgeIntroView extends AbstractView {

	private Locale locale;
	private ConfigurationService configurationService;
	private String macIntroUrl;

	@Override
	protected void before(Request request) throws Exception {
		locale = request.getLocale();
		macIntroUrl = configurationService.getApplicationContext("front", "mac", request);
	}

	public String getMacIntroUrl() {
		return macIntroUrl;
	}

	public Locale getLocale() {
		return this.locale;
	}

	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}
}
