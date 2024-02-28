package dk.in2isoft.onlineobjects.core;

import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.hibernate.CacheMode;
import org.hibernate.HibernateException;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.exception.SQLGrammarException;
import org.hibernate.proxy.AbstractLazyInitializer;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.query.MutationQuery;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.onlineobjects.modules.database.Migrator;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.beans.factory.InitializingBean;

import com.google.common.collect.Lists;

import dk.in2isoft.commons.lang.Code;
import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.core.events.EventService;
import dk.in2isoft.onlineobjects.core.events.ModelEventType;
import dk.in2isoft.onlineobjects.core.exceptions.ContentNotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.Item;
import dk.in2isoft.onlineobjects.model.LogEntry;
import dk.in2isoft.onlineobjects.model.Privilege;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.model.validation.EntityValidator;
import dk.in2isoft.onlineobjects.model.validation.UserValidator;
import dk.in2isoft.onlineobjects.services.ConfigurationService;
import dk.in2isoft.onlineobjects.ui.Request;

public class ModelService implements InitializingBean, OperationProvider {

	private static Logger log = LogManager.getLogger(ModelService.class);

	private StandardServiceRegistry registry;
	private SessionFactory sessionFactory;

	private EventService eventService;
	private SecurityService securityService;
	private Migrator migrator;
	private ConfigurationService configuration;
	
	private List<Class<?>> classes = Lists.newArrayList(); 
	private List<Class<? extends Entity>> entityClasses = Lists.newArrayList(); 
	private List<EntityValidator> entityValidators;
	private long operationCount;

	private Finder finder;
		
	public SessionFactory getSessionfactory() {
		return sessionFactory;
	}

	protected ModelService() {
		entityValidators = new ArrayList<>();
		UserValidator userValidator = new UserValidator();
		userValidator.setModelService(this);
		entityValidators.add(userValidator);
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if (configuration.isMigrateDatabaseSchema()) {
			migrator.migrate();
		}
		try {
			sessionFactory = getSessionFactory();
		} catch (Throwable t) {
			log.fatal("Could not create session factory", t);
			throw new ExceptionInInitializerError(t);
		}
		loadModelInfo();
	}

	public SessionFactory getSessionFactory() {
		if (sessionFactory == null) {
			try {
				/*
	            BootstrapServiceRegistry bootstrapRegistry =
	                    new BootstrapServiceRegistryBuilder()
	                    .applyIntegrator(new Integrator() {
							
							@Override
							public void integrate(Metadata metadata, SessionFactoryImplementor sessionFactory,
									SessionFactoryServiceRegistry serviceRegistry) {
							    EventListenerRegistry eventListenerRegistry = 
							    serviceRegistry.getService(EventListenerRegistry.class);
							    //eventListenerRegistry.appendListeners(EventType.PERSIST, eventService);
							    eventListenerRegistry.appendListeners(EventType.POST_COMMIT_UPDATE, eventService);
							    eventListenerRegistry.appendListeners(EventType.POST_COMMIT_INSERT, eventService);
							    eventListenerRegistry.appendListeners(EventType.POST_COMMIT_DELETE, eventService);
							    //eventListenerRegistry.appendListeners(EventType.SAVE, eventService);
							    //eventListenerRegistry.appendListeners(EventType.POST_UPDATE, eventService);
							    
							}
							
							@Override
							public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
								
							}
						})
	                    .build();
	              
	            StandardServiceRegistryBuilder registryBuilder = 
	                    new StandardServiceRegistryBuilder(bootstrapRegistry);

				registry = registryBuilder.configure().build();
				*/
				registry = new StandardServiceRegistryBuilder().configure().build();
				

				MetadataSources sources = new MetadataSources(registry);

				Metadata metadata = sources.getMetadataBuilder().build();

				sessionFactory = metadata.getSessionFactoryBuilder().build();
			} catch (Exception e) {
				log.error(e);
				if (registry != null) {
					StandardServiceRegistryBuilder.destroy(registry);
				}
			}
		}
		return sessionFactory;
	}

	public DataSource getDataSource() {
		Configuration configuration = new Configuration().configure();
		Properties properties = configuration.getProperties();
		
		PGSimpleDataSource ds = new PGSimpleDataSource() ;
		ds.setURL(properties.getProperty("hibernate.connection.url"));
		ds.setUser( properties.getProperty("hibernate.connection.username") );       
		ds.setPassword( properties.getProperty("hibernate.connection.password") );
		return ds;
	}

