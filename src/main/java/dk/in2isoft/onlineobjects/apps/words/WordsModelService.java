package dk.in2isoft.onlineobjects.apps.words;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.onlineobjects.common.Auditor;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import dk.in2isoft.commons.lang.Code;
import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.in2igui.data.Diagram;
import dk.in2isoft.in2igui.data.Node;
import dk.in2isoft.onlineobjects.apps.words.importing.WordsImporter;
import dk.in2isoft.onlineobjects.apps.words.perspectives.WordsImportRequest;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.core.exceptions.BadRequestException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.NotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.Language;
import dk.in2isoft.onlineobjects.model.LexicalCategory;
import dk.in2isoft.onlineobjects.model.Pile;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.model.Word;
import dk.in2isoft.onlineobjects.modules.importing.ImportSession;
import dk.in2isoft.onlineobjects.modules.language.WordListPerspective;
import dk.in2isoft.onlineobjects.modules.language.WordListPerspectiveQuery;
import dk.in2isoft.onlineobjects.modules.language.WordService;
import dk.in2isoft.onlineobjects.services.ImportService;
import dk.in2isoft.onlineobjects.services.LanguageService;
import dk.in2isoft.onlineobjects.services.PileService;
import dk.in2isoft.onlineobjects.services.SemanticService;
import dk.in2isoft.onlineobjects.util.Messages;

public class WordsModelService {

	private ModelService modelService;
	private LanguageService languageService;
	private SecurityService securityService;
	private PileService pileService;
	private SemanticService semanticService;
	private ImportService importService;
	private WordService wordService;

	private Map<String,Object> getData(Entity entity) {
		Map<String,Object> data = Maps.newHashMap();
		data.put("id", entity.getId());
		data.put("type", entity.getType());
		data.put("name", entity.getName());
		return data;
	}

	public Diagram getDiagram(String text, Operator operator) throws ModelException {
		Messages msg = new Messages(WordsController.class);
		boolean showLanguage = !false;
		Diagram diagram = new Diagram();
		diagram.setMaxNodeCount(100);
		Query<Word> query = Query.of(Word.class).withFieldLowercase(Word.TEXT_FIELD, text);
		List<Word> words = modelService.search(query, operator).getList();
		for (Word word : words) {
			if (diagram.isFull()) {
				break;
			}
			Node wordNode = new Node();
			wordNode.setId(word.getId());
			wordNode.setTitle(word.getText());
			wordNode.addProperty("Type", "Word");
			wordNode.setData(getData(word));
			diagram.addNode(wordNode);
			Locale locale = new Locale("en");

			List<Relation> relationsAwayFrom = modelService.find().relations(operator).from(word).to(Word.class).list();
			for (Relation relation : relationsAwayFrom) {
				if (diagram.isFull()) {
					break;
				}
				Entity child = relation.getTo();
				Node childNode = new Node();
				childNode.setId(child.getId());
				childNode.setTitle(child.getName());
				childNode.addProperty("Type", "Word");
				childNode.setData(getData(child));
				diagram.addNode(childNode);
				diagram.addEdge(wordNode,msg.get(relation.getKind(), locale),childNode);

				if (!diagram.isFull() && showLanguage) {
					Language language = modelService.getParent(child, Language.class, operator);
					if (language!=null) {
						Node langNode = new Node();
						langNode.setId(language.getId());
						langNode.setTitle(language.getName());
						langNode.addProperty("Type", "Language");
						diagram.addNode(langNode);
						diagram.addEdge(langNode,childNode);
					}
				}
			}

			List<Relation> parentRelations = modelService.find().relations(operator).to(word).from(Word.class).list();
			for (Relation relation : parentRelations) {
				if (diagram.isFull()) {
					break;
				}
				Entity parent = relation.getFrom();
				Node childNode = new Node();
				childNode.setId(parent.getId());
				childNode.setTitle(parent.getName());
				childNode.addProperty("Type", "Word");
				childNode.setData(getData(parent));
				diagram.addNode(childNode);
				diagram.addEdge(wordNode,msg.get(relation.getKind()+".reverse", locale),childNode);

				if (!diagram.isFull() && showLanguage) {
					Language language = modelService.getParent(parent, Language.class, operator);
					if (language!=null) {
						Node langNode = new Node();
						langNode.setId(language.getId());
						langNode.setTitle(language.getName());
						langNode.addProperty("Type", "Language");
						diagram.addNode(langNode);
						diagram.addEdge(langNode,childNode);
					}
				}
			}

			if (!diagram.isFull() && showLanguage) {
				Language language = modelService.getParent(word, Language.class, operator);
				if (language!=null) {
					Node langNode = new Node();
					langNode.setId(language.getId());
					langNode.setTitle(language.getName());
					langNode.addProperty("Type", "Language");
					diagram.addNode(langNode);
					diagram.addEdge(langNode, wordNode);
				}
			}
			if (!diagram.isFull()) {
				LexicalCategory category = modelService.getParent(word, LexicalCategory.class, operator);
				if (category!=null) {
					Node categoryNode = new Node();
					categoryNode.setId(category.getId());
					categoryNode.setTitle(category.getName());
					categoryNode.addProperty("Type", "Lexical category");
					diagram.addNode(categoryNode);
					diagram.addEdge(wordNode,"Category",categoryNode);

					if (!diagram.isFull()) {
						LexicalCategory superCategory = modelService.getParent(category, Relation.KIND_STRUCTURE_SPECIALIZATION, LexicalCategory.class, operator);
						if (superCategory!=null) {
							Node superNode = new Node();
							superNode.setId(superCategory.getId());
							superNode.setTitle(superCategory.getName());
							superNode.addProperty("Type", "Lexical category");
							diagram.addNode(superNode);
							diagram.addEdge(superNode,"Specialization",categoryNode);
						}
					}
				}
			}
			if (!diagram.isFull()) {
				User user = modelService.getChild(word, Relation.KIND_COMMON_ORIGINATOR, User.class, operator);
				if (user!=null) {
					Node userNode = new Node();
					userNode.setId(user.getId());
					userNode.setTitle(user.getName());
					userNode.addProperty("Type", "User");
					diagram.addNode(userNode);
					diagram.addEdge(wordNode,"Originator",userNode);
				}
			}
		}
		return diagram;
	}

