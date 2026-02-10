package org.onlineobjects.apps.developer;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.beanutils.PropertyUtils;
import org.onlineobjects.core.Model;
import org.springframework.beans.factory.annotation.Autowired;

import dk.in2isoft.commons.jsf.AbstractView;
import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.Pair;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.Privilege;
import dk.in2isoft.onlineobjects.model.Property;
import dk.in2isoft.onlineobjects.ui.Request;
import dk.in2isoft.onlineobjects.ui.data.SimpleEntityPerspective;

public class BrowseView extends AbstractView {

	@Autowired
	protected Model model;

	private Entity entity;
	private List<RelationPerspective> relationsFrom = new ArrayList<>();
	private List<RelationPerspective> relationsTo = new ArrayList<>();
	private List<Pair<String, String>> properties = new ArrayList<>();

	private List<PrivilegeView> privileges;

	@Override
	protected void before(Request request) throws Exception {

		Operator operator = model.newAdminOperator();

		Long id = request.getOptionalId().orElseGet(() -> request.getSession().getIdentity());
		entity = model.get(id, operator).orElseThrow();
		Locale locale = request.getLocale();

		model.find().relations(operator).from(entity).stream(50).forEach(relation -> {
			RelationPerspective pers = new RelationPerspective();
			pers.entity = EntityPerspective.from(relation.getTo(), locale);
			pers.kind = relation.getKind();
			relationsFrom.add(pers);
		});

		model.find().relations(operator).to(entity).stream(50).forEach(relation -> {
			RelationPerspective pers = new RelationPerspective();
			pers.entity = EntityPerspective.from(relation.getFrom(), locale);
			pers.kind = relation.getKind();
			relationsTo.add(pers);
		});
		PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(entity);
		for (PropertyDescriptor descriptor : descriptors) {
		    String propertyName = descriptor.getName();
		    Object value = PropertyUtils.getProperty(entity, propertyName);
		    properties.add(Pair.of(propertyName, value == null ? "<null>" : value.toString()));
		}

		entity.getProperties().stream().forEach(p -> {
			properties.add(Pair.of(p.getKey(), getValue(p)));
		});

		this.privileges = model.getPrivileges(entity, getRequest()).stream().map(p -> {
			return new PrivilegeView(p, find(p.getSubject()));
		}).toList();
	}

	public String getTitle() {
		return entity.getName();
	}

	public String getType() {
		return entity.getClass().getSimpleName();
	}

	public List<Pair<String, String>> getProperties() {
		return properties;
	}

	public List<PrivilegeView> getPrivileges() {
		return this.privileges;
	}

	public List<RelationPerspective> getRelationsFrom() {
		return relationsFrom;
	}

	public List<RelationPerspective> getRelationsTo() {
		return relationsTo;
	}

	private Entity find(long id) {
		try {
			return model.get(id, getRequest()).orElse(null);
		} catch (ModelException e) {
			return null;
		}
	}

	private String getValue(Property p) {
		if (p.getDateValue() != null) {
			return p.getDateValue().toString();
		}
		else if (p.getDoubleValue() != null) {
			return p.getDoubleValue().toString();
		}
		return p.getValue();
	}

	public static class PrivilegeView {
		private Privilege privilege;
		private Entity other;

		public PrivilegeView(Privilege privilege, Entity subject) {
			this.privilege = privilege;
			this.other = subject;
		}

		public long getId() {
			return privilege.getId();
		}

		public boolean isAlter() {
			return privilege.isAlter();
		}

		public boolean isDelete() {
			return privilege.isDelete();
		}

		public boolean isReference() {
			return privilege.isReference();
		}

		public boolean isView() {
			return privilege.isView();
		}

		public SimpleEntityPerspective getPrivileged() {
			return SimpleEntityPerspective.create(other);
		}

	}

	public static class RelationPerspective {
		EntityPerspective entity;
		String kind;

		public EntityPerspective getEntity() {
			return entity;
		}

		public String getKind() {
			return kind;
		}
	}

	public static class EntityPerspective {
		String name;
		String type;
		long id;
		String href;

		public String getName() {
			return name;
		}

		public String getType() {
			return type;
		}

		public long getId() {
			return id;
		}

		public String getHref() {
			return href;
		}

		public static EntityPerspective from(Entity entity, Locale locale) {
			EntityPerspective perspective = new EntityPerspective();
			perspective.id = entity.getId();
			perspective.name = Strings.isBlank(entity.getName()) ? "<No name>" : entity.getName();
			perspective.type = entity.getClass().getSimpleName();
			perspective.href = "/browse?id=" + entity.getId();
			return perspective;
		}

		public static EntityPerspective from(Class<? extends Entity> type, long id, Locale locale) {
			EntityPerspective perspective = new EntityPerspective();
			perspective.id = id;
			perspective.name = "?";
			perspective.type = type.getSimpleName();
			perspective.href = "/browse?id=" + id;
			return perspective;
		}
	}
}
