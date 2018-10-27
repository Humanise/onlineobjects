package dk.in2isoft.onlineobjects.apps.knowledge.views;

import java.util.Locale;

import dk.in2isoft.commons.jsf.AbstractView;
import dk.in2isoft.onlineobjects.ui.Request;

public class KnowledgeIntroView extends AbstractView {

	private Locale locale;
	
	@Override
	protected void before(Request request) throws Exception {
		locale = request.getLocale();
	}
	
	public Locale getLocale() {
		return this.locale;
	}
}
