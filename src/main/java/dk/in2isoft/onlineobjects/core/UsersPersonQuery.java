package dk.in2isoft.onlineobjects.core;

import org.hibernate.Session;
import org.hibernate.query.Query;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.commons.util.HQLBuilder;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.Person;
import dk.in2isoft.onlineobjects.model.Privilege;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.User;

public class UsersPersonQuery implements PairQuery<User, Person> {

	private String username;
	private String[] words;
	private int pageNumber;
	private int pageSize;
	private Class<? extends Entity> childClass;
	private boolean publicView;

	public Query<Long> createCountQuery(Session session) {
		HQLBuilder hql = new HQLBuilder().select("count(person)");
		build(hql, true);
		Query<Long> q = session.createQuery(hql.toString(), Long.class);
		setParams(true, q);
		return q;
	}

	public Query<?> createItemQuery(Session session) {
		HQLBuilder hql = new HQLBuilder().select("user","person");
		build(hql, false);
		Query<?> q = session.createQuery(hql.toString());
		setParams(false, q);
		return q;
	}

	private void build(HQLBuilder hql, boolean ignorePaging) {
		hql.from(User.class,"user");
		hql.from(Person.class,"person");
		hql.from(Relation.class,"rel");
		if (publicView) {
			hql.from(Privilege.class,"userPrivilege");
			hql.from(User.class,"publicUser");
		}
		hql.where("rel.to=person");
		hql.where("rel.from=user");
		hql.where("rel.kind='" + Relation.KIND_SYSTEM_USER_SELF + "'");

		if (username!=null) {
			hql.where("user.username=:username");
		}
		if (Strings.isDefined(words)) {
			for (int i = 0; i < words.length; i++) {
				hql.where("(lower(person.name) like lower(:word" + i + ") or lower(user.name) like lower(:word" + i + "))");
			}
		}
		if (childClass!=null) {
			hql.where(" user.id = some ( select priv.subject from Privilege as priv,"+childClass.getName()+" as x where priv.object=x.id)");
		}
		if (publicView) {
			hql.where(" user.id = userPrivilege.object and userPrivilege.subject=publicUser.id and publicUser.username='public' and userPrivilege.view=true");
		}
		if (!ignorePaging) {
			hql.orderBy("person.name");
		}
	}

	private void setParams(boolean ignorePaging, Query<?> q) {
		if (pageSize > 0 && !ignorePaging) {
			q.setMaxResults(pageSize);
			q.setFirstResult(pageNumber * pageSize);
		}
		if (username!=null) {
			q.setParameter("username", username);
		}
		if (Strings.isDefined(words)) {
			for (int i = 0; i < words.length; i++) {
				String word = words[i];
				q.setParameter("word" + i, "%" + word + "%");
			}
		}
	}

	public UsersPersonQuery withUsername(String username) {
		this.username = username;
		return this;
	}

	public UsersPersonQuery withPublicView() {
		this.publicView = true;
		return this;
	}

	public UsersPersonQuery withWords(String query) {
		if (Strings.isNotBlank(query)) {
			words = Strings.getWords(query);
		}
		return this;
	}

	public UsersPersonQuery withPaging(int pageNumber, int pageSize) {
		this.pageNumber = pageNumber;
		this.pageSize = pageSize;
		return this;
	}

	public UsersPersonQuery withUsersChildren(Class<? extends Entity> usersChildClass) {
		this.childClass = usersChildClass;
		return this;
	}

}
