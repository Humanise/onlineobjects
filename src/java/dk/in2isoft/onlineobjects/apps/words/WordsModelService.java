package dk.in2isoft.onlineobjects.apps.words;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import dk.in2isoft.in2igui.data.Diagram;
import dk.in2isoft.in2igui.data.Node;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.core.UserSession;
import dk.in2isoft.onlineobjects.core.exceptions.ContentNotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.IllegalRequestException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.Language;
import dk.in2isoft.onlineobjects.model.LexicalCategory;
import dk.in2isoft.onlineobjects.model.Pile;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.model.Word;
import dk.in2isoft.onlineobjects.services.LanguageService;
import dk.in2isoft.onlineobjects.services.PileService;
import dk.in2isoft.onlineobjects.util.Messages;

public class WordsModelService {
	
	private ModelService modelService;
	private LanguageService languageService;
	private SecurityService securityService;
	private PileService pileService;
	
	private Map<String,Object> getData(Entity entity) {
		Map<String,Object> data = Maps.newHashMap();
		data.put("id", entity.getId());
		data.put("type", entity.getType());
		data.put("name", entity.getName());
		return data;
	}

	public Diagram getDiagram(String text) throws ModelException {
		Messages msg = new Messages(WordsController.class);
		Diagram diagram = new Diagram();
		Query<Word> query = Query.of(Word.class).withFieldLowercase(Word.TEXT_FIELD, text);
		List<Word> words = modelService.search(query).getList();
		for (Word word : words) {
			Node wordNode = new Node();
			wordNode.setId(word.getId());
			wordNode.setTitle(word.getText());
			wordNode.addProperty("Type", "Word");
			wordNode.setData(getData(word));
			diagram.addNode(wordNode);
			Locale locale = new Locale("en");
			
			List<Relation> childRelations = modelService.getChildRelations(word, Word.class);
			for (Relation relation : childRelations) {
				Entity child = relation.getSubEntity();
				Node childNode = new Node();
				childNode.setId(child.getId());
				childNode.setTitle(child.getName());
				childNode.addProperty("Type", "Word");
				childNode.setData(getData(child));
				diagram.addNode(childNode);
				diagram.addEdge(wordNode,msg.get(relation.getKind()+".reverse", locale),childNode);
				
				Language language = modelService.getParent(child, Language.class);
				if (language!=null) {
					Node langNode = new Node();
					langNode.setId(language.getId());
					langNode.setTitle(language.getName());
					langNode.addProperty("Type", "Language");
					diagram.addNode(langNode);
					diagram.addEdge(childNode,langNode);
				}
			}
			
			List<Relation> parentRelations = modelService.getParentRelations(word, Word.class);
			for (Relation relation : parentRelations) {
				Entity parent = relation.getSuperEntity();
				Node childNode = new Node();
				childNode.setId(parent.getId());
				childNode.setTitle(parent.getName());
				childNode.addProperty("Type", "Word");
				childNode.setData(getData(parent));
				diagram.addNode(childNode);
				diagram.addEdge(wordNode,msg.get(relation.getKind(), locale),childNode);
				
				Language language = modelService.getParent(parent, Language.class);
				if (language!=null) {
					Node langNode = new Node();
					langNode.setId(language.getId());
					langNode.setTitle(language.getName());
					langNode.addProperty("Type", "Language");
					diagram.addNode(langNode);
					diagram.addEdge(childNode,langNode);
				}
			}
			
			Language language = modelService.getParent(word, Language.class);
			if (language!=null) {
				Node langNode = new Node();
				langNode.setId(language.getId());
				langNode.setTitle(language.getName());
				langNode.addProperty("Type", "Language");
				diagram.addNode(langNode);
				diagram.addEdge(wordNode,langNode);
			}
			LexicalCategory category = modelService.getParent(word, LexicalCategory.class);
			if (category!=null) {
				Node categoryNode = new Node();
				categoryNode.setId(category.getId());
				categoryNode.setTitle(category.getName());
				categoryNode.addProperty("Type", "Lexical category");
				diagram.addNode(categoryNode);
				diagram.addEdge(wordNode,"Category",categoryNode);

				LexicalCategory superCategory = modelService.getParent(category, Relation.KIND_STRUCTURE_SPECIALIZATION, LexicalCategory.class);
				if (superCategory!=null) {
					Node superNode = new Node();
					superNode.setId(superCategory.getId());
					superNode.setTitle(superCategory.getName());
					superNode.addProperty("Type", "Lexical category");
					diagram.addNode(superNode);
					diagram.addEdge(superNode,"Specialization",categoryNode);
				}
			}
			User user = modelService.getChild(word, Relation.KIND_COMMON_ORIGINATOR, User.class);
			if (user!=null) {
				Node userNode = new Node();
				userNode.setId(user.getId());
				userNode.setTitle(user.getName());
				userNode.addProperty("Type", "User");
				diagram.addNode(userNode);
				diagram.addEdge(wordNode,"Originator",userNode);
			}
		}
		return diagram;
	}
	