	@SuppressWarnings("deprecation")
	private void loadModelInfo() {
		SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
		Set<jakarta.persistence.metamodel.EntityType<?>> entities = sessionFactory.getMetamodel().getEntities();
		for (jakarta.persistence.metamodel.EntityType<?> entityType : entities) {
			Class<?> clazz = entityType.getJavaType();

			log.debug(clazz + " with super " + clazz.getSuperclass());
			if (clazz.getSuperclass().equals(Entity.class)) {
				Class<? extends Entity> entityClass = Code.cast(clazz);
				entityClasses.add(entityClass);
			}
			classes.add(clazz);
		}

	}

	public Class<? extends Entity> getModelClass(String simpleName) throws ModelException {
		try {
			return Code.castClass(Class.forName("dk.in2isoft.onlineobjects.model." + simpleName));
		} catch (ClassNotFoundException e) {
			throw new ModelException("Could not find class with simple name=" + simpleName);
		}
	}
	
	public Collection<Class<? extends Entity>> getEntityClasses() {
		return entityClasses;
	}
	
	public Class<? extends Entity> getEntityClass(String simpleName) {
		for (Class<? extends Entity> cls : entityClasses) {
			if (cls.getSimpleName().equals(simpleName)) {
				return cls;
			}
		}
		return null;
	}
	
	public Operator newOperator(Privileged privileged) {
		return new SimpleOperator(privileged, this);
	}
	
	public Operator newPublicOperator() {
		return new SimpleOperator(securityService.getPublicUser(), this);
	}
	
	public Operator newAdminOperator() {
		return new SimpleOperator(securityService.getAdminPrivileged(), this);
	}

	@Override
	public Operation newOperation() {
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		operationCount++;
		return new Operation(session);
	}
	
	@Override
	public void execute(Operation operation) {
		operationCount--;
		Session session = operation.getSession();
		Transaction tx = session.getTransaction();
		if (tx.isActive()) {
			try {
				session.flush();
				session.clear();
				log.debug("Commit transaction!");
				tx.commit();
				for (Pair<ModelEventType, Object> event : getEffectiveEvents(operation)) {
					ModelEventType type = event.getKey();
					Object object = event.getValue();
					if (object instanceof Item && type == ModelEventType.update) {
						eventService.fireItemWasUpdated((Item) object);
					}
					else if (object instanceof Item && type == ModelEventType.create) {
						eventService.fireItemWasCreated((Item) object);
					}
					else if (object instanceof Item && type == ModelEventType.delete) {
						eventService.fireItemWasDeleted((Item) object);
					}
					else if (object instanceof Pair && type == ModelEventType.privilegesRemoved) {
						Pair<Item,List<User>> pair = Code.cast(object);
						eventService.firePrivilegesRemoved(pair.getKey(), pair.getValue());
					}
				}
				log.debug("Did commit transaction!");
			} catch (HibernateException e) {
				log.error("Rolling back!", e);
				tx.rollback();
			}
			log.debug("Commit transaction!");
		} else {
			log.error("Unable to commit inactive transaction");
		}
	}

	private List<Pair<ModelEventType, Object>> getEffectiveEvents(Operation operation) {
		List<Pair<ModelEventType, Object>> copy = new ArrayList<>();
		for (Pair<ModelEventType, Object> pair : operation.getEvents()) {
			copy.removeIf(x -> equals(pair, x));
			copy.add(pair);
		}
		return copy;
	}

	private boolean equals(Pair<ModelEventType, Object> pair, Pair<ModelEventType, Object> x) {
		
		Object a = x.getValue();
		Object b = pair.getValue();
		if (a instanceof Item && b instanceof Item) {
			return ((Item)a).getId() == ((Item)b).getId();
		}
		return a.equals(b);
	}

	public void rollBack(Operation operation) {
		operationCount--;
		Session session = operation.getSession();
		Transaction tx = session.getTransaction();
		if (tx.isActive()) {
			try {
				tx.rollback();
				log.info("Rolling back transaction");
			} catch (HibernateException e) {
				log.error("Could not roll back!", e);
			}
		}
	}

	public long getActiveOperationCount() {
		return operationCount;
	}

	protected <T> Query<T> createQuery(String hql, Class<T> type, Session session) {
		return session.createQuery(hql, type);
	}

	private Query<?> createQuery(String hql, Session session) {
		return session.createQuery(hql);
	}


	public void createOrUpdate(Item item, Operator operator) throws ModelException, SecurityException {
		if (item.isNew()) {
			create(item, operator);
		} else {
			update(item, operator);
		}
	}

	public void create(LogEntry entry, Operator session) {
		session.getOperation().getSession().save(entry);
	}

	public void create(Item item, Operator operator) throws ModelException, SecurityException {
		createItem(item, operator, operator.getOperation().getSession());
		operator.getOperation().addCreateEvent(item);
	}

