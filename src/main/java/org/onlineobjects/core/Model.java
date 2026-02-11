package org.onlineobjects.core;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.annotation.ApplicationScope;

import dk.in2isoft.onlineobjects.core.Finder;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.Item;
import dk.in2isoft.onlineobjects.model.Privilege;

@ApplicationScope
public class Model {

	@Autowired
	protected ModelService modelService;

	public <T extends Entity> Optional<T> get(Class<T> type, Long id, Operator operator) throws ModelException {
		return modelService.getOptional(type, id, operator);
	}

	public <T extends Entity> Optional<T> get(Long id, Operator request) throws ModelException {
		return (Optional<T>) Optional.ofNullable(modelService.get(Entity.class, id, request));
	}

	public List<Privilege> getPrivileges(Item item, Operator operator) {
		return modelService.getPrivileges(item, operator);
	}

	public Finder find() {
		return modelService.find();
	}

	public Operator newAdminOperator() {
		return modelService.newAdminOperator();
	}

	public <T extends Entity> List<T> list(Query<T> query, Operator operator) {
		return modelService.list(query, operator);
	}
}
