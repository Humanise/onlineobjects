package dk.in2isoft.onlineobjects.core;

import java.io.IOException;
import java.io.InputStream;
import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.metamodel.EntityType;
import javax.transaction.Synchronization;

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
import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.exception.SQLGrammarException;
import org.hibernate.proxy.AbstractLazyInitializer;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.hibernate.type.LongType;
import org.springframework.beans.factory.InitializingBean;

import com.google.common.collect.Lists;

import dk.in2isoft.commons.lang.Code;
import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.core.events.EventService;
import dk.in2isoft.onlineobjects.core.exceptions.ContentNotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.Item;
import dk.in2isoft.onlineobjects.model.LogEntry;
import dk.in2isoft.onlineobjects.model.Privilege;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.model.util.ModelClassInfo;
import dk.in2isoft.onlineobjects.model.validation.EntityValidator;
import dk.in2isoft.onlineobjects.model.validation.UserValidator;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

public class ModelService implements InitializingBean {

	private static Logger log = LogManager.getLogger(ModelService.class);

	private StandardServiceRegistry registry;
	private SessionFactory sessionFactory;

	private EventService eventService;
	private SecurityService securityService;
	
	private Collection<ModelClassInfo> modelClassInfo;
	private List<Class<?>> classes = Lists.newArrayList(); 
	private List<Class<? extends Entity>> entityClasses = Lists.newArrayList(); 
	private List<EntityValidator> entityValidators;

	private Finder finder;
	
