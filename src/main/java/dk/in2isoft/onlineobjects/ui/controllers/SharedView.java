package dk.in2isoft.onlineobjects.ui.controllers;

import dk.in2isoft.commons.jsf.AbstractView;
import dk.in2isoft.onlineobjects.ui.Request;

public class SharedView extends AbstractView {

	private String language;

	@Override
	protected void before(Request request) throws Exception {
		language = request.getLanguage();
	}

	public String getLanguage() {
		return language;
	}
}
