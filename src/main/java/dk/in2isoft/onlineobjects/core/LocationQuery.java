package dk.in2isoft.onlineobjects.core;

import org.hibernate.query.Query;
import org.hibernate.type.DoubleType;
import org.hibernate.type.StringType;
import org.hibernate.Session;

import dk.in2isoft.commons.geo.GeoLatLng;
import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.Location;
import dk.in2isoft.onlineobjects.model.Relation;

public class LocationQuery<T extends Entity> implements PairQuery<Location, T> {

	private String[] words;
	private int pageNumber;
	private int pageSize;
	private Class<T> cls;
	private GeoLatLng northEast;
	private GeoLatLng southWest;

	public LocationQuery(Class<T> cls) {
		this.cls = cls;
	}

	public Query<Long> createCountQuery(Session session) {
		StringBuilder hql = new StringBuilder("select count(location) from ");
		return createQuery(session, hql, true);
	}

	public Query<?> createItemQuery(Session session) {
		StringBuilder hql = new StringBuilder("select location,entity from ");
		return createQuery(session, hql, false);
	}

	public Query createQuery(Session session, StringBuilder hql, boolean ignorePaging) {
		hql.append(Location.class.getName()).append(" as location");
		hql.append(",").append(cls.getName()).append(" as entity");
		hql.append(",").append(Relation.class.getName()).append(" as rel");
		hql.append(" where rel.to=entity and rel.from=location");
		if (Strings.isDefined(words)) {
			for (int i = 0; i < words.length; i++) {
				hql.append(" and (lower(entity.name) like lower(:word" + i
						+ ") or lower(location.name) like lower(:word" + i
						+ "))");
			}
		}
		if (northEast != null && southWest != null) {
			hql.append(" and location.latitude>:minLatitude and location.latitude<:maxLatitude");
			hql.append(" and location.longitude>:minLongitude and location.longitude<:maxLongitude");
		}
		if (!ignorePaging) {
			hql.append(" order by entity.name");
		}
		Query q = session.createQuery(hql.toString());
		if (pageSize > 0 && !ignorePaging) {
			q.setMaxResults(pageSize);
			q.setFirstResult(pageNumber * pageSize);
		}
		if (Strings.isDefined(words)) {
			for (int i = 0; i < words.length; i++) {
				String word = words[i];
				q.setParameter("word" + i, "%" + word + "%", StringType.INSTANCE);
			}
		}
		if (northEast != null && southWest != null) {
			q.setParameter("minLongitude", southWest.getLng(), DoubleType.INSTANCE);
			q.setParameter("maxLongitude", northEast.getLng(), DoubleType.INSTANCE);
			q.setParameter("minLatitude", southWest.getLat(), DoubleType.INSTANCE);
			q.setParameter("maxLatitude", northEast.getLat(), DoubleType.INSTANCE);
		}
		return q;
	}

	public LocationQuery<T> withWords(String query) {
		if (Strings.isNotBlank(query)) {
			words = Strings.getWords(query);
		}
		return this;
	}

	public LocationQuery<T> withPaging(int pageNumber, int pageSize) {
		this.pageNumber = pageNumber;
		this.pageSize = pageSize;
		return this;
	}

	public LocationQuery<T> withBounds(GeoLatLng northEast, GeoLatLng southWest) {
		this.northEast = northEast;
		this.southWest = southWest;
		return this;
	}

}