	public void createWord(String languageCode, String category, String text, Operator operator) throws ModelException, BadRequestException, SecurityException, NotFoundException {
		if (StringUtils.isBlank(languageCode)) {
			throw new BadRequestException("No language provided");
		}
		if (StringUtils.isBlank(text)) {
			throw new BadRequestException("No text provided");
		}
		LexicalCategory lexicalCategory = null;
		if (StringUtils.isNotBlank(category)) {
			lexicalCategory = languageService.getLexcialCategoryForCode(category, operator);
			if (lexicalCategory==null) {
				throw new BadRequestException("Unsupported category ("+category+")");
			}
		}
		Language language = languageService.getLanguageForCode(languageCode, operator);
		if (language==null) {
			throw new BadRequestException("Unsupported language ("+languageCode+")");
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
			securityService.makePublicVisible(word, operator);
			Relation languageRelation = modelService.createRelation(language, word, operator);
			securityService.makePublicVisible(languageRelation, operator);
			if (lexicalCategory!=null) {
				Relation categoryRelation = modelService.createRelation(lexicalCategory, word, operator);
				securityService.makePublicVisible(categoryRelation, operator);
			}
			wordService.ensureOriginator(word, operator);
		}
	}

	public void deleteRelation(long id, Operator operator) throws BadRequestException, SecurityException, ModelException {
		Relation relation = modelService.getRelation(id, operator).orElseThrow(() ->
			new BadRequestException("Relation not found (id="+id+")")
		);
		modelService.delete(relation, operator);
	}

	public void relateWords(long parentId,String kind, long childId, Operator operator) throws ModelException, BadRequestException, SecurityException {
		Word parentWord = modelService.get(Word.class, parentId, operator);
		Word childWord = modelService.get(Word.class, childId, operator);
		if (parentWord==null || childWord==null) {
			throw new BadRequestException("Word not found");
		}
		Set<String> allowed = Sets.newHashSet(Relation.KIND_SEMANTICS_EQUIVALENT, Relation.KIND_SEMANTICS_ANTONYMOUS, Relation.KIND_SEMANTICS_SYNONYMOUS,Relation.KIND_SEMANTICS_ANALOGOUS, Relation.KIND_SEMANTICS_MORPHEME, Relation.KIND_SEMANTICS_GENRALTIZATION);
		if (!allowed.contains(kind)) {
			throw new BadRequestException("Illegal relation: "+kind);
		}

		Optional<Relation> relation = modelService.getRelation(parentWord, childWord, kind, operator);
		if (!relation.isPresent()) {
			Relation newRelation = modelService.createRelation(parentWord, childWord, kind, operator);
			securityService.makePublicVisible(newRelation, operator);

		}
	}

	public void changeLanguage(long wordId, String languageCode, Operator operator) throws ModelException, BadRequestException, SecurityException, NotFoundException {
		Word word = getWord(wordId, operator);
		if (word==null) {
			throw new NotFoundException(Word.class, wordId);
		}
		changeLanguage(word, languageCode, operator);
	}

	public void changeLanguage(Word word, String languageCode, Operator privileged) throws ModelException, BadRequestException, SecurityException, NotFoundException {
		Language language = null;
		if (languageCode!=null) {
			language = languageService.getLanguageForCode(languageCode, privileged);
			if (language==null) {
				throw new BadRequestException("Unsupported language ("+languageCode+")");
			}
		}
		List<Relation> parents = modelService.find().relations(privileged).to(word).from(Language.class).list();
		modelService.delete(parents, privileged);
		if (language!=null) {
			Relation relation = modelService.createRelation(language, word, privileged);
			securityService.makePublicVisible(relation, privileged);
		}
		wordService.ensureOriginator(word, privileged);
	}

