package dk.in2isoft.onlineobjects.apps.words.views;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import dk.in2isoft.commons.jsf.AbstractView;
import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.apps.words.LoadManager;
import dk.in2isoft.onlineobjects.apps.words.views.util.UrlBuilder;
import dk.in2isoft.onlineobjects.apps.words.views.util.WordsInterfaceHelper;
import dk.in2isoft.onlineobjects.core.SearchResult;
import dk.in2isoft.onlineobjects.core.exceptions.BadRequestException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.modules.language.WordListPerspective;
import dk.in2isoft.onlineobjects.modules.language.WordQuery;
import dk.in2isoft.onlineobjects.modules.language.WordService;
import dk.in2isoft.onlineobjects.ui.Request;
import dk.in2isoft.onlineobjects.ui.jsf.model.Filters;
import dk.in2isoft.onlineobjects.ui.jsf.model.Filters.Filter;
import dk.in2isoft.onlineobjects.ui.jsf.model.Filters.Variant;
import dk.in2isoft.onlineobjects.ui.jsf.model.Option;
import dk.in2isoft.onlineobjects.util.Messages;

public class WordsSearchView extends AbstractView {

	private static final int PAGING = 10;
	private WordService wordService;
	private WordsInterfaceHelper wordsInterfaceHelper;
	private LoadManager loadManager;

	private static final Logger log = LogManager.getLogger(WordsSearchView.class);

	private List<WordListPerspective> list;
	private int count;
	private int page;

	private String title;

	private String text;
	private String letter;
	private String language;
	private String category;
	private String source;
	private String state;

	private int pageSize = 20;
	private List<Option> pages;

	private String nextPage;
	private String previousPage;

	private String effectiveQuery;
	private Messages wordsMsg;
	private Messages languageMsg;
	private Messages categoryMsg;
	private String description;

	private List<Option> suggestions;
	private Filters filters;

	@Override
	protected void before(Request request) throws Exception {
		checkRequest(request);
		loadManager.failIfBusy();
		text = request.getString("text");
		letter = request.getString("letter");
		language = request.getString("language");
		category = request.getString("category");
		source = request.getString("source");
		state = request.getString("state");

		String[] localPath = request.getLocalPath();
		page = 0;
		if (localPath.length>2) {
			page = Math.max(0, NumberUtils.toInt(localPath[2])-1);
		}

		wordsMsg = wordsInterfaceHelper.getWordsMessages();
		languageMsg = wordsInterfaceHelper.getLanguageMessages();
		categoryMsg = wordsInterfaceHelper.getCategoryMessages();

		WordQuery query = new WordQuery().withText(text).withLetter(letter).withCategory(category).withLanguage(language).withSource(source).withPage(page).withPageSize(20);
		if ("validated".equals(state)) {
			query.setSourceDefined(true);
		}
		query.cached(request.getBoolean("cached"));
		SearchResult<WordListPerspective> result = wordService.search(query, request);

		list = result.getList();
		count = result.getTotalCount();

		highlight();

		effectiveQuery = result.getDescription();

		title = buildTitle(request);
		description = title;
		pages = buildPages(request);
		suggestions = buildSuggestions(request);
		filters = buildFilters(request);
	}

	private void checkRequest(Request request) throws BadRequestException {
		String ua = request.getUserAgent();
		if (ua != null && (ua.contains("bingbot") || ua.contains("Googlebot"))) {
			if (Strings.isNotBlank(request.getString("text"))) {
				throw new BadRequestException("Bingbot and Googlebot cannot search by text since they have abused it");
			}
		}
	}

	private List<Option> buildSuggestions(Request request) {
		List<Option> suggestions = Lists.newArrayList();
		Locale locale = request.getLocale();
		String textLetter = text.toLowerCase(locale);
		if (wordsInterfaceHelper.isLetter(textLetter)) {

			Option option = new Option();
			option.setValue(buildUrl(request, "", language, category, textLetter, state));
			option.setLabel(wordsMsg.get("search_for_words_starting_with", locale) + " \"" + textLetter + "\"");
			suggestions.add(option);
		}
		Iterable<String> languages = getLanguages(request);
		for (String langCode : languages) {
			if (!langCode.equals(this.language) && wordsInterfaceHelper.isKnownLanguage(langCode)) {
				Option option = new Option();
				option.setValue(buildUrl(request, text, langCode, category, letter, state));
				option.setLabel(wordsMsg.get("search_for_words_in", locale) + " " + languageMsg.get("code",langCode, locale).toLowerCase(locale));
				suggestions.add(option);
			}
		}
		if (!"validated".equals(state)) {
			Option option = new Option();
			option.setValue(buildUrl(request, text, language, category, letter, "validated"));
			option.setLabel(wordsMsg.get("search_for_validated_words", locale));
			suggestions.add(option);
		}
		return suggestions;
	}