	private void createItem(Item item, Privileged privileged, Session session) throws ModelException, SecurityException {
		if (!item.isNew()) {
			throw new ModelException("Tried to create an already created item!");
		}
		if (securityService.isPublicUser(privileged)) {
			if (!(item instanceof User)) {
				throw new SecurityException("Public can only create new users");
			}
		}
		if (!canCreate(item, privileged)) {
			throw new SecurityException("The privileged is not allowed to create the item");
		}
		validate(item);
		item.setCreated(new Date());
		item.setUpdated(new Date());
		session.save(item);
		if (!securityService.isAdminUser(privileged)) {
			grantPrivilegesPrivately(item, privileged, true, true, true, session);
		}
	}
	

	/**
	 * Will create a core user if it doesn't already exist
	 * @param user
	 * @throws SecurityException
	 */
	public void createCoreUser(User user, Operation operation) throws SecurityException {
		if (!securityService.isCoreUser(user)) {
			throw new SecurityException("The user is not a core user");
		}
		User existing = getUser(user.getUsername(), operation);
		if (existing!=null) {
			throw new SecurityException("The user already exists");
		}
		Session session = operation.getSession();
		user.setCreated(new Date());
		user.setUpdated(new Date());
		session.save(user);
	}

	public void delete(Item item, Operator operator) throws ModelException, SecurityException {
		if (item instanceof Entity) {
			deleteEntity((Entity)item, operator);
		} else if (item instanceof Relation) {
			deleteItem((Relation)item, operator);
		}
	}

	public <T extends Entity> void delete(Class<T> type, long id, Operator operator) throws ModelException, ContentNotFoundException, SecurityException {
		T address = getRequired(type, id, operator);
		delete(address, operator);
	}
	
	private void deleteEntity(Entity entity, Operator operator) throws ModelException, SecurityException {
		if (!securityService.canDelete(entity, operator)) {
			throw new SecurityException("Privilieged=" + operator + " cannot delete Entity=" + entity);
		}
		removeAllRelations(entity, operator);
		deleteItem(entity, operator);
	}

	private void removeAllRelations(Entity entity, Operator operator) {
		
		Session session = operator.getOperation().getSession();
		{
			String hql = "delete Privilege p where p.object in (select relation.id from Relation relation where relation.from=:entity or relation.to=:entity)";
			MutationQuery q = session.createMutationQuery(hql);
			q.setParameter("entity", entity);
			int count = q.executeUpdate();
			log.info("Deleting relation privileges for: " + entity.getClass().getSimpleName() + " (" + entity.getIcon() + "); count: " + count);
		}
		{
			String hql = "from Relation relation where relation.from=:entity or relation.to=:entity";
			Query<Relation> q = session.createQuery(hql, Relation.class);
			q.setParameter("entity", entity);
			ScrollableResults<Relation> results = q.scroll(ScrollMode.FORWARD_ONLY);
			while (results.next()) {
				Relation rel = (Relation) results.get();
				session.remove(rel);
				operator.getOperation().addDeleteEvent(rel);
			}
		}
	}

	public <T extends Item> void delete(List<T> items, Operator operator) throws SecurityException, ModelException {
		for (Item item : items) {
			deleteItem(item, operator);
		}
	}

	private void deleteItem(Item item, Operator operator) throws SecurityException, ModelException {
		if (!securityService.canDelete(item, operator)) {
			throw new SecurityException("Privilieged=" + operator + " cannot delete Item=" + item);
		}
		removeAllPrivileges(item, operator.getOperation());
		try {
			operator.getOperation().getSession().delete(item);
		} catch (HibernateException e) {
			log.error(e.getMessage(), e);
			throw new ModelException(e);
		}
		operator.getOperation().addDeleteEvent(item);
	}

	
	public void update(Item item, Operator operational) throws SecurityException,
	ModelException {
		if (!canUpdate(item, operational)) {
			throw new SecurityException("Privilieged=" + operational.getIdentity() + " cannot update Item=" + item);
		}
		validate(item);
		Operation operation = operational.getOperation();
		Session session = operation.getSession();
		item.setUpdated(new Date());
		session.update(item);
		operation.addChangeEvent(item);
		
	}

	private void validate(Item item) throws ModelException, SecurityException {
		if (item instanceof Entity) {
			Entity entity = (Entity) item;
			for (EntityValidator validator : entityValidators) {
				validator.validate(entity);
			}
		}
	}


	public <T extends Entity> void syncRelationsFrom(Entity fromEntity, String relationKind, Class<T> toType, Collection<Long> ids, Operator operator) throws ModelException, SecurityException {
		List<Relation> relations = this.getRelationsFrom(fromEntity, toType, relationKind, operator);
		List<Long> toAdd = Lists.newArrayList();
		toAdd.addAll(ids);
		for (Relation relation : relations) {
			if (!ids.contains(relation.getTo().getId())) {
				this.delete(relation, operator);
			} else {
				toAdd.remove(relation.getTo().getId());
			}
		}
		if (!toAdd.isEmpty()) {
			List<T> list = this.list(dk.in2isoft.onlineobjects.core.Query.of(toType).withIds(toAdd), operator);
			for (T t : list) {
				this.createRelation(fromEntity, t, relationKind, operator);
			}
		}
	}

