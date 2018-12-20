package dk.in2isoft.onlineobjects.core;

import org.hibernate.Session;

public interface PairQuery<T,U> {

	public org.hibernate.query.Query<?> createItemQuery(Session session);
	public org.hibernate.query.Query<Long> createCountQuery(Session session);
}
