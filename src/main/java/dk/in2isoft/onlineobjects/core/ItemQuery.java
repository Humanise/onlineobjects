package dk.in2isoft.onlineobjects.core;

import org.hibernate.Session;

public interface ItemQuery<T> {

	public org.hibernate.query.Query<T> createItemQuery(Session session);
	public org.hibernate.query.Query<Long> createCountQuery(Session session);
}