	@Deprecated
	public boolean canCreate(Item item, Privileged privileged) {
		// TODO: What does this mean...
		if (privileged.getIdentity() < 1) {
			return false;
		}
		return true;		
	}

	public boolean canUpdate(Item item, Operator privileged) {
		if (securityService.isAdminUser(privileged)) {
			return true;
		}
		if (item.getId() == privileged.getIdentity()) {
			return true;
		}
		Privilege privilege = getPriviledge(item, privileged, privileged.getOperation().getSession());
		if (privilege != null && privilege.isAlter()) {
			return true;
		}
		return false;
	}

	public <T extends Entity> @NonNull T getRequired(@NonNull Class<T> entityClass, @NonNull Long id, @NonNull Operator operator) throws ModelException,ContentNotFoundException {
		@Nullable
		T found = get(entityClass, id, operator);
		if (found==null) {
			throw new ContentNotFoundException(entityClass, id);
		}
		return found;
	}

	public <T extends Entity> @Nullable T get(@NonNull Class<T> entityClass, @NonNull Long id, @NonNull Operator operator) throws ModelException {
		dk.in2isoft.onlineobjects.core.Query<T> query = dk.in2isoft.onlineobjects.core.Query.of(entityClass);
		setPrivileged(operator, query);
		query.withIds(id);
		return getFirst(query, operator);
	}

	public <T extends Entity> Optional<T> getOptional(@NonNull Class<T> entityClass, @NonNull Long id, @NonNull Operator operator) throws ModelException {
		return Optional.ofNullable(get(entityClass, id, operator));
	}

	public Relation createRelation(Entity from, Entity to, String kind, Operator operator) throws ModelException, SecurityException {
		if (from==null || to==null) {
			return null;
		}
		Relation relation = new Relation(from, to);
		relation.setKind(kind);
		create(relation, operator);
		return relation;
	}

	public Relation createRelation(Entity from, Entity to, Operator operator) throws ModelException, SecurityException {
		if (from==null || to==null) {
			return null;
		}
		Relation relation = new Relation(from, to);
		create(relation, operator);
		return relation;
	}


	public void ensureRelation(Entity from, Entity to, Request request) throws ModelException, SecurityException {
		if (!find().relations(request).from(from).to(to).exists()) {
			createRelation(from, to, request);
		}
	}

	public List<Relation> getRelations(Entity entity, Operator privileged) throws ModelException, SecurityException {
		// TODO: Extend RelationQuery to support this
		if (!securityService.isAdminUser(privileged)) {
			throw new SecurityException("Only admin can get all relations");
		}
		String hql = "from Relation as relation where relation.from=:entity or relation.to=:entity order by relation.position";
		Query<Relation> q = createQuery(hql, Relation.class, privileged.getOperation().getSession());
		q.setParameter("entity", entity);
		return q.list();
	}

	public List<Relation> getRelationsFrom(Entity entity, Class<? extends Entity> clazz, Operator privileged) throws ModelException {
		return find().relations(privileged).from(entity).to(clazz).list();
	}

	public List<Relation> getRelationsFrom(Entity entity, Class<? extends Entity> clazz, String relationKind, Operator privileged) throws ModelException {
		return find().relations(privileged).from(entity).to(clazz).withKind(relationKind).list();
	}

	public List<Relation> getRelationsTo(Entity entity, Class<? extends Entity> clazz, String relationKind, Operator privileged) throws ModelException {
		return find().relations(privileged).to(entity).from(clazz).withKind(relationKind).list();
	}

	public <T> List<T> getParents(Entity entity, String kind, Class<T> classObj, Operator operator) throws ModelException {
		dk.in2isoft.onlineobjects.core.Query<T> q = dk.in2isoft.onlineobjects.core.Query.of(classObj);
		q.to(entity,kind);
		setPrivileged(operator, q);
		return list(q, operator);
	}

	public <T> List<T> getParents(Entity entity, Class<T> classObj, Operator operator) throws ModelException {
		dk.in2isoft.onlineobjects.core.Query<T> q = dk.in2isoft.onlineobjects.core.Query.of(classObj);
		q.to(entity);
		setPrivileged(operator, q);
		return list(q, operator);
	}

	public <T extends Entity> @Nullable T getParent(Entity entity, Class<T> classObj, Operator operator) throws ModelException {
		return getParent(entity, null, classObj, operator);
	}

