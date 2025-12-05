package dk.in2isoft.onlineobjects.apps.words.views;

import java.util.List;
import java.util.Locale;

import dk.in2isoft.commons.jsf.AbstractView;
import dk.in2isoft.onlineobjects.apps.words.views.util.WordsInterfaceHelper;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.modules.language.WordListPerspective;
import dk.in2isoft.onlineobjects.ui.Request;
import dk.in2isoft.onlineobjects.ui.jsf.model.Option;

public class WordsFrontView extends AbstractView {

	private List<WordListPerspective> latestWords;
	private List<Option> alphabeth;
	private List<Option> languages;
	private List<Option> categories;

	private WordsInterfaceHelper wordsInterfaceHelper;

	@Override
	protected void before(Request request) throws Exception {
		Locale locale = request.getLocale();
		alphabeth = wordsInterfaceHelper.getLetterOptions(locale);
		languages = wordsInterfaceHelper.getLanguageOptions(locale);
		categories = wordsInterfaceHelper.getCategoryOptions(locale);
	}

	public List<WordListPerspective> getLatestWords() throws ModelException {
		return latestWords;
	}

	public List<Option> getAlphabeth() {
		return alphabeth;
	}

	public List<Option> getLanguages() {
		return languages;
	}

	public List<Option> getCategories() {
		return categories;
	}

	// Wiring...

	public void setWordsInterfaceHelper(WordsInterfaceHelper wordsInterfaceHelper) {
		this.wordsInterfaceHelper = wordsInterfaceHelper;
	}

}
