package dk.in2isoft.onlineobjects.modules.language;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil;
import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.query.NativeQuery;
import org.hibernate.type.LongType;

import com.google.common.collect.Lists;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.core.CustomQuery;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.SearchResult;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.core.exceptions.ContentNotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.ExplodingClusterFuckException;
import dk.in2isoft.onlineobjects.core.exceptions.IllegalRequestException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.InternetAddress;
import dk.in2isoft.onlineobjects.model.Language;
import dk.in2isoft.onlineobjects.model.LexicalCategory;
import dk.in2isoft.onlineobjects.model.Property;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.model.Word;
import dk.in2isoft.onlineobjects.modules.index.IndexManager;
import dk.in2isoft.onlineobjects.modules.index.IndexSearchQuery;
import dk.in2isoft.onlineobjects.modules.index.IndexSearchResult;
import dk.in2isoft.onlineobjects.services.LanguageService;
import dk.in2isoft.onlineobjects.util.ValidationUtil;

public class WordService {
	
	private static final Logger log = LogManager.getLogger(WordService.class);

	private IndexManager index;
	private ModelService modelService;
	private LanguageService languageService;
	private SecurityService securityService;
	
	private boolean enableMaterializedView;

	private List<String> trademarks = Lists.newArrayList("rockwool");
	
	public SearchResult<WordListPerspective> search(WordQuery query, Operator operator) throws ExplodingClusterFuckException, ModelException {
		
		if (enableMaterializedView && Strings.isBlank(query.getText())) {
			WordListPerspectiveViewQuery listQuery = new WordListPerspectiveViewQuery().withPaging(query.getPage(), query.getPageSize()).orderByText();
			String letter = query.getLetter();
			if ("other".equals(letter)) {
				letter = "?";
			}
			if ("number".equals(letter)) {
				letter = "#";
			}
			listQuery.startingWith(letter).withLanguage(query.getLanguage()).withCategory(query.getCategory());
			return modelService.search(listQuery, operator);
		}
		StopWatch watch = new StopWatch();
		final List<Long> ids = Lists.newArrayList();
		IndexSearchQuery searchQuery = buildQuery(query);
		searchQuery.setPage(query.getPage());
		searchQuery.setPageSize(query.getPageSize());
		watch.start();
		SearchResult<IndexSearchResult> indexResult = index.search(searchQuery);
		if (indexResult.getTotalCount()==0) {
			return SearchResult.empty();
		}
		for (IndexSearchResult item : indexResult.getList()) {
			Document document = item.getDocument();
			IndexableField field = document.getField("id");
			ids.add(Long.parseLong(field.stringValue()));
		}
		watch.stop();
		log.trace("Index query time="+watch.getTime());
		SearchResult<WordListPerspective> result;
		watch.reset();
		watch.start();
		if (query.isCached()) {
			WordListPerspectiveViewQuery listQuery = new WordListPerspectiveViewQuery().withPaging(0, query.getPageSize()).orderByText();
			if (!ids.isEmpty()) {
				listQuery.withIds(ids);
			}
			result = modelService.search(listQuery, operator);
		} else {
			WordListPerspectiveQuery listQuery = new WordListPerspectiveQuery().withPaging(0, query.getPageSize()).orderByText();
			if (!ids.isEmpty()) {
				listQuery.withIds(ids);
			}
			result = modelService.search(listQuery, operator);
		}
		watch.stop();
		log.trace("Database query time="+watch.getTime());
		
		Collections.sort(result.getList(), (o1, o2) -> {
			int index1 = ids.indexOf(o1.getId());
			int index2 = ids.indexOf(o2.getId());
			if (index1>index2) {
				return 1;
			} else if (index2>index1) {
				return -1;
			}
			return 0;
		});
		result.setTotalCount(indexResult.getTotalCount());
		result.setDescription(searchQuery.getQuery());
		return result;
	}
	