	public <T extends Entity> @Nullable T getParent(Entity entity, String kind, Class<T> classObj, Operator operator) throws ModelException {
		dk.in2isoft.onlineobjects.core.Query<T> q = dk.in2isoft.onlineobjects.core.Query.of(classObj);
		q.to(entity,kind).withPaging(0, 1);
		setPrivileged(operator, q);
		List<T> list = list(q, operator);
		if (!list.isEmpty()) {
			return list.get(0);
		} else {
			return null;
		}
	}

	public <T extends Entity> @Nullable T getChild(Entity entity, Class<T> classObj, Operator privileged) throws ModelException {
		return getChild(entity, null, classObj, privileged);
	}

	public <T extends Entity> @Nullable T getChild(Entity entity, String kind, Class<T> classObj, Operator operator) throws ModelException {
		dk.in2isoft.onlineobjects.core.Query<T> q = dk.in2isoft.onlineobjects.core.Query.of(classObj);
		q.from(entity,kind).withPaging(0, 1);
		setPrivileged(operator, q);
		List<T> list = list(q, operator);
		if (!list.isEmpty()) {
			return list.get(0);
		} else {
			return null;
		}
	}

	public User getUser(String username, Operator operator) {
		return getUser(username, operator.getOperation());
	}

	public User getUser(String username, Operation operation) {
		Session session = null;
		try {
			if (Strings.isBlank(username)) {
				log.warn("Empty user requested");
			} else {
				session = operation.getSession();
				
				Query<User> q = session.createQuery("from User as user left join fetch user.properties where lower(user.username)=lower(:username)", User.class);
				q.setParameter("username", username);
				for (User user : q.list()) {
					if (user != null) {
						return getSubject(user);
					}
				}
			}
		} catch (org.hibernate.exception.GenericJDBCException e) {
			boolean active = session!=null && session.getTransaction()!=null ? session.getTransaction().isActive() : false;
			log.error("Unable to get user: "+username+" / session.active="+active,e);
			Throwable cause = e.getCause();
			if (cause instanceof BatchUpdateException) {
				BatchUpdateException batchUpdateException = (BatchUpdateException) cause;
				SQLException nextException = batchUpdateException.getNextException();
				log.error("Next exception: "+nextException.getMessage(),e);
			}
			log.error("SQL: "+e.getSQL());
		}
		return null;
	}

	public User getUser(Operator operator) throws ModelException, ContentNotFoundException {
		return getRequired(User.class, operator.getIdentity(), operator);
	}

	public Privilege getPriviledge(Item item, Operator priviledged) {
		return getPriviledge(item, priviledged, priviledged.getOperation().getSession());
	}

	private Privilege getPriviledge(Item item, Privileged privileged, Session session) {
		String queryString = "from Privilege as priv where priv.object=:object and priv.subject=:subject";
		Query<Privilege> q = session.createQuery(queryString, Privilege.class);
		q.setParameter("object", item.getId());
		q.setParameter("subject", privileged.getIdentity());
		Privilege privilege = (Privilege) q.uniqueResult();
		if (privilege != null) {
			return privilege;
		} else {
			return null;
		}
	}

	public List<Privilege> getPrivileges(Item item, Operator operator) {
		Query<Privilege> q = createQuery("from Privilege as priv where priv.object=:object", Privilege.class, operator.getOperation().getSession());
		q.setParameter("object", item.getId());
		return q.list();
	}

	public User getOwner(Item item, Operator privileged) throws ModelException {
		Query<User> q = buildOwnerQuery(item, privileged, privileged.getOperation().getSession());
		q.setMaxResults(1);
		return getSubject(q.uniqueResult());
	}

	public List<User> getOwners(Item item, Operator privileged) throws ModelException {
		Query<User> q = buildOwnerQuery(item, privileged, privileged.getOperation().getSession());
		return getSubjects(q.list());
	}

	private Query<User> buildOwnerQuery(Item item, Privileged privileged, Session session) {
		String hql = "select user from User as user, Privilege as itemPriv";
		boolean isAdmin = securityService.isAdminUser(privileged);
		if (!isAdmin) {
			hql+=", Privilege as userPriv";
		}
		hql+=" where itemPriv.alter=true and itemPriv.object=:object and itemPriv.subject=user.id" +
			" and user.id!=:public and user.id!=:admin";
		if (!isAdmin) {
			hql += " and userPriv.object=user.id and userPriv.subject in (:privileged)";
		}
		hql +=" order by itemPriv.id asc";
		Query<User> q = createQuery(hql, User.class, session);
		q.setParameter("object", item.getId());
		q.setParameter("public", securityService.getPublicUser().getId());
		q.setParameter("admin", securityService.getAdminPrivileged().getIdentity());
		if (!isAdmin) {
			List<Long> privs = new ArrayList<>();
			privs.add(privileged.getIdentity());
			if (!securityService.isPublicUser(privileged)) {
				privs.add(securityService.getPublicUser().getId());
			}
			q.setParameterList("privileged", privs);
		}
		return q;
	}