	private static final ThreadLocal<String> threadIsDirty = new ThreadLocal<String>();

	
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
		try {
			Configuration configuration = new Configuration();
			configuration.configure("hibernate.cfg.xml");
			//configuration.getEventListeners().setPostDeleteEventListeners(postDeleteEventListener);
			//PostDeleteEventListener[] postDeleteEventListener = ;
			/*
			EventListeners events = configuration.getEventListeners();
			events.setPostCommitDeleteEventListeners(new PostDeleteEventListener[] {eventService});
			events.setPostCommitInsertEventListeners(new PostInsertEventListener[] {eventService});
			events.setPostCommitUpdateEventListeners(new PostUpdateEventListener[] {eventService});*/
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
				// Create registry
				registry = new StandardServiceRegistryBuilder().configure().build();

				// Create MetadataSources
				MetadataSources sources = new MetadataSources(registry);

				// Create Metadata
				Metadata metadata = sources.getMetadataBuilder().build();
				/*
				 * new SchemaExport() // .setOutputFile("db-schema.hibernate5.ddl") //
				 * .create(EnumSet.of(TargetType.SCRIPT), metadata);
				 */

				// Create SessionFactory
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

	@SuppressWarnings("deprecation")
	private void loadModelInfo() {
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("model.xml");
		Builder parser = new Builder();
		Document doc;
		try {
			doc = parser.build(stream);
			modelClassInfo = new ArrayList<ModelClassInfo>();
			Elements items = doc.getRootElement().getChildElements("item");
			for (int i = 0; i < items.size(); i++) {
				Element item = items.get(i);
				Element classElement = item.getFirstChildElement("class");
				String className = classElement.getValue();
				Class<Item> clazz = Code.cast(Class.forName("dk.in2isoft.onlineobjects.model." + className));
				ModelClassInfo info = new ModelClassInfo(clazz);
				modelClassInfo.add(info);
			}
			log.debug("Model info loaded: " + modelClassInfo.size() + " items");
		} catch (ValidityException e) {
			log.error("Could not load model info", e);
		} catch (ParsingException e) {
			log.error("Could not load model info", e);
		} catch (IOException e) {
			log.error("Could not load model info", e);
		} catch (ClassNotFoundException e) {
			log.error("Could not load model info", e);
		}
		SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
		Set<EntityType<?>> entities = sessionFactory.getMetamodel().getEntities();
		for (EntityType<?> entityType : entities) {
			Class<?> clazz = entityType.getJavaType();

			log.debug(clazz + " with super " + clazz.getSuperclass());
			if (clazz.getSuperclass().equals(Entity.class)) {
				Class<? extends Entity> entityClass = Code.cast(clazz);
				entityClasses.add(entityClass);
			}
			classes.add(clazz);
		}

	}

	public Collection<ModelClassInfo> getClassInfo() {
		return modelClassInfo;
	}

	public ModelClassInfo getClassInfo(String simpleName) {
		for (ModelClassInfo info : modelClassInfo) {
			if (info.getSimpleName().equals(simpleName)) {
				return info;
			}
		}
		return null;
	}

	public Collection<ModelClassInfo> getClassInfo(Class<?> interfaze) {
		Collection<ModelClassInfo> infos = new ArrayList<ModelClassInfo>();
		for (ModelClassInfo info : modelClassInfo) {
			if (interfaze.isAssignableFrom(info.getModelClass())) {
				infos.add(info);
			}
		}
		return infos;
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

	private Session getSession() {
		threadIsDirty.set("started");
		Session session = sessionFactory.getCurrentSession();
		if (!session.getTransaction().isActive()) {
			try {
				final Transaction tx = session.beginTransaction();
				tx.registerSynchronization(new Synchronization() {
					
					@Override
					public void beforeCompletion() {
						log.debug("before transaction completion");
					}
					
					@Override
					public void afterCompletion(int status) {
						log.debug("after transaction completion: " + status + ", "+ tx.hashCode());
					}
				});
				log.debug("New transaction: " + tx.hashCode()); 
			} catch (JDBCConnectionException e) {
				// TODO Handle this somehow
			}
			log.debug("Begin transaction!");
		}
		return session;
	}
	
	public Session newSession() {
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		return session;
	}

	protected <T> Query<T> createQuery(String hql, Class<T> type) {
		return getSession().createQuery(hql, type);
	}

	protected Query<?> createQuery(String hql) {
		return getSession().createQuery(hql);
	}

	public void addToSession(Item item) {
		getSession().merge(item);
	}
	
	public void clearAndFlush() {
		getSession().clear();
		getSession().flush();
	}

	public void commit() {
		commit(getSession());
	}

	public void commit(Session session) {
		Transaction tx = session.getTransaction();
		if (tx.isActive()) {
			try {
				session.flush();
				session.clear();
				tx.commit();
			} catch (HibernateException e) {
				log.error("Rolling back!", e);
				tx.rollback();
			}
			log.debug("Commit transaction!");
		} else {
			log.error("Unable to commit inactive transaction");
		}
	}
	
	public void startThread() {
		threadIsDirty.set("newborn");
	}
	
	public void commitThread() {
		String dirty = threadIsDirty.get();
		if ("started".equals(dirty)) {
			commit();
		}
	}

	public void rollBack() {
		Session session = getSession();
		Transaction tx = session.getTransaction();
		if (tx.isActive()) {
			try {
				tx.rollback();
				log.warn("Rolling back!");
			} catch (HibernateException e) {
				log.error("Could not roll back!", e);
			}
		}
	}

	public void createOrUpdate(Item item, Privileged privileged) throws ModelException, SecurityException {
		if (item.isNew()) {
			createItem(item, privileged, getSession());
		} else {
			update(item, privileged);
		}
	}

	public void create(LogEntry entry) {
		create(entry, getSession());
	}

	public void create(LogEntry entry, Session session) {
		session.save(entry);
	}

	public void create(Item item, Privileged privileged) throws ModelException, SecurityException {
		createItem(item, privileged, getSession());
	}
	
	public void create(Item item, Privileged privileged, Session session) throws ModelException, SecurityException {
		createItem(item, privileged, session);
	}

	public boolean isDirty() {
		return getSession().isDirty();
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
			grantPrivilegesPrivately(item, privileged, true, true, true);
		}
		eventService.fireItemWasCreated(item);
	}
	

	/**
	 * Will create a core user if it doesn't already exist
	 * @param user
	 * @throws SecurityException
	 */
	public void createCoreUser(User user) throws SecurityException {
		if (!securityService.isCoreUser(user)) {
			throw new SecurityException("The user is not a core user");
		}
		User existing = getUser(user.getUsername());
		if (existing!=null) {
			throw new SecurityException("The user already exists");
		}
		Session session = getSession();
		user.setCreated(new Date());
		user.setUpdated(new Date());
		session.save(user);
	}

	public void delete(Item item, Privileged privileged) throws ModelException, SecurityException {
		if (item instanceof Entity) {
			deleteEntity((Entity)item, privileged);
		} else if (item instanceof Relation) {
			deleteItem((Relation)item, privileged);
		}
	}

	private void deleteEntity(Entity entity, Privileged privileged) throws ModelException, SecurityException {
		if (!canDelete(entity, privileged)) {
			throw new SecurityException("Privilieged=" + privileged + " cannot delete Entity=" + entity);
		}
		removeAllRelations(entity);
		deleteItem(entity, privileged);
	}

	@Deprecated
	// TODO: Deleting like this may 
	private void removeAllRelations(Entity entity) {
		
		Session session = getSession();
		{
			String hql = "delete Privilege p where p.object in (select relation.id from Relation relation where relation.from=:entity or relation.to=:entity)";
			Query<?> q = session.createQuery(hql);
			q.setParameter("entity", entity);
			int count = q.executeUpdate();
			log.info("Deleting relation privileges for: " + entity.getClass().getSimpleName() + " (" + entity.getIcon() + "); count: " + count);
		}
		{
			String hql = "from Relation relation where relation.from=:entity or relation.to=:entity";
			Query<?> q = session.createQuery(hql);
			q.setParameter("entity", entity);
			ScrollableResults results = q.scroll(ScrollMode.FORWARD_ONLY);
			while (results.next()) {
				Relation rel = (Relation) results.get(0);
				session.delete(rel);
				eventService.fireItemWasDeleted(rel);
			}
		}
	}

	public <T extends Item> void delete(List<T> items, Privileged privileged) throws SecurityException, ModelException {
		for (Item item : items) {
			deleteItem(item, privileged);
		}
	}

	private void deleteItem(Item item, Privileged privileged) throws SecurityException, ModelException {
		if (!canDelete(item, privileged)) {
			throw new SecurityException("Privilieged=" + privileged + " cannot delete Item=" + item);
		}
		removeAllPrivileges(item);
		try {
			getSession().delete(item);
		} catch (HibernateException e) {
			log.error(e.getMessage(), e);
			throw new ModelException(e);
		}
		eventService.fireItemWasDeleted(item);
	}

	// TODO Change to update
	public void update(Item item, Privileged privileged) throws SecurityException,
			ModelException {
		if (!canUpdate(item, privileged)) {
			throw new SecurityException("Privilieged=" + privileged + " cannot update Item=" + item);
		}
		validate(item);
		Session session = getSession();
		item.setUpdated(new Date());
		session.update(item);
		eventService.fireItemWasUpdated(item);
	}

	private void validate(Item item) throws ModelException, SecurityException {
		if (item instanceof Entity) {
			Entity entity = (Entity) item;
			for (EntityValidator validator : entityValidators) {
				validator.validate(entity);
			}
		}
	}

	
	public <T extends Entity> void syncRelationsFrom(Entity fromEntity, String relationKind, Class<T> toType, Collection<Long> ids, Privileged privileged) throws ModelException, SecurityException {
		List<Relation> relations = this.getRelationsFrom(fromEntity, toType, relationKind, privileged);
		List<Long> toAdd = Lists.newArrayList();
		toAdd.addAll(ids);
		for (Relation relation : relations) {
			if (!ids.contains(relation.getTo().getId())) {
				this.delete(relation, privileged);
			} else {
				toAdd.remove(relation.getTo().getId());
			}
		}
		if (!toAdd.isEmpty()) {
			List<T> list = this.list(dk.in2isoft.onlineobjects.core.Query.of(toType).withIds(toAdd));
			for (T t : list) {
				this.createRelation(fromEntity, t, relationKind, privileged);
			}
		}
	}
	
	public boolean canCreate(Item item, Privileged privileged) {
		if (privileged.getIdentity() < 1) {
			return false;
		}
		return true;		
	}
	public boolean canUpdate(Item item, Privileged privileged) {
		if (securityService.isAdminUser(privileged)) {
			return true;
		}
		if (item.getId() == privileged.getIdentity()) {
			return true;
		}
		Privilege privilege = getPriviledge(item, privileged, getSession());
		if (privilege != null && privilege.isAlter()) {
			return true;
		}
		return false;
	}
	
	private boolean canDelete(Item item, Privileged privileged) {
		return securityService.canDelete(item, privileged);
	}

	public <T extends Entity> @NonNull T getRequired(@NonNull Class<T> entityClass, @NonNull Long id, @NonNull Privileged privileged) throws ModelException,ContentNotFoundException {
		@Nullable
		T found = get(entityClass, id, privileged);
		if (found==null) {
			throw new ContentNotFoundException(entityClass, id);
		}
		return found;
	}
	
	public <T extends Entity> @Nullable T get(@NonNull Class<T> entityClass, @NonNull Long id, @NonNull Privileged privileged) throws ModelException {
		dk.in2isoft.onlineobjects.core.Query<T> query = dk.in2isoft.onlineobjects.core.Query.of(entityClass);
		setPrivileged(privileged, query);
		query.withIds(id);
		List<T> result = list(query);
		if (!result.isEmpty()) {
			return result.get(0);
		}
		return null;
	}

	public Relation createRelation(Entity from, Entity to, Privileged privileged) throws ModelException, SecurityException {
		if (from==null || to==null) {
			return null;
		}
		Relation relation = new Relation(from, to);
		create(relation, privileged);
		return relation;
	}

	// TODO make kind the second argument
	public Relation createRelation(Entity from, Entity to, String kind, Privileged privileged) throws ModelException, SecurityException {
		if (from==null || to==null) {
			return null;
		}
		Relation relation = new Relation(from, to);
		relation.setKind(kind);
		create(relation, privileged);
		return relation;
	}

	public List<Relation> getRelations(Entity entity, Privileged privileged) throws ModelException, SecurityException {
		// TODO: Extend RelationQuery to support this
		if (!securityService.isAdminUser(privileged)) {
			throw new SecurityException("Only admin can get all relations");
		}
		String hql = "from Relation as relation where relation.from=:entity or relation.to=:entity order by relation.position";
		Query<Relation> q = createQuery(hql, Relation.class);
		q.setParameter("entity", entity);
		return q.list();
	}

	public List<Relation> getRelationsFrom(Entity entity, Class<? extends Entity> clazz, Privileged privileged) throws ModelException {
		return find().relations(privileged).from(entity).to(clazz).list();
	}

	public List<Relation> getRelationsFrom(Entity entity, Class<? extends Entity> clazz, String relationKind, Privileged privileged) throws ModelException {
		return find().relations(privileged).from(entity).to(clazz).withKind(relationKind).list();
	}

	public List<Relation> getRelationsTo(Entity entity, Class<? extends Entity> clazz, String relationKind, Privileged privileged) throws ModelException {
		return find().relations(privileged).to(entity).from(clazz).withKind(relationKind).list();
	}

	public <T> List<T> getParents(Entity entity, Class<T> classObj, Privileged privileged) throws ModelException {
		dk.in2isoft.onlineobjects.core.Query<T> q = dk.in2isoft.onlineobjects.core.Query.of(classObj);
		q.to(entity);
		setPrivileged(privileged, q);
		return list(q);
	}

	public <T> List<T> getParents(Entity entity, String kind, Class<T> classObj, Privileged privileged) throws ModelException {
		dk.in2isoft.onlineobjects.core.Query<T> q = dk.in2isoft.onlineobjects.core.Query.of(classObj);
		q.to(entity,kind);
		setPrivileged(privileged, q);
		return list(q);
	}

	public <T extends Entity> @Nullable T getParent(Entity entity, Class<T> classObj, Privileged privileged) throws ModelException {
		return getParent(entity, null, classObj, privileged);
	}

	public <T extends Entity> @Nullable T getParent(Entity entity, String kind, Class<T> classObj, Privileged privileged) throws ModelException {
		dk.in2isoft.onlineobjects.core.Query<T> q = dk.in2isoft.onlineobjects.core.Query.of(classObj);
		q.to(entity,kind).withPaging(0, 1);
		setPrivileged(privileged, q);
		List<T> list = list(q);
		if (!list.isEmpty()) {
			return list.get(0);
		} else {
			return null;
		}
	}

	public <T extends Entity> @Nullable T getChild(Entity entity, Class<T> classObj, Privileged privileged) throws ModelException {
		return getChild(entity, null, classObj, privileged);
	}

	public <T extends Entity> @Nullable T getChild(Entity entity, String kind, Class<T> classObj, Privileged privileged) throws ModelException {
		dk.in2isoft.onlineobjects.core.Query<T> q = dk.in2isoft.onlineobjects.core.Query.of(classObj);
		q.from(entity,kind).withPaging(0, 1);
		setPrivileged(privileged, q);
		List<T> list = list(q);
		if (!list.isEmpty()) {
			return list.get(0);
		} else {
			return null;
		}
	}

	public User getUser(String username) {
		Session session = null;
		try {
			if (Strings.isBlank(username)) {
				log.warn("Empty user requested");
			} else {
				session = getSession();
				
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

	public Privilege getPriviledge(Item item, Privileged priviledged) {
		return getPriviledge(item, priviledged, getSession());
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

	public List<Privilege> getPrivileges(Item item) {
		Query<Privilege> q = createQuery("from Privilege as priv where priv.object=:object", Privilege.class);
		q.setParameter("object", item.getId());
		return q.list();
	}
	
	public User getOwner(Item item, Privileged privileged) throws ModelException {
		Query<User> q = buildOwnerQuery(item, privileged);
		q.setMaxResults(1);
		return getSubject(q.uniqueResult());
	}

	public List<User> getOwners(Item item, Privileged privileged) throws ModelException {
		Query<User> q = buildOwnerQuery(item, privileged);
		return getSubjects(q.list());
	}

	private Query<User> buildOwnerQuery(Item item, Privileged privileged) {
		String hql = "select user from User as user, Privilege as itemPriv";
		if (!securityService.isAdminUser(privileged)) {
			hql+=", Privilege as userPriv";
		}
		hql+=" where itemPriv.alter=true and itemPriv.object=:object and itemPriv.subject=user.id" +
			" and user.id!=:public and user.id!=:admin";
		if (!securityService.isAdminUser(privileged)) {
			hql += " and userPriv.object=user and userPriv.subject in (:privileged)";
		}
		hql +=" order by itemPriv.id asc";
		Query<User> q = createQuery(hql, User.class);
		q.setParameter("object", item.getId());
		q.setParameter("public", securityService.getPublicUser().getId());
		q.setParameter("admin", securityService.getAdminPrivileged().getIdentity());
		if (!securityService.isAdminUser(privileged)) {
			List<Long> privs = new ArrayList<>();
			privs.add(privileged.getIdentity());
			if (!securityService.isPublicUser(privileged)) {
				privs.add(securityService.getPublicUser().getId());
			}
			q.setParameterList("privileged", privs, LongType.INSTANCE);
		}
		return q;
	}
	
	public Privilege getPrivilege(long object, long subject) {
		List<Privilege> list = getPrivileges(object, subject);
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

	private List<Privilege> getPrivileges(long object, long subject) {
		Query<Privilege> q = createQuery("from Privilege as priv where priv.object=:object and priv.subject=:subject", Privilege.class);
		q.setParameter("object", object);
		q.setParameter("subject", subject);
		return q.list();
	}

	public void removePrivileges(Item object, Privileged subject) throws SecurityException {
		removePrivileges(object, subject, subject);
	}

	public void removePrivileges(Item object, Privileged subject, Privileged user) throws SecurityException {
		if (!securityService.canModify(object, user)) {
			throw new SecurityException("The user "+subject+" cannot modify "+object+" - so cannot remove privileges");
		}
		Query<?> q = createQuery("delete from Privilege as priv where priv.object=:object and priv.subject=:subject");
		q.setParameter("object", object.getId());
		q.setParameter("subject", subject.getIdentity());
		int count = q.executeUpdate();
		log.info("Deleting privileges for: " + object.getClass().getName() + "; count: " + count);
	}

	public List<Long> listIds(IdQuery query) {
		Query<Long> q = query.createIdQuery(getSession());
		return q.list();
	}

	public <T> Results<T> scroll(ItemQuery<T> query) {
		Query<T> q = query.createItemQuery(getSession()).setReadOnly(true).setFetchSize(0).setCacheable(false).setCacheMode(CacheMode.IGNORE);
		return new Results<T>(q.scroll(ScrollMode.FORWARD_ONLY));
	}
	
	public List<Object[]> querySQL(String sql) throws ModelException {
		try {
			NativeQuery<?> query = getSession().createSQLQuery(sql);
			return Code.castList(query.list());
		} catch (HibernateException e) {
			log.error(e.getMessage(),e);
		}
		return null;
	}

	public <T> List<T> list(CustomQuery<T> query) throws ModelException {
		try {
			NativeQuery<?> sql = getSession().createSQLQuery(query.getSQL());
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

	@SuppressWarnings("unchecked")
	public <T> SearchResult<T> search(CustomQuery<T> query) throws ModelException {
		String sql = query.getSQL();
		try {
			int totalCount = count(query);

			NativeQuery<T> sqlQuery = getSession().createSQLQuery(sql);
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

	public <T> int count(CustomQuery<T> query) {

		String countSQL = query.getCountSQL();
		int totalCount = 0;
		if (countSQL!=null) {
			NativeQuery<?> countSqlQuery = getSession().createSQLQuery(countSQL);
			query.setParameters(countSqlQuery);
			List<?> countRows = countSqlQuery.list();
			Object next = countRows.iterator().next();
			totalCount = ((Number) next).intValue();
		}
		return totalCount;
	}

	public <T> List<T> list(ItemQuery<T> query) {
		Query<T> q = query.createItemQuery(getSession());
		//q.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		List<T> items = q.list();
		for (int i = 0; i < items.size(); i++) {
			T item = items.get(i);
			items.set(i, getSubject(item));
		}
		return items;
	}

	public <T> @Nullable T getFirst(ItemQuery<T> query) {
		Query<T> q = query.createItemQuery(getSession());
		//q.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		q.setFetchSize(1);
		ScrollableResults results = q.scroll(ScrollMode.FORWARD_ONLY);
		if (results.next()) {
			return Code.cast(getSubject(results.get(0)));
		}
		return null;
	}

	public <T> SearchResult<T> search(ItemQuery<T> query) {
		Query<Long> cq = query.createCountQuery(getSession());
		Long count = cq.list().iterator().next();
		Query<T> q = query.createItemQuery(getSession());
		//q.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		List<T> items = q.list();
		for (int i = 0; i < items.size(); i++) {
			T item = items.get(i);
			items.set(i, getSubject(item));
		}
		return new SearchResult<T>(items,count.intValue());
	}

	public Long count(ItemQuery<?> query) {
		Query<Long> cq = query.createCountQuery(getSession());
		return cq.list().iterator().next();
	}

	public <T,U> PairSearchResult<T,U> searchPairs(PairQuery<T,U> query) {
		Query<Long> cq = query.createCountQuery(getSession());
		Long count = cq.list().iterator().next();
		Query<?> q = query.createItemQuery(getSession());
		//q.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		List<Pair<T, U>> map = new ArrayList<Pair<T, U>>();
		try (ScrollableResults scroll = q.scroll(ScrollMode.FORWARD_ONLY)) {
			while (scroll.next()) {
				Object[] object = scroll.get();
				T key = Code.cast(getSubject(object[0]));
				U value = Code.cast(getSubject(object[1]));
				map.add(new Pair<T,U>(key, value));
			}
		}
		return new PairSearchResult<T,U>(map,count.intValue());
	}

	public void grantPrivileges(Item item, Privileged user, boolean view, boolean alter, boolean delete, Privileged granter) throws ModelException, SecurityException {
		if (!securityService.canModify(item, granter)) {
			throw new SecurityException("The granter cannot modify the privileges");
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
		grantPrivilegesPrivately(item, user, view, alter, delete);
	}

	private void grantPrivilegesPrivately(Item item, Privileged user, boolean view, boolean alter, boolean delete) throws ModelException, SecurityException {

		Session session = getSession();
		List<Privilege> list = getPrivileges(item.getId(), user.getIdentity());
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

	private void removeAllPrivileges(Item item) {

		Query<User> query = createQuery("select user from User as user, Privilege as priv where priv.object=:object and priv.subject=user.id", User.class);
		query.setParameter("object", item.getId());
		List<User> users = query.list();
		
		String hql = "delete Privilege p where p.object = :id";
		Query<?> q = createQuery(hql);
		q.setParameter("id", item.getId());
		int count = q.executeUpdate();
		log.info("Deleting privileges for: " + item.getClass().getName() + "; count: " + count);
		
		eventService.firePrivilegesRemoved(item,users);
	}

	public List<Entity> getChildren(Entity item, String relationKind, Privileged privileged) throws ModelException {
		dk.in2isoft.onlineobjects.core.Query<Entity> q = dk.in2isoft.onlineobjects.core.Query.of(Entity.class);
		q.from(item,relationKind);
		setPrivileged(privileged, q);
		return list(q);
	}

	public <T> List<T> getChildren(Entity item, String relationKind, Class<T> classObj, Privileged privileged) throws ModelException {
		dk.in2isoft.onlineobjects.core.Query<T> query = dk.in2isoft.onlineobjects.core.Query.of(classObj);
		query.from(item,relationKind);
		setPrivileged(privileged, query);
		return list(query);
	}

	private <T> void setPrivileged(Privileged privileged, PrivilegedQuery query) {
		if (!securityService.isAdminUser(privileged)) {
			query.as(privileged,securityService.getPublicUser());
		}
	}
	
	public <T> List<T> getChildren(Entity entity, Class<T> classObj, Privileged privileged) throws ModelException {
		dk.in2isoft.onlineobjects.core.Query<T> q = dk.in2isoft.onlineobjects.core.Query.of(classObj);
		q.from(entity);
		setPrivileged(privileged, q);
		return list(q);
	}

	public <T> List<T> getChildrenOrdered(Entity entity, Class<T> classObj, Privileged privileged) throws ModelException {
		dk.in2isoft.onlineobjects.core.Query<T> q = dk.in2isoft.onlineobjects.core.Query.of(classObj);
		q.from(entity).inPosition();
		setPrivileged(privileged, q);
		return list(q);
	}
	
	public Map<String, Integer> getProperties(String key, Class<? extends Entity> cls, Privileged priviledged) {
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
		Query<?> q = createQuery(hql.toString());
		q.setParameter("key", key);
		Map<String, Integer> list = new LinkedHashMap<String, Integer>();
		ScrollableResults scroll = q.scroll();
		while (scroll.next()) {
			list.put(scroll.getString(0), scroll.getLong(1).intValue());
		}
		return list;
	}

	public Optional<Relation> getRelation(long id, Privileged privileged) {
		return find().relations(privileged).withId(id).first();
	}

	public Optional<Relation> getRelation(Entity from, Entity to, String kind, Privileged privileged) {
		return find().relations(privileged).from(from).to(to).withKind(kind).first();
	}

	public Optional<Relation> getRelation(Entity from, Entity to, Privileged privileged) {
		return find().relations(privileged).from(from).to(to).first();
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

}