	private IndexSearchQuery buildQuery(WordQuery query) {

		String text = query.getText();
		String letter = query.getLetter();
		String language = query.getLanguage();
		String category = query.getCategory();
		String[] words = query.getWords();
		String source = query.getSource();
		
		boolean order = true;
		
		StringBuilder searchQuery = new StringBuilder();
		if (StringUtils.isNotBlank(text)) {
			String[] textWords = Strings.getWords(text);
			if (textWords.length>1) {
				searchQuery.append("word:");
				for (String word : textWords) {
					searchQuery.append(QueryParserUtil.escape(word));
				}
				searchQuery.append("^7 OR ");
				searchQuery.append("word:");
				for (String word : textWords) {
					searchQuery.append(QueryParserUtil.escape(word));
				}
				searchQuery.append("*^6 OR ");
				searchQuery.append("word:");
				for (String word : textWords) {
					searchQuery.append(QueryParserUtil.escape(word)).append("*");
				}
				searchQuery.append("^5 OR ");
			}
			for (int i = 0; i < textWords.length; i++) {
				String word = textWords[i];
				if (i > 0) {
					searchQuery.append(" AND ");
				}
				searchQuery.append("(word:").append(QueryParserUtil.escape(word)).append("^4").append(" OR word:").append(QueryParserUtil.escape(word)).append("*^4 OR ").append(QueryParserUtil.escape(word)).append("*");
				searchQuery.append(" OR word:*").append(QueryParserUtil.escape(word)).append("*^4");
				searchQuery.append(" OR *").append(QueryParserUtil.escape(word)).append("*");
				searchQuery.append(")");
			}
			order = false;
		}
		if (StringUtils.isNotBlank(letter)) {
			if (searchQuery.length()>0) {
				searchQuery.append(" AND ");
			}
			searchQuery.append("(letter:").append(QueryParserUtil.escape(letter)).append(")");
		}
				
		if (Strings.isNotBlank(language)) {
			if (searchQuery.length()>0) {
				searchQuery.append(" AND ");
			}
			searchQuery.append("language:").append(language);
		}
		if (Strings.isNotBlank(category)) {
			if (searchQuery.length()>0) {
				searchQuery.append(" AND ");
			}
			searchQuery.append("category:").append(category);
		}
		if (Strings.isNotBlank(source)) {
			if (searchQuery.length()>0) {
				searchQuery.append(" AND ");
			}
			searchQuery.append("source:").append(source);
		}
		if (query.isSourceDefined()) {
			if (searchQuery.length()>0) {
				searchQuery.append(" AND ");
			}
			searchQuery.append("NOT source:none");
			
		}
		if (Strings.isDefined(words)) {
			if (searchQuery.length()>0) {
				searchQuery.append(" AND ");
			}
			searchQuery.append("(");
			for (int i = 0; i < words.length; i++) {
				if (i > 0) {
					searchQuery.append(" OR ");
				}
				searchQuery.append("word:");
				searchQuery.append("\"").append(QueryParserUtil.escape(words[i])).append("\"");
			}
			searchQuery.append(")");
			order = false;
		}
		IndexSearchQuery isq = new IndexSearchQuery(searchQuery.toString());
		if (order) {
			isq.addStringOrdering("word");
		}
		return isq;
	}
	
	public WordImpression getImpression(Word word, Operator operator) throws ModelException {
		WordImpression impression = new WordImpression();
		if (trademarks.contains(word.getText().toLowerCase())) {
			impression.setTrademark(true);
		}
		impression.setWord(word);
		impression.setLanguage(modelService.getParent(word, Language.class, operator));
		impression.setLexicalCategory(modelService.getParent(word, LexicalCategory.class, operator));
		impression.setOriginator(modelService.getChild(word, User.class, operator));
		impression.setSource(modelService.getChild(word, Relation.KIND_COMMON_SOURCE, InternetAddress.class, operator));
		impression.setGlossary(word.getPropertyValue(Property.KEY_SEMANTICS_GLOSSARY));
		impression.setExamples(word.getPropertyValues(Property.KEY_SEMANTICS_EXAMPLE));
		String dataSource = word.getPropertyValue(Property.KEY_DATA_SOURCE);
		impression.setDataSource(dataSource);
		if (Strings.isNotBlank(dataSource)) {
			impression.setSourceTitle(dataSource.startsWith("WID") ? "wordnet.princeton.edu" : "WordNet.dk");
		}
		return impression;
	}


