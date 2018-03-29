package dk.in2isoft.onlineobjects.core;

import org.hibernate.Query;
import org.hibernate.Session;

import dk.in2isoft.onlineobjects.model.Client;
import dk.in2isoft.onlineobjects.model.Property;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.User;

public class UserQuery implements ItemQuery<User> {

	private String secret;
	
	private static String base = "from " + User.class.getName() + " as user, " +
			Client.class.getName() + " as client, " + 
			Relation.class.getName() + " as rel " +
			" inner join client.properties as prop" +
			" where rel.from = user and rel.to = client "+
			" and prop.key='" + Property.KEY_AUTHENTICATION_SECRET + "' and prop.value = :secret"; 
	
	@Override
	public Query createItemQuery(Session session) {
		String hql = "select user " + base;
		Query query = session.createQuery(hql);
		query.setString("secret", secret);
		return query;
	}

	@Override
	public Query createCountQuery(Session session) {
		String hql = "select count(user.id) " + base;
		Query query = session.createQuery(hql);
		query.setString("secret", secret);
		return query;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}
}
