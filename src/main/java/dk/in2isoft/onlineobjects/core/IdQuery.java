package dk.in2isoft.onlineobjects.core;

import org.hibernate.Session;

public interface IdQuery {

	public org.hibernate.query.Query<Long> createIdQuery(Session session);
}
