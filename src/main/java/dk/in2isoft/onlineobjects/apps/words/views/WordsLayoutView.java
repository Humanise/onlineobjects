package dk.in2isoft.onlineobjects.apps.words.views;

import java.util.List;

import dk.in2isoft.commons.jsf.AbstractView;
import dk.in2isoft.onlineobjects.apps.words.views.util.WordsInterfaceHelper;
import dk.in2isoft.onlineobjects.core.Ability;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.ui.Request;
import dk.in2isoft.onlineobjects.ui.jsf.model.Option;

public class WordsLayoutView extends AbstractView {

	private WordsInterfaceHelper wordsInterfaceHelper;
	private SecurityService securityService;
	private boolean front;
	private String selectedMenuItem;
	private String language;
	private List<Option> alphabeth;
	private boolean canModify;
	private boolean loggedIn;
	
	@Override
	protected void before(Request request) throws Exception {
		front = request.getLocalPath().length < 2;
		selectedMenuItem = selectedMenuItem(request);
		language = request.getLanguage();
		alphabeth = wordsInterfaceHelper.getLetterOptions(request.getLocale());
		canModify = request.getSession().has(Ability.modifyWords);
		loggedIn = !securityService.isPublicUser(request.getSession());
	}
		
	private String selectedMenuItem(Request request) {
		String[] path = request.getLocalPath();
		if (path.length==0 || path.length==1) {
			return "front";
		}
		if (path.length>=2 && path[1].equals("search")) {
			return "search";
		}
		if (path.length>=2 && path[1].equals("statistics")) {
			return "statistics";
		}
		if (path.length>=2 && path[1].equals("index")) {
			return "index";
		}
		if (path.length>=2 && path[1].equals("about")) {
			return "about";
		}
		return null;
	}

	public boolean isFront() {
		return front;
	}
	
	public String getSelectedMenuItem() {
		return selectedMenuItem;
	}
	
	
	public List<Option> getAlphabeth() {
		return alphabeth;
	}

	public boolean isCanModify() {
		return canModify;
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}

	public String getLanguage() {
		return language;
	}

	// Wiring...
	
	public void setWordsInterfaceHelper(WordsInterfaceHelper wordsInterfaceHelper) {
		this.wordsInterfaceHelper = wordsInterfaceHelper;
	}
	
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
}
