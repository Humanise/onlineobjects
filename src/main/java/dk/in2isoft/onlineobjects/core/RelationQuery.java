package dk.in2isoft.onlineobjects.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.query.Query;
import org.hibernate.type.StandardBasicTypes;

import dk.in2isoft.commons.lang.Code;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.Relation;

public class RelationQuery {

	private Long id;
	private final List<Privileged> privileged = new ArrayList<>();
	private Entity fromEntity;
	private Entity toEntity;
	private Class<? extends Entity> toClass;
	private Class<? extends Entity> fromClass;
	private String kind;
	private ModelService modelService;
	private SecurityService securityService;
	private Operation operation;
	
	public RelationQuery(ModelService modelService, SecurityService securityService, Operation operation) {
		this.modelService = modelService;
		this.securityService = securityService;
		this.operation = operation;
	}

	public RelationQuery withId(Long id) {
		this.id = id;
		return this;
	}

	public RelationQuery withKind(String kind) {
		this.kind = kind;
		return this;
	}

	public RelationQuery from(Entity entity) {
		this.fromEntity = entity;
		return this;
	}

	public RelationQuery answers(Entity entity) {
		this.kind = Relation.ANSWERS;
		this.toEntity = entity;
		return this;
	}

	public RelationQuery answers(Class<? extends Entity> type) {
		withKind(Relation.ANSWERS);
		to(type);
		return this;
	}

	public RelationQuery to(Entity entity) {
		this.toEntity = entity;
		return this;
	}

	public RelationQuery to(Class<? extends Entity> type) {
		this.toClass = type;
		return this;
	}

	public RelationQuery from(Class<? extends Entity> type) {
		this.fromClass = type;
		return this;
	}
	
	
	protected RelationQuery as(Privileged privileged) {
		if (!securityService.isAdminUser(privileged)) {
			if (!securityService.isPublicUser(privileged)) {
				this.privileged.add(privileged);
			}
			this.privileged.add(securityService.getPublicUser());
		}
		return this;
	}
	
	private String getCountHQL() {
		StringBuilder hql = new StringBuilder("select count(distinct rel) from Relation as rel");
		buildHQL(hql);
		return hql.toString();
	}
	
	public String getHQL() {
		StringBuilder hql = new StringBuilder("select distinct rel from Relation as rel");
		buildHQL(hql);
		// Note some already rely on position ordering
		hql.append(" order by rel.position, rel.id");
		return hql.toString();
	}

	private void buildHQL(StringBuilder hql) {
		if (toClass!=null) {
			hql.append(", " + toClass.getSimpleName() + " as toClass");
		}
		if (fromClass!=null) {
			hql.append(", " + fromClass.getSimpleName() + " as fromClass");
		}
		if (!privileged.isEmpty()) {
			hql.append(", Privilege as relPriv");
			hql.append(", Privilege as fromPriv");
			hql.append(", Privilege as toPriv");
		}
		hql.append(" where rel.id > 0");
		if (id!=null) {
			hql.append(" and rel.id=:id");
		}
		if (fromEntity!=null) {
			hql.append(" and rel.from.id=:from");
		}
		if (toEntity!=null) {
			hql.append(" and rel.to.id=:to");
		}
		if (fromClass!=null) {
			hql.append(" and rel.from = fromClass");
		}
		if (toClass!=null) {
			hql.append(" and rel.to = toClass");
		}
		if (kind!=null) {
			hql.append(" and rel.kind=:kind");
		}
		if (!privileged.isEmpty()) {
			hql.append(" and relPriv.object=rel.id and relPriv.subject in (:privileged)");
			hql.append(" and toPriv.object=rel.to.id and toPriv.subject in (:privileged)");
			hql.append(" and fromPriv.object=rel.from.id and fromPriv.subject in (:privileged)");
		}
	}
	
	private void decorate(Query<?> query) {
		if (this.id!=null) {
			query.setParameter("id", this.id, StandardBasicTypes.LONG);
		}
		if (this.fromEntity!=null) {
			query.setParameter("from", this.fromEntity.getId(), StandardBasicTypes.LONG);
		}
		if (this.toEntity!=null) {
			query.setParameter("to", this.toEntity.getId(), StandardBasicTypes.LONG);
		}
		if (this.kind!=null) {
			query.setParameter("kind", this.kind, StandardBasicTypes.STRING);
		}
		if (!privileged.isEmpty()) {
			List<Long> privIds = privileged.stream().map(priv -> priv.getIdentity()).collect(Collectors.toList());
			query.setParameterList("privileged", privIds);
		}
	}

	private Query<Relation> getQuery() {
		Query<Relation> query = modelService.createQuery(getHQL(), Relation.class, operation.getSession());
		decorate(query);
		return query;
	}

	private Query<Long> getCountQuery() {
		Query<Long> query = modelService.createQuery(getCountHQL(), Long.class, operation.getSession());
		decorate(query);
		return query;
	}

	public Optional<Relation> first() {
		Query<Relation> query = getQuery();
		List<Relation> list = query.list();
		if (list.isEmpty()) return Optional.empty();
		Relation unique = (Relation) list.get(0);
		return unique==null ? Optional.empty() : Optional.of(ModelService.getSubject(unique));
	}

	public long count() {
		Query<Long> query = getCountQuery();
		return query.list().iterator().next();
	}

	public Stream<Relation> stream(int max) {
		Query<Relation> query = getQuery();
		query.setMaxResults(max);
		return query.stream();
	}

	public Stream<Relation> stream() {
		Query<Relation> query = getQuery();
		return query.stream();
	}

	public List<Relation> list() {
		Query<Relation> query = getQuery();
		List<Relation> list = Code.castList(query.list());
		return list;
	}
	
	public void delete(Operator operator) throws ModelException, SecurityException {
		for (Relation relation : list()) {			
			modelService.delete(relation, operator);			
		}
	}

	public boolean exists() {		
		return count() > 0;
	}
}
