package dk.in2isoft.onlineobjects.core;

import org.hibernate.Session;
import org.hibernate.query.Query;

public interface ModelQuery {

	public Query<?> createItemQuery(Session session);
}
