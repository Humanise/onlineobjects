package dk.in2isoft.onlineobjects.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.Query;

import dk.in2isoft.commons.lang.Code;
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
	
	public RelationQuery(ModelService modelService, SecurityService securityService) {
		this.modelService = modelService;
		this.securityService = securityService;
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
	
	public String getHQL() {
		StringBuilder hql = new StringBuilder("select rel from Relation as rel");
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
		return hql.toString();
	}
	
	private void decorate(Query query) {
		if (this.id!=null) {
			query.setLong("id", this.id);
		}
		if (this.fromEntity!=null) {
			query.setLong("from", this.fromEntity.getId());
		}
		if (this.toEntity!=null) {
			query.setLong("to", this.toEntity.getId());
		}
		if (this.kind!=null) {
			query.setString("kind", this.kind);
		}
		if (!privileged.isEmpty()) {
			List<Long> privIds = privileged.stream().map(priv -> priv.getIdentity()).collect(Collectors.toList());
			query.setParameterList("privileged", privIds);
		}
	}

	public Optional<Relation> first() {
		Query query = modelService.createQuery(getHQL());
		decorate(query);
		query.setMaxResults(1);
		List<Relation> list = Code.castList(query.list());
		return list.isEmpty() ? Optional.empty() : Optional.of(ModelService.getSubject(list.get(0)));
	}

	public Stream<Relation> stream(int max) {
		Query query = modelService.createQuery(getHQL());
		decorate(query);
		query.setMaxResults(max);
		List<Relation> list = Code.castList(query.list());
		return list.stream();
	}

	public Stream<Relation> stream() {
		Query query = modelService.createQuery(getHQL());
		decorate(query);
		List<Relation> list = Code.castList(query.list());
		return list.stream();
	}

	public List<Relation> list() {
		Query query = modelService.createQuery(getHQL());
		decorate(query);
		List<Relation> list = Code.castList(query.list());
		return list;
	}
}
