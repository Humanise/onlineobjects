package dk.in2isoft.onlineobjects.modules.surveillance;

import org.hibernate.Session;
import org.hibernate.query.Query;

import dk.in2isoft.onlineobjects.core.ItemQuery;
import dk.in2isoft.onlineobjects.model.LogEntry;

public class LogQuery implements ItemQuery<LogEntry> {
	
	private int page = 0;
	private int size = 100;
	
	private String getHQL(boolean count) {
		return (count ? "select count(obj.id)" : "select obj") + " from " + LogEntry.class.getCanonicalName() + " as obj" + (count ? "" : " order by obj.id desc"); 
	}


	@Override
	public Query<LogEntry> createItemQuery(Session session) {
		String hql = getHQL(false);
		Query<LogEntry> query = session.createQuery(hql, LogEntry.class);
		query.setFirstResult(page * size);
		query.setMaxResults(size);
		return query;
	}

	@Override
	public Query<Long> createCountQuery(Session session) {
		String hql = getHQL(true);
		return session.createQuery(hql, Long.class);
	}

	public LogQuery withSize(int size) {
		this.size = size;
		return this;
	}
	
	public LogQuery withPage(int page) {
		this.page = page;
		return this;
	}
}