	private LinkedHashSet<String> getLanguages(Request request) {
		LinkedHashSet<String> languages = Sets.newLinkedHashSet();
		String acceptLanguage = request.getRequest().getHeader("Accept-Language");
		if (Strings.isNotBlank(acceptLanguage)) {
			String[] parts = acceptLanguage.split(",");
			for (String part : parts) {
				Pattern patter = Pattern.compile("([a-z]+)(-([A-Za-z]+))?;?(q=[0-9]\\.[0-9])?");
				Matcher matcher = patter.matcher(part);
				if (matcher.matches()) {
					String language = matcher.group(1);
					languages.add(language);
				}
			}

		}
		return languages;
	}

	public List<Option> getSuggestions() {
		return suggestions;
	}

	private void highlight() {
		String[] words = Strings.getWords(text);
		for (WordListPerspective row : this.list) {
			row.setHighlightedText(Strings.highlight(row.getText(), words));
			row.setHighlightedGlossary(Strings.highlight(row.getGlossary(), words));
		}

	}

	private String buildTitle(Request request) {
		Locale locale = request.getLocale();
		if (Strings.isBlank(language) && Strings.isBlank(category) && Strings.isBlank(letter) && Strings.isBlank(text)) {
			return wordsMsg.get("searching", locale);
		}
		StringBuilder title = new StringBuilder();
		if (Strings.isNotBlank(category)) {
			title.append(categoryMsg.get("code",category+".plural", locale));
		} else {
			title.append(wordsMsg.get("any_words", locale));
		}
		if (Strings.isNotBlank(language)) {
			title.append(" ").append(wordsMsg.get("in", locale));
			title.append(" ");
			title.append(languageMsg.get("code",language, locale).toLowerCase(locale));
		}
		if (Strings.isNotBlank(letter)) {
			title.append(" ");
			title.append(wordsMsg.get("starting_with", locale));
			title.append(" ");
			if (letter.equals("other")) {
				title.append(wordsMsg.get("other_letter", locale));
			} else if (letter.equals("number")) {
				title.append(wordsMsg.get("number_letter", locale));
			} else {
				title.append(letter.toUpperCase(locale));
			}
		}
		if (Strings.isNotBlank(text)) {
			title.append(" ");
			if (Strings.isBlank(letter)) {
				title.append(wordsMsg.get("containing", locale));
			} else {
				title.append(wordsMsg.get("and_contains", locale));
			}
			String[] words = Strings.getWords(text);
			for (int i = 0; i < words.length; i++) {
				String word = words[i];
				if (i > 0) {
					title.append(" ").append(wordsMsg.get("and", locale));
				}
				title.append(" ");
				title.append("\"").append(word).append("\"");
			}
		}
		if ("validated".equals(state)) {
			title.append(" ").append(wordsMsg.get("that_are_validated", locale));
		}
		return title.toString();
	}

	public String getTitle() {
		return this.title;
	}

	public String getDescription() {
		return this.description;
	}

	public boolean isClean() {
		boolean blank = Strings.isBlank(text);
		blank &= Strings.isBlank(letter);
		blank &= Strings.isBlank(language);
		blank &= Strings.isBlank(category);
		blank &= Strings.isBlank(source);
		return blank;
	}

	public boolean isBlank() {
		return list.isEmpty();
	}

	private List<Option> buildCategoryOptions(Request request) {
		List<Option> options = Lists.newArrayList();
		Locale locale = request.getLocale();
		{
			Option option = new Option();
			option.setValue(buildUrl(request, text, language, null,letter, state));
			option.setLabel(wordsMsg.get("any", locale));
			option.setKey("default");
			option.setSelected(StringUtils.isBlank(category));
			options.add(option);
		}
		for (Option cat : wordsInterfaceHelper.getCategoryOptions(locale)) {
			Option option = new Option();
			String code = cat.getValue().toString();
			option.setValue(buildUrl(request, text, language, code,letter, state));
			option.setLabel(categoryMsg.get("code",code, locale));
			option.setSelected(code.equals(category));
			options.add(option);
		}
		return options;
	}

	private String buildUrl(Request request,String text, String language, String category, String letter, String state) {
		UrlBuilder url = new UrlBuilder(request.getLocalContext());
		url.folder(request.getLanguage()).folder("search");
		url.parameter("category", category);
		url.parameter("language", language);
		url.parameter("text", text);
		url.parameter("letter", letter);
		url.parameter("state", state);
		return url.toString();
	}

	private List<Option> buildLanguageOptions(Request request) {
		List<Option> options = Lists.newArrayList();
		Locale locale = request.getLocale();
		{
			Option option = new Option();
			option.setValue(buildUrl(request, text, null, category,letter, state));
			option.setLabel(wordsMsg.get("any", locale));
			option.setKey("default");
			option.setSelected(StringUtils.isBlank(language));
			options.add(option);
		}
		for (Option lang : wordsInterfaceHelper.getLanguageOptions(locale)) {
			Option option = new Option();
			String value = lang.getValue().toString();
			option.setValue(buildUrl(request, text, value, category,letter, state));
			option.setLabel(languageMsg.get("code",value, locale));
			option.setSelected(value.equals(language));
			options.add(option);
		}
		return options;
	}