	public List<WordImpression> getImpressions(Query<Word> query, Operator operator) throws ModelException {
		return getImpressions(modelService.list(query, operator), operator);
	}

	public List<WordImpression> getImpressions(List<Word> list, Operator operator) throws ModelException {
		List<WordImpression> impressions = Lists.newArrayList();
		for (Word word : list) {
			impressions.add(getImpression(word, operator));
		}
		return impressions;
	}
	
	public Word createWord(String languageCode, String category, String text, Operator operator) throws ModelException, IllegalRequestException, SecurityException, ContentNotFoundException {
		if (StringUtils.isBlank(languageCode)) {
			throw new IllegalRequestException("No language provided");
		}
		if (StringUtils.isBlank(text)) {
			throw new IllegalRequestException("No text provided");
		}
		LexicalCategory lexicalCategory = null;
		if (StringUtils.isNotBlank(category)) {
			lexicalCategory = languageService.getLexcialCategoryForCode(category, operator);
			if (lexicalCategory==null) {
				throw new IllegalRequestException("Unsupported category ("+category+")");
			}
		}
		Language language = languageService.getLanguageForCode(languageCode, operator);
		if (language==null) {
			throw new IllegalRequestException("Unsupported language ("+languageCode+")");
		}
		Query<Word> query = Query.of(Word.class).withField(Word.TEXT_FIELD, text).from(language);
		if (lexicalCategory!=null) {
			query.from(lexicalCategory);
		}
		List<Word> list = modelService.list(query, operator);
		if (list.size()==0) {
			Word word = new Word();
			word.setText(text);
			modelService.create(word, operator);
			securityService.grantPublicView(word, true, operator);
			Relation languageRelation = modelService.createRelation(language, word, operator);
			securityService.grantPublicView(languageRelation, true, operator);
			if (lexicalCategory!=null) {
				Relation categoryRelation = modelService.createRelation(lexicalCategory, word, operator);
				securityService.grantPublicView(categoryRelation, true, operator);
			}
			ensureOriginator(word, operator);
			return word;
		} else {
			return list.iterator().next();
		}
	}

	public void updateWord(WordModification modification, Operator operator) throws ModelException, IllegalRequestException, SecurityException {
		StopWatch watch = new StopWatch();
		WordListPerspectiveQuery query = new WordListPerspectiveQuery();
		query.withWord(modification.text.toLowerCase());
		log.debug("Searching for: " + modification.text);
		watch.start();
		List<WordListPerspective> list = modelService.list(query, operator);
		watch.stop();
		log.info("Found: " + list.size() + ", time=" + watch.getTime());
		watch.reset();
		watch.start();
		InternetAddress source = null;
		if (Strings.isNotBlank(modification.source)) {
			if (ValidationUtil.isWellFormedURI(modification.source)) {
				source = getSource(modification.source, operator);
			}
		}
		watch.stop();
		log.info("Source lookup time=" + watch.getTime());
		
		//Word word = getWordBySourceId(modification.sourceId, privileged);
		
		List<WordMatch> matches = Lists.newArrayList();
		for (WordListPerspective perspective : list) {
			WordMatch match = new WordMatch();
			if (!perspective.getText().equalsIgnoreCase(modification.text)) {
				log.warn("Skipping because text is different: " + Strings.toJSON(perspective));
				continue;
			}
			if (!Strings.isBlank(perspective.getLanguage())) {
				if (!perspective.getLanguage().equals(modification.language)) {
					log.warn("Skipping because language is different: " + Strings.toJSON(perspective));
					continue;
				}
				match.language = true;
			}
			// Only look at category if set
			if (!Strings.isBlank(modification.lexicalCategory)) {
				if (!Strings.isBlank(perspective.getLexicalCategory())) {
					if (!modification.lexicalCategory.equals(perspective.getLexicalCategory())) {
						log.warn("Skipping because category is different: " + Strings.toJSON(perspective));
						continue;
					}
				}
				// Category matches if found is uncategorized or is equal to the modification
				match.category = Strings.isBlank(perspective.getLexicalCategory()) || Strings.equals(modification.lexicalCategory, perspective.getLexicalCategory());
			}
			
			Word word = modelService.get(Word.class, perspective.getId(), operator);
			match.word = word;
			String sourceId = word.getPropertyValue(Property.KEY_DATA_SOURCE);
			if (Strings.isNotBlank(sourceId)) {
				if (Strings.equals(modification.sourceId, sourceId)) {
					match.source = true;
				} else {
					log.warn("Skipping because source is different: " + Strings.toJSON(perspective));
					continue;
				}
			}
			if (Strings.isNotBlank(perspective.getGlossary()) && !match.source) {
				// Skip words that have a glossary 
				log.warn("Skipping because it has a glossary: " + Strings.toJSON(perspective));
				continue;
			}
			// TODO: Check glossary - fail or skip if it is already set with something else
			
			match.text = perspective.getText().equals(modification.text);
			match.perspective = perspective;
			matches.add(match);				
		}
		Word word;
		if (!matches.isEmpty()) {
			Collections.sort(matches);
			log.info("Mathces: " + Strings.toJSON(matches));
			
			WordMatch best = matches.get(0);
			log.info("Best match: " + Strings.toJSON(best.perspective));
			word = best.word;
		} else {
			log.debug("No matches for " + modification.text);
			word = new Word();
			word.setText(modification.text);
			modelService.create(word, operator);
		}
		updateWord(word,modification,source,operator);
	}

