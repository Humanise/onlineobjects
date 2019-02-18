package dk.in2isoft.onlineobjects.core;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;

import dk.in2isoft.onlineobjects.core.events.ModelEventType;
import dk.in2isoft.onlineobjects.model.Item;

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
}

