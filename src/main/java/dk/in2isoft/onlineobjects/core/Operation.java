package dk.in2isoft.onlineobjects.core;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;

import dk.in2isoft.onlineobjects.core.events.ModelEventType;
import dk.in2isoft.onlineobjects.model.Item;
import dk.in2isoft.onlineobjects.model.User;

public class Operation {

	private Session session;
	private List<Pair<ModelEventType, Object>> events;

	public Operation(Session session) {
		this.session = session;
		this.events = new ArrayList<>();
	}

	public Session getSession() {
		return this.session;
	}

	public void addChangeEvent(Item item) {
		events.add(Pair.of(ModelEventType.update, item));
	}

	public List<Pair<ModelEventType, Object>> getEvents() {
		return events;
	}

	public void addCreateEvent(Item item) {
		events.add(Pair.of(ModelEventType.create, item));
	}

	public void addPrivilegesRemoved(Item item, List<User> users) {
		events.add(Pair.of(ModelEventType.privilegesRemoved, Pair.of(item, users)));
	}

	public void addDeleteEvent(Item item) {
		events.add(Pair.of(ModelEventType.delete, item));
	}
}

