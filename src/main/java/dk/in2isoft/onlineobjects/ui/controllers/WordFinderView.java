package dk.in2isoft.onlineobjects.ui.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.InitializingBean;

import dk.in2isoft.commons.jsf.AbstractView;
import dk.in2isoft.onlineobjects.apps.words.views.util.WordsInterfaceHelper;
import dk.in2isoft.onlineobjects.model.LexicalCategory;
import dk.in2isoft.onlineobjects.ui.Request;
import dk.in2isoft.onlineobjects.ui.jsf.model.Option;
import dk.in2isoft.onlineobjects.util.Messages;

public class WordFinderView extends AbstractView implements InitializingBean {
	
	private WordsInterfaceHelper wordsInterfaceHelper;
	
	private List<Option> categories;
	private List<Option> languages;

	public WordFinderView() {
	}
	
	@Override
	protected void before(Request request) throws Exception {
		Locale locale = request.getLocale();
		{
			Messages msg = new Messages(LexicalCategory.class);
			List<Option> options = new ArrayList<>();
			Option unknown = new Option();
			unknown.setLabel(msg.get("code","none", locale));
			options.add(0, unknown);
			options.addAll(wordsInterfaceHelper.getCategoryOptions(locale));
			categories = options;
		}
		languages = wordsInterfaceHelper.getLanguageOptions(locale);
	}
	
	public List<Option> getCategories() {
		return categories;
	}
	

	public List<Option> getLanguages() {
		return languages; 
	}

	public void setWordsInterfaceHelper(WordsInterfaceHelper wordsInterfaceHelper) {
		this.wordsInterfaceHelper = wordsInterfaceHelper;
	}
}
