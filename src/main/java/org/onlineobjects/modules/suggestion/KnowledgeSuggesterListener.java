package org.onlineobjects.modules.suggestion;

import dk.in2isoft.onlineobjects.core.Ability;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.Results;
import dk.in2isoft.onlineobjects.core.events.ModelEventListener;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.InternetAddress;
import dk.in2isoft.onlineobjects.model.Property;
import dk.in2isoft.onlineobjects.model.Question;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.Statement;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.services.InitializingService;

public class KnowledgeSuggesterListener implements ModelEventListener, InitializingService {
	private KnowledgeSuggester knowledgeSuggester;
	private ModelService modelService;

	@Override
	public void initialize() {

		try (Operator adminOperator = modelService.newAdminOperator()) {
			Query<User> query = Query.after(User.class).withCustomProperty(Property.KEY_ABILITY, Ability.earlyAdopter.name());
			try (Results<User> scroll = modelService.scroll(query, adminOperator)) {
		 		while (scroll.next()) {
		 			User user = scroll.get();
		 			knowledgeSuggester.addUser(user);
		 		}
			}
		}
	}

	@Override
	public void entityWasCreated(Entity entity) {
		checkUser(entity);
		invalidateEverything();
	}

	private void checkUser(Entity entity) {
		if (entity instanceof User) {
			User user = (User) entity;
			if (user.hasAbility(Ability.earlyAdopter)) {
				knowledgeSuggester.addUser(user);
			}
		}
	}

	@Override
	public void entityWasUpdated(Entity entity) {
		checkUser(entity);
		invalidateEverything();
	}

	@Override
	public void entityWasDeleted(Entity entity) {
		if (entity instanceof User) {
			User user = (User) entity;
			knowledgeSuggester.removeUser(user);
		}
		if (entity instanceof Question) {
			invalidateEverything();
		}
		else if (entity instanceof Statement) {
			invalidateEverything();
		}
		else if (entity instanceof InternetAddress) {
			invalidateEverything();
		}
	}

	@Override
	public void relationWasCreated(Relation relation) {
		checkRelation(relation);
	}

	@Override
	public void relationWasUpdated(Relation relation) {
		checkRelation(relation);
	}

	@Override
	public void relationWasDeleted(Relation relation) {
		checkRelation(relation);
	}

	private void checkRelation(Relation relation) {
		if (relation.getFrom() instanceof Statement) {
			if (relation.getTo() instanceof Question) {
				if (Relation.ANSWERS.equals(relation.getKind())) {
					invalidateEverything();
				}
			}
		}
	}

	/*
	 * TODO: Only invalidate the correct user
	 */
	@Deprecated
	private void invalidateEverything() {
		knowledgeSuggester.invalidateEverything();
	}

	public void setKnowledgeSuggester(KnowledgeSuggester knowledgeSuggester) {
		this.knowledgeSuggester = knowledgeSuggester;
	}

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
}
