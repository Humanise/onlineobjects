package dk.in2isoft.onlineobjects.services;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.tika.langdetect.optimaize.OptimaizeLangDetector;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.language.detect.LanguageResult;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;

import dk.in2isoft.commons.lang.Counter;
import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.model.Language;
import dk.in2isoft.onlineobjects.model.LexicalCategory;
import dk.in2isoft.onlineobjects.modules.language.TextAnalysis;
import dk.in2isoft.onlineobjects.modules.language.WordListPerspective;
import dk.in2isoft.onlineobjects.modules.language.WordListPerspectiveQuery;

public class LanguageService {

	private ModelService modelService;
		
	private SemanticService semanticService;
	
	private Set<Locale> locales = Sets.newHashSet(new Locale("en","US"),new Locale("da","DK"));
		
	public Language getLanguageForCode(String code, Operator operator) {
		if (Strings.isBlank(code)) {
			return null;
		}
		Query<Language> query = Query.of(Language.class).withField(Language.CODE, code);
		return modelService.search(query, operator).getFirst();
	}
	
	public Locale getLocaleForCode(String language) {
		for (Locale locale : locales) {
			if (language.equals(locale.getLanguage())) {
				return locale;
			}
		}
		return new Locale(language);
	}

	public LexicalCategory getLexcialCategoryForCode(String code, Operator operator) {
		if (Strings.isBlank(code)) {
			return null;
		}
		Query<LexicalCategory> query = Query.of(LexicalCategory.class).withField(LexicalCategory.CODE, code);
		return modelService.search(query, operator).getFirst();
	}
	
	public Counter<String> countLanguages(List<WordListPerspective> perspectives) {
		Multimap<String,String> wordsToLanguages = HashMultimap.create();
		for (WordListPerspective perspective : perspectives) {
			if (perspective.getLanguage()!=null) {
				wordsToLanguages.put(perspective.getText().toLowerCase(), perspective.getLanguage());					
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
		return languageCounts;
	}
	
	public Locale getLocale(String text) {
		if (Strings.isBlank(text)) {
			return null;
		}		
		LanguageDetector detector = new OptimaizeLangDetector().loadModels();
        LanguageResult result = detector.detect(text);
        String language = result.getLanguage();
		if (Strings.isBlank(language)) {
			return null;
		}
		return new Locale(language);
	}

	public Locale getSupportedLocale(Locale loc) {
		if (loc != null) {
			String language = loc.getLanguage();
			if ("no".equals(language) || "sv".equals(language)) {
				return getLocaleForCode("da");
			}
			if ("en".equals(language) || "da".equals(language)) {
				return loc;
			}
		}

		return getLocaleForCode("en");
	}

	public TextAnalysis analyse(String text, Operator operator) throws ModelException {
		String[] words = semanticService.getWords(text);
		
		semanticService.lowercaseWords(words);
		
		List<String> uniqueWords = Strings.asList(semanticService.getUniqueWords(words));
		
		WordListPerspectiveQuery query = new WordListPerspectiveQuery().withWords(uniqueWords);
		
		List<WordListPerspective> list = modelService.list(query, operator);
		
		List<String> unknownWords = Lists.newArrayList();
		
		Set<String> knownWords = new HashSet<String>();
				
		Multimap<String,String> wordsByLanguage = HashMultimap.create();
		
		for (WordListPerspective perspective : list) {
			String word = perspective.getText().toLowerCase();
			knownWords.add(word);
			if (perspective.getLanguage()!=null) {
				wordsByLanguage.put(perspective.getLanguage(), word);
			}
		}
		
		Multiset<String> languages = wordsByLanguage.keys();
		String language = null;
		for (String lang : languages) {
			if (language==null || (wordsByLanguage.get(lang).size()>wordsByLanguage.get(language).size())) {
				language = lang;
			}
		}
		
		
		for (String word : uniqueWords) {
			if (!knownWords.contains(word)) {
				unknownWords.add(word);
			}
		}
		
		Locale possibleLocale = Locale.ENGLISH;
		String[] sentences = semanticService.getSentences(text, possibleLocale);
		
		TextAnalysis analysis = new TextAnalysis();
		analysis.setLanguage(language);
		analysis.setSentences(Strings.asList(sentences));
		analysis.setWordsByLanguage(wordsByLanguage.asMap());
		analysis.setUniqueWords(uniqueWords);
		analysis.setKnownWords(list);
		analysis.setUnknownWords(unknownWords);
		return analysis;
	}
	
	
	// Wiring...

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
	
	public void setSemanticService(SemanticService semanticService) {
		this.semanticService = semanticService;
	}


}
