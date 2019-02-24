package dk.in2isoft.onlineobjects.apps.words.views;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.time.StopWatch;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import dk.in2isoft.commons.jsf.AbstractView;
import dk.in2isoft.commons.lang.Counter;
import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.apps.words.importing.WordsImporter;
import dk.in2isoft.onlineobjects.apps.words.perspectives.WordImportProspectPerspective;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.exceptions.IllegalRequestException;
import dk.in2isoft.onlineobjects.model.Language;
import dk.in2isoft.onlineobjects.model.LexicalCategory;
import dk.in2isoft.onlineobjects.modules.importing.ImportSession;
import dk.in2isoft.onlineobjects.modules.language.WordListPerspective;
import dk.in2isoft.onlineobjects.modules.language.WordListPerspectiveQuery;
import dk.in2isoft.onlineobjects.services.ImportService;
import dk.in2isoft.onlineobjects.services.LanguageService;
import dk.in2isoft.onlineobjects.services.SemanticService;
import dk.in2isoft.onlineobjects.ui.Request;
import dk.in2isoft.onlineobjects.ui.jsf.model.Option;
import dk.in2isoft.onlineobjects.util.Messages;

public class WordsImportListView extends AbstractView {

	private ModelService modelService;
	private LanguageService languageService;
	private ImportService importService;
	private SemanticService semanticService;
	
	private String id;
	private Language language;
	private String status;
	private String title;
	private long queryTime;
	private List<Option> categories;
	private List<Option> languageOptions;
	
	private List<WordImportProspectPerspective> list;


	public List<Option> getCategories() {
		return categories;
	}

	public List<Option> getLanguageOptions() {
		return languageOptions;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getId() {
		return id;
	}
	
	public long getQueryTime() {
		return queryTime;
	}
		
	public Language getLanguage() {
		return language;
	}
	
	public String getStatus() {
		return status;
	}
	
	public List<WordImportProspectPerspective> getList() {
		return list;
	}

	@Override
	protected void before(Request request) throws Exception {
		String[] path = request.getLocalPath();
		if (path.length==3) {
			StopWatch watch = new StopWatch();
			watch.start();
			id = path[2];
			ImportSession session = importService.getImportSession(id);
			if (session==null) {
				throw new IllegalRequestException("The session does not exist");
			}
			list = Lists.newArrayList();
			status = session.getStatus().name();
			WordsImporter handler = (WordsImporter) session.getTransport();
			this.title = handler.getTitle();
			String text = handler.getText();
			
			List<String> words = semanticService.getUniqueNoEmptyLines(text);
			List<String> lowercaseWords = semanticService.lowercaseWordsCopy(words);
			categories = buildCategories(request);
			languageOptions = buildLanguageOptions(request);
			
			if (words.size()<100) {
			
				WordListPerspectiveQuery perspectiveQuery = new WordListPerspectiveQuery().withWords(lowercaseWords).orderByText();
				List<WordListPerspective> found = modelService.list(perspectiveQuery, request);
	
				for (String word : words) {
					WordImportProspectPerspective perspective = new WordImportProspectPerspective();
					perspective.setText(word);
					Multimap<String, String> x = HashMultimap.create(); 
					for (WordListPerspective wordListPerspective : found) {
						if (wordListPerspective.getText()==null) {
							continue;
						}
						if (wordListPerspective.getText().toLowerCase().equals(word.toLowerCase())) {
							String lang = Strings.fallback(wordListPerspective.getLanguage(),"none");
							String category = Strings.fallback(wordListPerspective.getLexicalCategory(),"none");
							x.put(lang, category);
						}
					}
					perspective.setExisting(x.asMap());
					
					list.add(perspective);
				}
				
				
				
				Multimap<String,String> wordsToLanguages = HashMultimap.create();
				for (WordListPerspective perspective : found) {
					if (perspective.getLanguage()!=null) {
						wordsToLanguages.put(perspective.getText(), perspective.getLanguage());					
					}
				}
				
				Counter<String> languageCounts = new Counter<String>();
				Set<String> set = wordsToLanguages.keySet();
				for (String word : set) {
					Collection<String> langs = wordsToLanguages.get(word);
					for (String lang : langs) {
						languageCounts.addOne(lang);
					}
				}
				language = findLanguage(languageCounts.getTop(), request);
			}
			watch.stop();
			this.queryTime = watch.getTime();
		}
	}

	private Language findLanguage(String fromContent, Request request) throws IllegalRequestException {
		String[] path = request.getLocalPath();
		String pathLang = path[0];
		String queryLang = request.getString("language");
		Language language = null;
		if (Strings.isNotBlank(queryLang)) {
			language = languageService.getLanguageForCode(queryLang, request);			
		}
		if (language==null) {
			language = languageService.getLanguageForCode(fromContent, request);
		}
		if (language==null) {
			language = languageService.getLanguageForCode(pathLang, request);
		}
		if (language == null) {
			throw new IllegalRequestException("Unsupported language");
		}
		return language;
	}	
	
	private List<Option> buildCategories(Request request) {
		Locale locale = request.getLocale();

		Messages msg = new Messages(LexicalCategory.class);
		List<Option> categories = Lists.newArrayList();

		Option unknown = new Option(null,msg.get("code","none", locale));
		categories.add(unknown);
		
		Query<LexicalCategory> query = Query.of(LexicalCategory.class).orderByName();
		List<LexicalCategory> list = modelService.list(query, request);
		for (LexicalCategory category : list) {
			Option option = new Option();
			option.setValue(category.getCode());
			option.setLabel(msg.get("code",category.getCode(), locale));
			option.setDescription(msg.get("code",category.getCode()+"_description", locale));
			categories.add(option);
		}
		return categories;
	}
	
	private List<Option> buildLanguageOptions(Request request) {

		Messages msg = new Messages(Language.class);
		List<Option> languageOptions = Lists.newArrayList();
		Query<Language> query = Query.of(Language.class).orderByName();
		List<Language> list = modelService.list(query, request);
		Locale locale = request.getLocale();
		for (Language category : list) {
			Option option = new Option();
			option.setValue(category.getCode());
			option.setLabel(msg.get("code",category.getCode(), locale));
			languageOptions.add(option);
		}
		return languageOptions;
	}
	
	// Services...

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
	
	public void setLanguageService(LanguageService languageService) {
		this.languageService = languageService;
	}
	
	public void setImportService(ImportService importService) {
		this.importService = importService;
	}
	
	public void setSemanticService(SemanticService semanticService) {
		this.semanticService = semanticService;
	}
}
