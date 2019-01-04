package dk.in2isoft.onlineobjects.core;

import org.hibernate.query.Query;
import org.hibernate.Session;

public interface ModelQuery {

	public Query<?> createItemQuery(Session session);
}
