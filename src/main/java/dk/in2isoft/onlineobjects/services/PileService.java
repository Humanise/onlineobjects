package dk.in2isoft.onlineobjects.services;

import java.util.Optional;

import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.Pile;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.User;

public class PileService {

	private ModelService modelService;
	
	public Pile getOrCreateGlobalPile(String key, Operator operator) throws ModelException, SecurityException {
		Query<Pile> query = Query.after(Pile.class).withCustomProperty(Pile.PROPERTY_KEY, key);
		Pile first = modelService.search(query, operator).getFirst();
		if (first==null) {
			first = new Pile();
			first.addProperty(Pile.PROPERTY_KEY, key);
			first.setName(key);
			modelService.create(first, operator);
		}
		return first;
	}
	
	public Pile getOrCreatePileByKey(String key, User user, Operator operator) throws ModelException, SecurityException {
		operator = operator.as(user);
		Query<Pile> query = Query.after(Pile.class).withCustomProperty(Pile.PROPERTY_KEY, key).from(user);
		Pile first = modelService.search(query, operator).getFirst();
		if (first==null) {
			first = new Pile();
			first.addProperty(Pile.PROPERTY_KEY, key);
			first.setName(key);
			modelService.create(first, operator);
			modelService.createRelation(user, first, operator);
		}
		return first;
	}

	public Pile getOrCreatePileByRelation(User user, String relationKind, Operator operator) throws ModelException, SecurityException {
		operator = operator.as(user);
		Query<Pile> query = Query.after(Pile.class).from(user, relationKind).as(user);
		Pile pile = modelService.getFirst(query, operator);
		if (pile==null) {
			pile = new Pile();
			pile.setName(relationKind + " for "+user.getUsername());
			modelService.create(pile, operator);
			modelService.createRelation(user, pile, relationKind, operator);
		}
		return pile;
	}

	public Pile getOrCreatePileByRelation(User user, Operator operator, String relationKind) throws ModelException, SecurityException {
		Query<Pile> query = Query.after(Pile.class).from(user, relationKind).as(user);
		Pile pile = modelService.getFirst(query, operator);
		if (pile==null) {
			pile = new Pile();
			pile.setName(relationKind + " for "+user.getUsername());
			modelService.create(pile, operator);
			modelService.createRelation(user, pile, relationKind, operator);
		}
		return pile;
	}

	public void addOrRemoveFromPile(User user, String relationKind, Entity enity, boolean add, Operator operator) throws ModelException, SecurityException {
		operator = operator.as(user);
		Pile pil = this.getOrCreatePileByRelation(user, relationKind, operator);
		Optional<Relation> relation = modelService.getRelation(pil, enity, operator);
		if (add && !relation.isPresent()) {
			modelService.createRelation(pil, enity, operator);
		} else if (!add && relation.isPresent()) {
			modelService.delete(relation.get(), operator);
		}
		
	}
	
	// Wiring...

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
}