	public @NonNull InternetAddress getSource(String src, Operator operator) throws ModelException, SecurityException {
		Query<InternetAddress> query = Query.after(InternetAddress.class).withField(InternetAddress.FIELD_ADDRESS, src);
		List<InternetAddress> list = modelService.list(query, operator);
		for (InternetAddress address : list) {
			CustomQuery<Long> q = new CustomQuery<Long>() {

				@Override
				public String getSQL() {
					return "select count(relation.id) from relation where relation.kind='common.source' and relation.sub_entity_id=:id";
				}

				@Override
				public String getCountSQL() {
					return null;
				}

				@Override
				public Long convert(Object[] row) {
					return ((Number) row[0]).longValue();
				}

				@Override
				public void setParameters(NativeQuery<?> sql) {
					sql.setParameter("id", address.getId(), LongType.INSTANCE);
				}
			};
			List<Long> num = modelService.list(q, operator);
			if (!num.isEmpty() && num.get(0) > 0) {
				return address;
			}
		}
		InternetAddress address = new InternetAddress();
		address.setAddress(src);
		address.setName(Strings.simplifyURL(src));
		modelService.create(address, operator);
		securityService.grantPublicView(address, true, operator);
		return address;
	}
	
	private void updateWord(Word word, WordModification modification, InternetAddress source, Operator operator) throws ModelException, IllegalRequestException, SecurityException {
		Language language = languageService.getLanguageForCode(modification.language, operator);
		if (language == null) {
			throw new IllegalRequestException("No language in modification");
		}
		LexicalCategory category = null;
		String categoryCode = modification.lexicalCategory;
		if (Strings.isNotBlank(categoryCode)) {
			category = languageService.getLexcialCategoryForCode(categoryCode, operator);
			if (category==null) {
				throw new IllegalRequestException("Unsupported category: " + categoryCode);
			}
			changeCategory(word, category, operator);
		}
		changeLanguage(word, language, operator);
		if (Strings.isNotBlank(modification.glossary)) {
			// TODO: Only modify if needed + remove others
			word.removeProperties(Property.KEY_SEMANTICS_GLOSSARY);
			word.addProperty(Property.KEY_SEMANTICS_GLOSSARY, modification.glossary);
			modelService.update(word, operator);
		}
		if (Strings.isNotBlank(modification.sourceId)) {
			// TODO: Only modify if needed + remove others
			word.removeProperties(Property.KEY_DATA_SOURCE);
			word.addProperty(Property.KEY_DATA_SOURCE, modification.sourceId);
			modelService.update(word, operator);
		}
		updateSource(word, source, operator);
		if (modification.clearOriginators) {
			List<Relation> originators = modelService.find().relations(operator).from(word).to(InternetAddress.class).withKind(Relation.KIND_COMMON_ORIGINATOR).list();
			log.info("Word->InternetAddress originator count: " + originators.size());
			for (Relation relation : originators) {
				modelService.delete(relation, operator);
			}
		}
		//modelService.getChildren(word, Relation.KIND_COMMON_ORIGINATOR, InternetAddress.class);
		securityService.grantPublicView(word, true, operator);
	}

