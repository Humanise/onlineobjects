package dk.in2isoft.onlineobjects.modules.inbox;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.core.events.AnyModelChangeListener;
import dk.in2isoft.onlineobjects.core.events.EventService;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.Item;
import dk.in2isoft.onlineobjects.model.Pile;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.User;

public class InboxService implements InitializingBean {

	private static Logger log = LogManager.getLogger(InboxService.class);

	private ModelService modelService;
	private SecurityService securityService;

	private EventService eventService;

	private Map<Long,Integer> counts;

	public InboxService() {
		super();
		counts = new HashMap<Long, Integer>();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		eventService.addModelEventListener(new AnyModelChangeListener() {
			@Override
			public void itemWasChanged(Item item) {
				counts.clear();
			}
		});
	}

	public Pile getOrCreateInbox(User user, Operator operator) throws ModelException, SecurityException {
		operator = operator.as(user);
		Query<Pile> query = Query.after(Pile.class).from(user, Relation.KIND_SYSTEM_USER_INBOX);
		if (!securityService.isAdminUser(user)) {
			query.as(user);
		}
		Pile inbox = modelService.getFirst(query, operator);
		if (inbox==null) {
			inbox = new Pile();
			inbox.setName("Inbox for "+user.getUsername());
			modelService.create(inbox, operator);
			modelService.createRelation(user, inbox, Relation.KIND_SYSTEM_USER_INBOX, operator);
		}
		return inbox;
	}

	public void add(User user, Entity entity, Operator operator) throws ModelException, SecurityException {
		if (!modelService.getRelation(user, entity, operator).isPresent()) {
			modelService.createRelation(getOrCreateInbox(user, operator), entity, operator);
		}
	}

	public int getCount(User user, Operator operator) throws ModelException, SecurityException {
		if (counts.containsKey(user.getId())) {
			return counts.get(user.getId());
		}

		// TODO Optimize this by caching id=count
		Pile inbox = getOrCreateInbox(user, operator);
		Query<Entity> query = Query.after(Entity.class).from(inbox).as(user);
		//List<Entity> list = modelService.list(query);
		int count = modelService.count(query, operator).intValue();
		counts.put(user.getId(), count);
		return count;
	}

	public int getCountSilently(User user, Operator operator) {
		if (user==null) {
			log.error("The user is null, will silently rebort zero");
			return 0;
		}
		try {
			return getCount(user, operator);
		} catch (EndUserException e) {
			log.error("Unable to get inbox count for user="+user+", will silently rebort zero",e);
			return 0;
		}
	}

	public boolean remove(User user, long id, Operator operator) throws ModelException, SecurityException {
		Pile inbox = getOrCreateInbox(user, operator);
		Entity entity = modelService.get(Entity.class, id, operator);
		Optional<Relation> relation = modelService.getRelation(inbox, entity, operator);
		if (!relation.isPresent()) {
			return false;
		}
		modelService.delete(relation.get(), operator);
		return true;
	}

	// Wiring...

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	public void setEventService(EventService eventService) {
		this.eventService = eventService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
}