	public Privilege getPrivilege(long object, long subject, Operation operation) {
		List<Privilege> list = getPrivileges(object, subject, operation.getSession());
		if (list.size()>1) {
			log.error("Got multiple privileges for object="+object+", subject="+subject);
		}
		Privilege privilege = list.size()>0 ? (Privilege) list.get(0) : null;
		if (privilege != null) {
			return privilege;
		} else {
			return null;
		}
	}

	public List<Long> getPrivilegedUsers(long object, Operator operator) {
		String hql = "select subject from Privilege as priv where priv.object=:object and priv.subject!=:public";
		Query<Long> q = createQuery(hql, Long.class, operator.getOperation().getSession());
		q.setParameter("object", object);
		q.setParameter("public", securityService.getPublicUser().getId());
		return q.list();
	}

	private List<Privilege> getPrivileges(long object, long subject, Session session) {
		Query<Privilege> q = createQuery("from Privilege as priv where priv.object=:object and priv.subject=:subject", Privilege.class, session);
		q.setParameter("object", object);
		q.setParameter("subject", subject);
		return q.list();
	}

	public void removePrivileges(Item object, Privileged subject, Operator operator) throws SecurityException {
		if (!securityService.canModify(object, operator)) {
			throw new SecurityException("The user "+subject+" cannot modify "+object+" - so cannot remove privileges");
		}
		String hql = "delete from Privilege as priv where priv.object=:object and priv.subject=:subject";
		Query<?> q = createQuery(hql, operator.getOperation().getSession());
		q.setParameter("object", object.getId());
		q.setParameter("subject", subject.getIdentity());
		int count = q.executeUpdate();
		log.info("Deleting privileges for: " + object.getClass().getName() + "; count: " + count);
	}

	public List<Long> listIds(IdQuery query, Operator operator) {
		Query<Long> q = query.createIdQuery(operator.getOperation().getSession());
		return q.list();
	}

	public <T> Results<T> scroll(ItemQuery<T> query, Operator operator) {
		Query<T> q = query.createItemQuery(operator.getOperation().getSession());
		q.setReadOnly(true).setFetchSize(0).setCacheable(false).setCacheMode(CacheMode.IGNORE);
		ScrollableResults<T> results = q.scroll(ScrollMode.FORWARD_ONLY);
		return new Results<T>(results);
	}


	public <T> List<T> list(CustomQuery<T> query, Operator operator) throws ModelException {
		return list(query, operator.getOperation().getSession());
	}

	private <T> List<T> list(CustomQuery<T> query, Session session) throws ModelException {
		try {
			NativeQuery<?> sql = session.createNativeQuery(query.getSQL());
			query.setParameters(sql);
			List<?> list = sql.list();
			List<T> result = Lists.newArrayList();
			for (Object t : list) {
				if (t instanceof Object[]) {
					result.add(query.convert((Object[]) t));					
				} else {
					result.add(query.convert(new Object[] {t}));
				}
			}
			return result;
		} catch (SQLGrammarException e) {
			log.error("SQL:"+e.getSQL());
			throw new ModelException("Error executing SQL", e);
		} catch (HibernateException e) {
			
			throw new ModelException("Error executing SQL", e);
		}
	}

	public Finder find() {
		return finder;
	}

	public <T> SearchResult<T> search(CustomQuery<T> query, Operator operator) throws ModelException {
		String sql = query.getSQL();
		try {
			int totalCount = count(query, operator);

			NativeQuery<?> sqlQuery = operator.getOperation().getSession().createNativeQuery(sql);
			query.setParameters(sqlQuery);

			List<Object[]> rows = Code.castList(sqlQuery.list());
			List<T> result = Lists.newArrayList();
			for (Object[] t : rows) {
				result.add(query.convert(t));
			}

			return new SearchResult<T>(result, totalCount);
		} catch (HibernateException e) {
			throw new ModelException("Error executing SQL: "+sql, e);
		}
	}

	public <T> int count(CustomQuery<T> query, Operator operator) {

		String countSQL = query.getCountSQL();
		int totalCount = 0;
		if (countSQL!=null) {
			NativeQuery<?> countSqlQuery = operator.getOperation().getSession().createNativeQuery(countSQL);
			query.setParameters(countSqlQuery);
			List<?> countRows = countSqlQuery.list();
			Object next = countRows.iterator().next();
			totalCount = ((Number) next).intValue();
		}
		return totalCount;
	}

