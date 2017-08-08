package dk.in2isoft.onlineobjects.apps.front.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.InitializingBean;

import dk.in2isoft.commons.jsf.AbstractView;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.core.UserSession;
import dk.in2isoft.onlineobjects.core.exceptions.IllegalRequestException;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.Privilege;
import dk.in2isoft.onlineobjects.model.Property;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.ui.Request;
import dk.in2isoft.onlineobjects.ui.data.Option;

public class FrontEntityView extends AbstractView implements InitializingBean {

	private ModelService modelService;
	private SecurityService securityService;
	
	private String title;
	private List<PrivilegePerspective> privileges = new ArrayList<>();
	private List<Option> properties = new ArrayList<>();
	private List<RelationPerspective> relationsFrom = new ArrayList<>();
	private List<RelationPerspective> relationsTo = new ArrayList<>();
	
	@Override
	public void afterPropertiesSet() throws Exception {
		Request request = getRequest();
		String[] path = request.getLocalPath();
		String type = path[1];
		Long id = Long.parseLong(path[2]);
		Class<? extends Entity> typeClass = extracted(type);
		if (typeClass==null) {
			throw new IllegalRequestException("Unknown type: "+type);
		}
		UserSession privileged = request.getSession();
		Entity entity = modelService.getRequired(typeClass, id, privileged);
		Locale locale = getLocale();
		this.title = entity.getName();
		for (Privilege privilege : modelService.getPrivileges(entity)) {
			PrivilegePerspective perspective = new PrivilegePerspective();
			User user = modelService.get(User.class, privilege.getSubject(), privileged);
			perspective.user = user == null ? EntityPerspective.from(User.class, privilege.getSubject(), locale) : EntityPerspective.from(user, locale);
			perspective.permissions = "" + (privilege.isAlter() ? "A" : "") + (privilege.isView() ? "V" : "") + (privilege.isDelete() ? "D" : "") + (privilege.isReference() ? "R" : "");
			this.privileges.add(perspective);
		}
		properties.add(Option.of("type", entity.getType()));
		properties.add(Option.of("class", typeClass.getSimpleName()));
		properties.add(Option.of("id", entity.getId()));
		properties.add(Option.of("created", entity.getCreated()));
		properties.add(Option.of("updated", entity.getUpdated()));
		for (Property prop : entity.getProperties()) {
			properties.add(Option.of(prop.getKey(), prop.getValue()));
		}
		entity.getProperties();
		
		modelService.find().relations(privileged).from(entity).stream(50).forEach(relation -> {
			RelationPerspective pers = new RelationPerspective();
			pers.entity = EntityPerspective.from(relation.getTo(), locale);
			pers.kind = relation.getKind();
			relationsFrom.add(pers);
		});

		modelService.find().relations(privileged).to(entity).stream(50).forEach(relation -> {
			RelationPerspective pers = new RelationPerspective();
			pers.entity = EntityPerspective.from(relation.getFrom(), locale);
			pers.kind = relation.getKind();
			relationsTo.add(pers);
		});
	}

	private Class<? extends Entity> extracted(String type) {
		Collection<Class<? extends Entity>> classes = modelService.getEntityClasses();
		for (Class<? extends Entity> cls : classes) {
			if (cls.getSimpleName().toLowerCase().equals(type)) {
				return cls;
			}
		}
		return null;
	}
	
	public String getTitle() {
		return title;
	}
	
	public List<PrivilegePerspective> getPrivileges() {
		return privileges;
	}
	
	public List<Option> getProperties() {
		return properties;
	}
	
	public List<RelationPerspective> getRelationsFrom() {
		return relationsFrom;
	}
	
	public List<RelationPerspective> getRelationsTo() {
		return relationsTo;
	}
	
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
	
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
	
	public static class PrivilegePerspective {
		public EntityPerspective user;
		public String permissions;
		
		public EntityPerspective getUser() {
			return user;
		}
		
		public String getPermissions() {
			return permissions;
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
			perspective.name = entity.getName();
			perspective.type = entity.getClass().getSimpleName();
			perspective.href = "/" + locale.getLanguage() + "/" + entity.getClass().getSimpleName().toLowerCase() + "/" + entity.getId(); 
			return perspective;
		}
		public static EntityPerspective from(Class<? extends Entity> type, long id, Locale locale) {
			EntityPerspective perspective = new EntityPerspective();
			perspective.id = id;
			perspective.name = "?";
			perspective.type = type.getSimpleName();
			perspective.href = "/" + locale.getLanguage() + "/" + type.getSimpleName().toLowerCase() + "/" + id; 
			return perspective;
		}
	}
}