	public void updateSource(Word word, Entity source, Operator operator) throws ModelException, SecurityException {
		if (source!=null) {
			Relation existing = modelService.getRelation(word, source, Relation.KIND_COMMON_SOURCE, operator).orElse(null);
			boolean create = false;
			if (existing==null) {
				create = true;
			}
			else if (existing.getTo().getId()!=source.getId()) {
				modelService.delete(existing, operator);
				create = true;
			}

			if (create) {
				Relation relation = modelService.createRelation(word, source, Relation.KIND_COMMON_SOURCE, operator);
				securityService.grantPublicView(relation, true, operator);
			}
		}
	}
	
	private void changeLanguage(Word word, Language language, Operator operator) throws ModelException, SecurityException {
		List<Relation> parents = modelService.find().relations(operator).to(word).from(Language.class).list();
		boolean found = false;
		for (Relation relation : parents) {
			if (!relation.getFrom().equals(language)) {
				modelService.delete(relation, operator);
			} else {
				found = true;
			}
		}
		if (!found) {
			Relation relation = modelService.createRelation(language, word, operator);
			securityService.grantPublicView(relation, true, operator);
		}
	}

	private void changeCategory(Word word, LexicalCategory category, Operator operator) throws ModelException, SecurityException {
		List<Relation> parents = modelService.find().relations(operator).to(word).from(LexicalCategory.class).list();
		boolean found = false;
		for (Relation relation : parents) {
			if (!relation.getFrom().equals(category)) {
				modelService.delete(relation, operator);
			} else {
				found = true;
			}
		}
		if (!found) {
			Relation relation = modelService.createRelation(category, word, operator);
			securityService.grantPublicView(relation, true, operator);
		}
	}

	class WordMatch implements Comparable<WordMatch> {
		Word word;
		WordListPerspective perspective;
		boolean source;
		boolean text;
		boolean language;
		boolean category;
		
		@Override
		public int compareTo(WordMatch other) {
			int score = getScore(this); 
			int otherScore = getScore(other);
			if (score == otherScore) {
				return this.word.getCreated().compareTo(other.word.getCreated());
			}
			return score > otherScore ? -1 : 1;
		}
		
		private int getScore(WordMatch other) {
			return (other.source ? 10 : 0) + (other.text ? 1 : 0) + (other.language ? 1 : 0) + (other.category ? 1 : 0);
		}
	}
	
	public Word getWordBySourceId(String sourceId, Operator privileged) {
		Query<Word> query = Query.after(Word.class).withCustomProperty(Property.KEY_DATA_SOURCE, sourceId).as(privileged);
		List<Word> list = modelService.list(query, privileged);
		if (!list.isEmpty()) {
			if (list.size() > 1) {
				log.warn("Found " + list.size() + " word with source ID=" + sourceId + ", max 1 expected");
			}
			return list.get(0);
		}
		return null;
	}
	
	public void ensureOriginator(Word word, Operator operator) throws ModelException, SecurityException, ContentNotFoundException {
		User user = modelService.getUser(operator);
		User existing = modelService.getChild(word, Relation.KIND_COMMON_ORIGINATOR, User.class, operator.as(securityService.getAdminPrivileged()));
		if (existing==null) {
			modelService.createRelation(word, user, Relation.KIND_COMMON_ORIGINATOR, operator);
		}
	}
	
	// Wiring...
	
	public void setIndex(IndexManager index) {
		this.index = index;
	}
	
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
	
	public void setLanguageService(LanguageService languageService) {
		this.languageService = languageService;
	}
	
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
}