	public <T> List<T> list(ItemQuery<T> query, Operator operator) {
		Query<T> q = query.createItemQuery(operator.getOperation().getSession());
		//q.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		List<T> items = q.list();
		for (int i = 0; i < items.size(); i++) {
			T item = items.get(i);
			items.set(i, getSubject(item));
		}
		return items;
	}

	public <T> @Nullable T getFirst(ItemQuery<T> query, Operator operator) {
		Query<T> q = query.createItemQuery(operator.getOperation().getSession());
		q.setFetchSize(1);
		List<T> list = q.list();
		if (!list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}

	public <T> SearchResult<T> search(ItemQuery<T> query, Operator operator) {
		return search(query, operator.getOperation().getSession());
	}

	private <T> SearchResult<T> search(ItemQuery<T> query, Session session) {
		Query<Long> cq = query.createCountQuery(session);
		Long count = cq.list().iterator().next();
		Query<T> q = query.createItemQuery(session);
		//q.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		List<T> items = q.list();
		for (int i = 0; i < items.size(); i++) {
			T item = items.get(i);
			items.set(i, getSubject(item));
		}
		return new SearchResult<T>(items,count.intValue());
	}

	public Long count(ItemQuery<?> query, Operator operator) {
		Query<Long> cq = query.createCountQuery(operator.getOperation().getSession());
		return cq.list().iterator().next();
	}

	public <T,U> PairSearchResult<T,U> searchPairs(PairQuery<T,U> query, Operator operator) {
		Session session = operator.getOperation().getSession();
		Query<Long> cq = query.createCountQuery(session);
		Long count = cq.list().iterator().next();
		Query<?> q = query.createItemQuery(session);
		//q.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		List<Pair<T, U>> map = new ArrayList<Pair<T, U>>();
		try (ScrollableResults<?> scroll = q.scroll(ScrollMode.FORWARD_ONLY)) {
			while (scroll.next()) {
				Object[] row = (Object[]) scroll.get();
				T key = Code.cast(getSubject(row[0]));
				U value = Code.cast(getSubject(row[1]));
				map.add(new Pair<T,U>(key, value));
			}
		}
		return new PairSearchResult<T,U>(map,count.intValue());
	}


	public void grantPrivileges(Item item, Privileged user, boolean view, boolean alter, boolean delete, Operator operator) throws ModelException, SecurityException {
		if (!securityService.canModify(item, operator)) {
			throw new SecurityException("The granter/operator cannot modify the privileges");
		}
		if (user==null) {
			throw new ModelException("Privileged is null");
		}
		if (user.getIdentity()<1) {
			throw new ModelException("Privileged ID is "+user.getIdentity());
		}
		if (item.isNew()) {
			throw new SecurityException("The item is new so cannot grant privileges: identity="+user.getIdentity());
		}
		if (item instanceof User) {
			if (securityService.isAdminUser((User) item)) {
				if (delete) {
					throw new SecurityException("It is not allowed to make the admin deletable");
				}
				if (alter && item.getId()!=user.getIdentity()) {
					throw new SecurityException("Admin can only be modified by itself");
				}
			}
			if (securityService.isPublicUser((User) item)) {
				if (delete) {
					throw new SecurityException("It is not allowed to make the public deletable");
				}
				if (alter && !securityService.isAdminUser(user)) {
					throw new SecurityException("Only admin can modify the public user");
				}
			}
			if (securityService.isPublicUser(user)) {
				if (delete || alter) {
					throw new SecurityException("The public user is not alowed to delete or modify any users");
				}
			}
		}
		grantPrivilegesPrivately(item, user, view, alter, delete, operator.getOperation().getSession());
	}

	private void grantPrivilegesPrivately(Item item, Privileged user, boolean view, boolean alter, boolean delete, Session session) throws ModelException, SecurityException {

		List<Privilege> list = getPrivileges(item.getId(), user.getIdentity(), session);
		Privilege privilege;
		if (list.size()==0) {
			privilege = new Privilege(user.getIdentity(), item.getId());
		} else {
			privilege = list.get(0);
			for (int i = 1; i < list.size(); i++) {
				Privilege extra = list.get(i);
				log.warn("Removing extra privilege: "+Strings.toJSON(extra));
				session.delete(extra);
			}
		}
		privilege.setAlter(alter);
		privilege.setDelete(delete);
		privilege.setView(view);
		session.save(privilege);
	}

	private void removeAllPrivileges(Item item, Operation operation) {
		
		Query<User> query = createQuery("select user from User as user, Privilege as priv where priv.object=:object and priv.subject=user.id", User.class, operation.getSession());
		query.setParameter("object", item.getId());
		List<User> users = query.list();
		
		String hql = "from Privilege as p where p.object = :id or p.subject = :id";
		Query<Privilege> q = createQuery(hql, Privilege.class, operation.getSession());
		q.setParameter("id", item.getId());
		List<Privilege> list = q.list();
		for (Privilege privilege : list) {
			operation.getSession().delete(privilege);
		}
		
		log.info("Deleting privileges for: " + item.getClass().getName() + "; count: " + list.size());
		operation.addPrivilegesRemoved(item,users);
	}

	public List<Entity> getChildren(Entity item, String relationKind, Operator privileged) throws ModelException {
		dk.in2isoft.onlineobjects.core.Query<Entity> q = dk.in2isoft.onlineobjects.core.Query.of(Entity.class);
		q.from(item,relationKind);
		setPrivileged(privileged, q);
		return list(q, privileged);
	}

	public <T> List<T> getChildren(Entity item, String relationKind, Class<T> classObj, Operator operator) throws ModelException {
		dk.in2isoft.onlineobjects.core.Query<T> query = dk.in2isoft.onlineobjects.core.Query.of(classObj);
		query.from(item,relationKind);
		setPrivileged(operator, query);
		return list(query, operator);
	}

	private <T> void setPrivileged(Privileged privileged, PrivilegedQuery query) {
		if (!securityService.isAdminUser(privileged)) {
			query.as(privileged,securityService.getPublicUser());
		}
	}

	public <T> List<T> getChildren(Entity item, Class<T> classObj, Operator operator) throws ModelException {
		dk.in2isoft.onlineobjects.core.Query<T> query = dk.in2isoft.onlineobjects.core.Query.of(classObj);
		query.from(item);
		setPrivileged(operator, query);
		return list(query, operator);
	}

	public <T> List<T> getChildrenOrdered(Entity entity, Class<T> classObj, Operator privileged) throws ModelException {
		dk.in2isoft.onlineobjects.core.Query<T> q = dk.in2isoft.onlineobjects.core.Query.of(classObj);
		q.from(entity).inPosition();
		setPrivileged(privileged, q);
		return list(q, privileged);
	}

	public Map<String, Integer> getProperties(String key, Class<? extends Entity> cls, Operator priviledged) {
		StringBuilder hql = new StringBuilder();
		hql.append("select p.value as value,count(p.id) as count from ");
		hql.append(cls.getSimpleName()).append(" as entity");
		if (priviledged!=null) { // TODO: Is this allowed
			hql.append(",").append(Privilege.class.getName()).append(" as priv");
		}
		hql.append(" left join entity.properties as p  where p.key=:key");
		if (priviledged!=null) {
			hql.append(" and entity.id = priv.object and priv.subject=").append(priviledged.getIdentity());
		}
		hql.append(" group by p.value order by lower(value)");
		Query<?> q = createQuery(hql.toString(), priviledged.getOperation().getSession());
		q.setParameter("key", key);
		Map<String, Integer> list = new LinkedHashMap<String, Integer>();
		ScrollableResults<?> scroll = q.scroll();
		while (scroll.next()) {
			Object[] row = (Object[]) scroll.get();
			list.put(row[0].toString(), ((Number)row[1]).intValue());
		}
		return list;
	}

	public Optional<Relation> getRelation(long id, Operator operator) {
		return find().relations(operator).withId(id).first();
	}

	public Optional<Relation> getRelation(Entity from, Entity to, String kind, Operator operator) {
		return find().relations(operator).from(from).to(to).withKind(kind).first();
	}

	public Optional<Relation> getRelation(Entity from, Entity to, Operator operator) {
		return find().relations(operator).from(from).to(to).first();
	}

	/* Util */

	/**
	 * @return the subject that a HibernateProxy object proxies or
	 *         <code>object</code> as-is if not proxied by Hibernate
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getSubject(T obj) {
		
		if (obj instanceof HibernateProxy) {
			obj = (T) ((AbstractLazyInitializer) ((HibernateProxy) obj).getHibernateLazyInitializer()).getImplementation();
		}
		return obj;
	}

	public static <T> List<T> getSubjects(List<T> list) {
		return list.stream().map(obj -> getSubject(obj)).collect(Collectors.toList());
	}

	// Wiring...
	
	public void setEventService(EventService eventService) {
		this.eventService = eventService;
	}

	public EventService getEventService() {
		return eventService;
	}
	
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
	
	public void setFinder(Finder finder) {
		this.finder = finder;
	}

	public void setMigrator(Migrator migrator) {
		this.migrator = migrator;
	}
	
	public void setConfiguration(ConfigurationService configuration) {
		this.configuration = configuration;
	}

	public void asAdmin(FailableConsumer<Operator, ? extends Throwable> runnable) {		
		Operator operator = newAdminOperator();
		try {
			runnable.accept(operator);
			operator.commit();
		} catch (Throwable e) {
			log.error(e);
			operator.rollBack();
		}
	}
}