	public void changeCategory(long wordId, String category, Operator operator) throws ModelException, BadRequestException, SecurityException, NotFoundException {
		Word word = getWord(wordId, operator);
		changeCategory(word, category, operator);
	}

	public void changeCategory(Word word, String category, Operator operator) throws BadRequestException, ModelException, SecurityException, NotFoundException {
		LexicalCategory lexicalCategory = languageService.getLexcialCategoryForCode(category, operator);
		if (lexicalCategory==null) {
			throw new BadRequestException("Unsupported category ("+category+")");
		}
		List<Relation> parents = modelService.find().relations(operator).to(word).from(LexicalCategory.class).list();
		modelService.delete(parents, operator);
		Relation categoryRelation = modelService.createRelation(lexicalCategory, word, operator);
		securityService.makePublicVisible(categoryRelation, operator);
		wordService.ensureOriginator(word, operator);
	}



	public void deleteWord(long wordId, Operator operator) throws ModelException, BadRequestException, SecurityException {
		Word word = getWord(wordId, operator);
		modelService.delete(word, operator);
	}

	private Word getWord(long wordId, Operator operator) throws ModelException, BadRequestException {
		Word word = modelService.get(Word.class, wordId, operator);
		if (word==null) {
			throw new BadRequestException("Word not found (id="+wordId+")");
		}
		return word;
	}

	public void addToPostponed(Word word) throws ModelException, SecurityException {
		Operator operator = modelService.newAdminOperator();
		try {
			Pile pile = pileService.getOrCreateGlobalPile("words.postponed", operator);
			Optional<Relation> relation = modelService.getRelation(pile, word, operator);
			if (!relation.isPresent()) {
				modelService.createRelation(pile, word, operator);
			}
		} finally {
			operator.commit();
		}
	}

	public void importWords(WordsImportRequest object, Auditor auditor, Operator operator) throws ModelException, BadRequestException, SecurityException, NotFoundException {
		Language language = languageService.getLanguageForCode(object.getLanguage(), operator);
		LexicalCategory category = languageService.getLexcialCategoryForCode(object.getCategory(), operator);

		Code.checkNotNull(language, "Unsupported language: "+object.getLanguage());
		Code.checkNotNull(category, "Unsupported category: "+object.getCategory());

		Collection<String> words = object.getWords();
		if (Code.isEmpty(words)) {
			String sessionId = object.getSessionId();
			ImportSession session = importService.getImportSession(sessionId);
			Code.checkNotNull(session, "No session");
			session.getTransport();

			WordsImporter handler = (WordsImporter) session.getTransport();
			String text = handler.getText();

			words = semanticService.getUniqueNoEmptyLines(text);

		}
		int num = 0;

		for (String word : words) {
			WordListPerspectiveQuery query = new WordListPerspectiveQuery().withWord(word.toLowerCase());
			List<WordListPerspective> list = modelService.list(query, operator);
			List<WordListPerspective> candidates = Lists.newArrayList();
			for (WordListPerspective perspective : list) {
				// If no or same language
				if (perspective.getLanguage()==null || Strings.equals(object.getLanguage(),perspective.getLanguage())) {
					// If no or same category
					if (perspective.getLexicalCategory()==null || Strings.equals(object.getCategory(),perspective.getLexicalCategory())) {
						candidates.add(perspective);
					}

				}
			}
			if (candidates.size()==0) {
				createWord(language.getCode(), category.getCode(), word, operator);
				auditor.info("Created word: '"+word+"'");
			} else {
				WordListPerspective perspective = candidates.get(0);
				if (!perspective.getText().equals(word)) {
					Word loaded = modelService.get(Word.class, perspective.getId(),operator);
					if (loaded!=null) {
						loaded.setText(word);
						auditor.info("Changed text from '"+perspective.getText()+"' to '"+word+"'");
					}
				}
				if (perspective.getLanguage()==null) {
					changeLanguage(perspective.getId(), language.getCode(), operator);
					auditor.info("Changed language of '"+word+"' to "+language.getCode());
				}
				if (perspective.getLexicalCategory()==null) {
					changeCategory(perspective.getId(), category.getCode(), operator);
					auditor.info("Changed category of '"+word+"' to "+category.getCode());
				}
			}
			num++;
			if (num>100) {
				operator.commit();
				//System.out.println(Math.round(((float) num)/(float) words.size()*100)+"%");
				num=0;
			}
		}
		operator.commit();
	}


	// Wiring...

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	public void setLanguageService(LanguageService languageService) {
		this.languageService = languageService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public void setPileService(PileService pileService) {
		this.pileService = pileService;
	}

	public void setImportService(ImportService importService) {
		this.importService = importService;
	}

	public void setSemanticService(SemanticService semanticService) {
		this.semanticService = semanticService;
	}

	public void setWordService(WordService wordService) {
		this.wordService = wordService;
	}
}
