package org.onlineobjects.modules.suggestion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;

import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.model.Question;
import dk.in2isoft.onlineobjects.model.Statement;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.modules.caching.CacheEntry;
import dk.in2isoft.onlineobjects.modules.caching.CacheService;
import dk.in2isoft.onlineobjects.modules.knowledge.KnowledgeService;
import dk.in2isoft.onlineobjects.services.SemanticService;
import dk.in2isoft.onlineobjects.ui.data.SimpleEntityPerspective;
import opennlp.tools.doccat.DoccatFactory;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.util.TrainingParameters;

public class KnowledgeSuggester {

	private static final Logger log = LogManager.getLogger(KnowledgeSuggester.class);
	private static final Collection<Class<?>> types = Lists.newArrayList(Question.class, Statement.class);
	private ModelService modelService;
	private KnowledgeService knowledgeService;
	private SemanticService semanticService;
	private CacheService cacheService;
	
	private enum ModelState {SYNCHED, INITIATING, UPDATING, IMPRECISE, EMPTY}
	
	private Map<Long, ModelState> userToModelFreshnessMapping = new HashMap<>();
	/*
	private DoccatModel getQuestionModel(Operator operator) throws EndUserException {
		return cacheService.cache(operator.getIdentity(), operator, DoccatModel.class, () -> {
			userToModelFreshnessMapping.put(operator.getIdentity(), ModelState.INITIATING);
			return buildModel(operator);
		});
	}*/

	public void addUser(Privileged user) {
		if (!userToModelFreshnessMapping.containsKey(user.getIdentity())) {
			userToModelFreshnessMapping.put(user.getIdentity(), ModelState.EMPTY);
		}
	}

	public void removeUser(Privileged user) {
		userToModelFreshnessMapping.remove(user.getIdentity());
	}
	
	private Optional<DoccatModel> getExistingQuestionModel(Operator operator) throws EndUserException {
		return cacheService.get(operator.getIdentity(), operator, DoccatModel.class);
	}

	private DoccatModel replaceQuestionModel(Operator operator) throws EndUserException {
		return cacheService.replace(operator.getIdentity(), operator, DoccatModel.class, () -> {
			userToModelFreshnessMapping.put(operator.getIdentity(), ModelState.UPDATING);
			return buildModel(operator);
		});
	}

	private CacheEntry<DoccatModel> buildModel(Operator operator) throws EndUserException, IOException {
		CacheEntry<DoccatModel> entry = new CacheEntry<>();
		entry.setValue(newModel(operator));
		entry.setPrivileged(operator.getIdentity());
		entry.setId(operator.getIdentity());
		entry.setTypes(types);
		userToModelFreshnessMapping.put(operator.getIdentity(), ModelState.SYNCHED);
		log.info("Built new model for user with id = %", operator.getIdentity());
		return entry;
	}

	private DoccatModel newModel(Operator operator) throws EndUserException, IOException {
		DoccatFactory factory = new DoccatFactory();
		QuestionTrainingStream stream = new QuestionTrainingStream(operator, modelService, knowledgeService, semanticService);
		DoccatModel model = DocumentCategorizerME.train("en", stream, new TrainingParameters(), factory);
		return model;
	}

	public SuggestionsCategory suggestQuestion(Statement statement, Operator operator) throws EndUserException {
		SuggestionsCategory category = new SuggestionsCategory();
		Optional<DoccatModel> possibleModel = getExistingQuestionModel(operator);
		if (!possibleModel.isPresent()) {
			category.setDirty(true);
			userToModelFreshnessMapping.put(operator.getIdentity(), ModelState.EMPTY);
			return category;
		}
		category.setDirty(isDirty(operator));
		
		String text = statement.getText();
		String[] tokens = semanticService.getTokensAsString(text, Locale.ENGLISH);

		DoccatModel model = possibleModel.get();
		DocumentCategorizerME categorizer = new DocumentCategorizerME(model);
		double[] outcomes = categorizer.categorize(tokens);
		List<Suggestion> results = new ArrayList<>();
		for (int i = 0; i < outcomes.length; i++) {
			double outcome = outcomes[i];
			long id = Long.parseLong(categorizer.getCategory(i));
			try {
				Question question = modelService.get(Question.class, id, operator);
				if (question != null) {
					Suggestion suggestion = new Suggestion();
					suggestion.setDescription(question.getText());
					suggestion.setTarget(SimpleEntityPerspective.create(statement));
					suggestion.setEntity(SimpleEntityPerspective.create(question));
					suggestion.setStrength(outcome);
					results.add(suggestion);
				}
			} catch (ModelException e) {
				log.error(e.getMessage(), e);
			}
		}
		results = results.stream().sorted((a, b) -> {
			return Double.compare(b.getStrength(), a.getStrength());
		}).collect(Collectors.toList());
		
		category.setSuggestions(results);
		return category;
	}

	private boolean isDirty(Operator operator) {
		return ModelState.SYNCHED.equals(userToModelFreshnessMapping.get(operator.getIdentity()));
	}

	public void invalidateEverything() {
		for (Entry<Long, ModelState> entry : userToModelFreshnessMapping.entrySet()) {
			entry.setValue(ModelState.IMPRECISE);
		}
		log.info("Invalidated everything");
	}

	public void beat() {
		for (Entry<Long, ModelState> entry : userToModelFreshnessMapping.entrySet()) {
			if (entry.getValue() == ModelState.IMPRECISE || entry.getValue() == ModelState.EMPTY) {
				Operator adminOperator = modelService.newAdminOperator();
				try {
					Optional<User> userMaybe = modelService.getOptional(User.class, entry.getKey(), adminOperator);
					if (userMaybe.isPresent()) {
						User user = userMaybe.get();
						replaceQuestionModel(adminOperator.as(user));
						return;
					}
				} catch (EndUserException e) {
					log.error(e.getMessage(), e);
				} finally {
					adminOperator.commit();
				}
			}
		}
	}

	// Wiring
	
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	public void setCacheService(CacheService cacheService) {
		this.cacheService = cacheService;
	}

	public void setSemanticService(SemanticService semanticService) {
		this.semanticService = semanticService;
	}

	public void setKnowledgeService(KnowledgeService knowledgeService) {
		this.knowledgeService = knowledgeService;
	}

}