	private List<Option> buildLetterOptions(Request request) {
		Locale locale = request.getLocale();
		List<Option> options = Lists.newArrayList();
		{
			Option option = new Option();
			option.setLabel(translate("any", locale));
			option.setKey("default");
			option.setSelected(Strings.isBlank(letter));
			option.setValue(buildUrl(request, text, language, category, null, state));
			options.add(option);
		}
		List<Option> letters = wordsInterfaceHelper.getLetterOptions(locale);
		for (Option character : letters) {
			Option option = new Option();
			option.setLabel(character.getLabel());
			option.setSelected(character.getValue().equals(letter));
			option.setValue(buildUrl(request, text, language, category, character.getValue().toString(), state));
			options.add(option);
		}
		return options;
	}

	private String translate(String key, Locale locale) {
		return wordsMsg.get(key, locale);
	}

	public List<WordListPerspective> getList() throws ModelException {
		return this.list;
	}

	public String getText() {
		return text;
	}

	public String getState() {
		return state;
	}

	public String getLanguage() {
		return language;
	}

	public String getCategory() {
		return category;
	}

	public String getEffectiveQuery() {
		return effectiveQuery;
	}

	public String getLetter() {
		return letter;
	}

	public Filters getFilters() {
		return filters;
	}

	private Filters buildFilters(Request request) {
		Locale locale = request.getLocale();
		Filters filters = new Filters();
		{
			Filter filter = new Filter(buildLetterOptions(request));
			filter.setVariant(Variant.index);
			filter.setTitle(translate("letters", locale));
			if (Strings.isBlank(letter)) {
				filter.setLabel(translate("letter", locale));
			} else {
				filter.setActive(true);
				if ("number".equals(letter)) {
					filter.setLabel("#");
				}
				else if ("other".equals(letter)) {
					filter.setLabel("&");
				}
				else {
					filter.setLabel(letter.toUpperCase());
				}
			}
			filters.addFilter(filter);
		}
		{
			Filter filter = new Filter(buildLanguageOptions(request));
			filter.setTitle(translate("languages", locale));
			if (Strings.isBlank(language)) {
				filter.setLabel(translate("language", locale));
			} else {
				filter.setActive(true);
				filter.setLabel(languageMsg.get("code", language, locale));
			}
			filters.addFilter(filter);
		}
		{
			Filter filter = new Filter(buildCategoryOptions(request));
			filter.setTitle(translate("categories", locale));
			if (Strings.isBlank(category)) {
				filter.setLabel(translate("category", locale));
			} else {
				filter.setActive(true);
				filter.setLabel(categoryMsg.get("code", category, locale));
			}
			filters.addFilter(filter);
		}
		return filters;
	}

	private List<Option> buildPages(Request request) {
		List<Option> pages = Lists.newArrayList();
		int pageCount = (int) Math.ceil((float)count/(float)pageSize);
		if (pageCount>1) {
			int min = Math.max(1,page-PAGING);
			int max = Math.min(pageCount, page+PAGING);
			if (min>1) {
				pages.add(buildOption(1, request));
			}
			if (min>2) {
				pages.add(null);
			}
			for (int i = min; i <= max; i++) {
				pages.add(buildOption(i, request));
			}
			if (max<pageCount-1) {
				pages.add(null);
			}
			if (max<pageCount) {
				pages.add(buildOption(pageCount, request));
			}
			if (page>0) {
				previousPage = buildOption(page, request).getValue().toString();
			}
			if (page+1<max) {
				nextPage = buildOption(page+2, request).getValue().toString();
			}
		}
		return pages;
	}

	public List<Option> getPages() {
		return pages;
	}

	public String getNextPage() {
		return nextPage;
	}

	public String getPreviousPage() {
		return previousPage;
	}

	private Option buildOption(int num, Request request) {
		Option option = new Option();
		UrlBuilder url = new UrlBuilder(request.getBaseContext()).folder(request.getLanguage()).folder("search");
		if (num>1) {
			url.folder(num);
		}
		url.parameter("text", text).parameter("category", category).parameter("language", language).parameter("letter", letter).parameter("state", state);
		option.setValue(url.toString());
		option.setKey(num==1 ? "default" : null);
		option.setLabel(num+"");
		option.setSelected(page==num-1);
		return option;
	}

	public int getCount() {
		return count;
	}

	public void setWordService(WordService wordService) {
		this.wordService = wordService;
	}

	public void setWordsInterfaceHelper(WordsInterfaceHelper wordsInterfaceHelper) {
		this.wordsInterfaceHelper = wordsInterfaceHelper;
	}

	public void setLoadManager(LoadManager loadManager) {
		this.loadManager = loadManager;
	}
}