	public void createWord(String languageCode,String category,String text, UserSession session) throws ModelException, IllegalRequestException {
		if (StringUtils.isBlank(languageCode)) {
			throw new IllegalRequestException("No language provided");
		}
		if (StringUtils.isBlank(text)) {
			throw new IllegalRequestException("No text provided");
		}
		LexicalCategory lexicalCategory = null;
		if (StringUtils.isNotBlank(category)) {
			lexicalCategory = languageService.getLexcialCategoryForCode(category);
			if (lexicalCategory==null) {
				throw new IllegalRequestException("Unsupported category ("+category+")");
			}
		}
		Language language = languageService.getLanguageForCode(languageCode);
		if (language==null) {
			throw new IllegalRequestException("Unsupported language ("+languageCode+")");
		}
		Query<Word> query = Query.of(Word.class).withField(Word.TEXT_FIELD, text).withParent(language);
		if (lexicalCategory!=null) {
			query.withParent(lexicalCategory);
		}
		List<Word> list = modelService.list(query);
		if (list.size()==0) {
			Word word = new Word();
			word.setText(text);
			modelService.createItem(word, session);
			securityService.grantPublicPrivileges(word, true, true, false);
			Relation languageRelation = modelService.createRelation(language, word, session);
			securityService.grantPublicPrivileges(languageRelation, true, true, false);
			if (lexicalCategory!=null) {
				Relation categoryRelation = modelService.createRelation(lexicalCategory, word, session);
				securityService.grantPublicPrivileges(categoryRelation, true, true, false);
			}
			ensureOriginator(word,session.getUser());
		}
	}

	public void deleteRelation(long id, UserSession session) throws IllegalRequestException, SecurityException, ModelException {
		Relation relation = modelService.getRelation(id);
		if (relation==null) {
			throw new IllegalRequestException("Relation not found (id="+id+")");
		}
		modelService.deleteRelation(relation, session);
	}

	public void relateWords(long parentId,String kind, long childId, UserSession session) throws ModelException, IllegalRequestException, SecurityException {
		Word parentWord = modelService.get(Word.class, parentId, session);
		Word childWord = modelService.get(Word.class, childId, session);
		if (parentWord==null || childWord==null) {
			throw new IllegalRequestException("Word not found");
		}
		Set<String> allowed = Sets.newHashSet(Relation.KIND_SEMANTICS_EQUIVALENT, Relation.KIND_SEMANTICS_ANTONYMOUS, Relation.KIND_SEMANTICS_SYNONYMOUS,Relation.KIND_SEMANTICS_ANALOGOUS, Relation.KIND_SEMANTICS_MORPHEME);
		if (!allowed.contains(kind)) {
			throw new IllegalRequestException("Illegal relation: "+kind);
		}
		
		Relation relation = modelService.getRelation(parentWord, childWord, kind);
		if (relation==null) {
			Relation newRelation = modelService.createRelation(parentWord, childWord, kind, session);
			securityService.grantPublicPrivileges(newRelation, true, true, false);
			
		}
	}
	
	public void changeLanguage(long wordId, String languageCode, UserSession session) throws ModelException, IllegalRequestException, SecurityException, ContentNotFoundException {
		Word word = getWord(wordId, session);
		if (word==null) {
			throw new ContentNotFoundException(Word.class, wordId);
		}
		changeLanguage(word, languageCode, session.getUser(), session);
	}
	
	public void changeLanguage(Word word, String languageCode, User originator, Privileged privileged) throws ModelException, IllegalRequestException, SecurityException {
		Language language = null;
		if (languageCode!=null) {
			language = languageService.getLanguageForCode(languageCode);
			if (language==null) {
				throw new IllegalRequestException("Unsupported language ("+languageCode+")");
			}
		}
		List<Relation> parents = modelService.getParentRelations(word, Language.class);
		modelService.deleteRelations(parents, privileged);
		if (language!=null) {
			Relation relation = modelService.createRelation(language, word, privileged);
			securityService.grantPublicPrivileges(relation, true, true, false);
		}
		ensureOriginator(word,originator);
	}

	public void changeCategory(long wordId, String category, UserSession session) throws ModelException, IllegalRequestException, SecurityException {
		Word word = getWord(wordId, session);
		changeCategory(word, category, session.getUser(), session);
	}

	public void changeCategory(Word word, String category, User originator, Privileged privileged) throws IllegalRequestException, ModelException, SecurityException {
		LexicalCategory lexicalCategory = languageService.getLexcialCategoryForCode(category);
		if (lexicalCategory==null) {
			throw new IllegalRequestException("Unsupported category ("+category+")");
		}
		List<Relation> parents = modelService.getParentRelations(word, LexicalCategory.class);
		modelService.deleteRelations(parents, privileged);
		Relation categoryRelation = modelService.createRelation(lexicalCategory, word, privileged);
		securityService.grantPublicPrivileges(categoryRelation, true, true, false);
		ensureOriginator(word,originator);		
	}

	
	
	public void deleteWord(long wordId, UserSession session) throws ModelException, IllegalRequestException, SecurityException {
		Word word = getWord(wordId, session);
		modelService.deleteEntity(word, session);
	}

	private Word getWord(long wordId, UserSession session) throws ModelException, IllegalRequestException {
		Word word = modelService.get(Word.class, wordId, session);
		if (word==null) {
			throw new IllegalRequestException("Word not found (id="+wordId+")");
		}
		return word;
	}

	private void ensureOriginator(Word word, User originator) throws ModelException {
		User user = modelService.getChild(word, Relation.KIND_COMMON_ORIGINATOR, User.class);
		if (user==null) {
			modelService.createRelation(word, originator, Relation.KIND_COMMON_ORIGINATOR, originator);
		}
	}

	public void addToPostponed(Word word) throws ModelException {
		User admin = modelService.getUser("admin");
		Pile pile = pileService.getOrCreateGlobalPile("words.postponed", admin);
		Relation relation = modelService.getRelation(pile, word);
		if (relation==null) {
			modelService.createRelation(pile, word, admin);
		}
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
	
	public void setPileService(PileService pileService) {
		this.pileService = pileService;
	}

}