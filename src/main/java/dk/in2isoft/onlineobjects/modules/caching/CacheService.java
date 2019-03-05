package dk.in2isoft.onlineobjects.modules.caching;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.Callable;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import dk.in2isoft.commons.lang.Code;
import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.events.ModelEventListener;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.StupidProgrammerException;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.services.ConfigurationService;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.DiskStoreConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration.Strategy;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

public class CacheService implements ApplicationListener<ApplicationContextEvent>, ModelEventListener {

	private ConfigurationService configurationService;
	private CacheManager manager;
	private static final Logger log = LogManager.getLogger(CacheService.class);	
	private CacheAccess<String, String> documentCache = null;
	private CacheAccess<String, Object> perspectiveCache = null;
	
	
	public <T> T getCached(Entity entity,Class<T> perspective, Callable<T> producer) {
		if (configurationService.isDisableCache()) {
			try {
				return producer.call();
			} catch (Exception e) {
				log.error("Unable to produce object for cache", e);
				return null;
			}
		}
		Cache cache = getCache(perspective);
		Serializable key = entity.getId();
		Element found = cache.get(key);
		if (found!=null) {
			Object value = found.getObjectValue();
			if (value!=null && perspective.isAssignableFrom(value.getClass())) {
				log.trace("Hit");
				return Code.cast(value);
			}
		}
		try {
			log.trace("Miss");
			T value = producer.call();
			Element element = new Element(key, value);
			log.trace("Added");
			cache.put(element);
			return value;
		} catch (Exception e) {
			log.error("Unable to produce object for cache", e);
			return null;
		}
	}
	
	private MultiValuedMap<Class<?>,CacheEntry<?>> cache = new ArrayListValuedHashMap<>(); 
	
	public synchronized <T> T cache(long id, Privileged privileged, Class<T> type, Callable<CacheEntry<T>> producer) throws EndUserException {
		String typeName = type.getSimpleName();
		String key = typeName+"_"+id+"_"+privileged.getIdentity(); 
		Collection<CacheEntry<?>> entries = cache.get(type);
		for (CacheEntry<?> entry : entries) {
			if (entry.getId() == id && entry.getPrivileged() == privileged.getIdentity()) {
				log.debug("Cache registry hit: {} {}", typeName, id);
				Object value = perspectiveCache.get(key);
				if (value!=null && type.isAssignableFrom(value.getClass())) {
					log.debug("Cache value hit: {} {}", typeName, id);
					return Code.cast(value);					
				} else {
					log.debug("Cache value miss: {} {}", typeName, id);
				}
			}
		}
		try {
			log.debug("Cache miss: {} {}", typeName, id);
			CacheEntry<T> produced = producer.call();
			if (produced != null) {
				T value = produced.getValue();
				if (value != null) {
					log.debug("Caching value: {} {}", typeName, id);
					perspectiveCache.put(key, value);
					produced.setValue(null);
					cache.put(type, produced);
				} else {
					log.warn("Null cache value: {} {}", typeName, id);
				}
				return value;
			} else {
				throw new StupidProgrammerException();
			}
		} catch (Exception e) {
			if (e instanceof EndUserException) {
				throw (EndUserException) e;
			} else {
				throw new EndUserException(e);
			}
		}
	}
	
	public String getCachedDocument(String key, Callable<String> producer) {
		String found = documentCache.get(key);
		if (found!=null) {
			return found;
		} else {
			log.debug("Cache miss: {}", key);
			String produced;
			try {
				produced = producer.call();
				if (produced != null) {
					documentCache.put(key, produced);
				}
				return produced;
			} catch (Exception e) {
				log.error("Problem producing cache value", e);
			}
		}
		return null;
	}
	
	private Cache getCache(Class<?> cls) {
		String cacheName = cls.getName();
		Cache cache = manager.getCache(cacheName);
		if (cache == null) {
			int maxEntriesLocalHeap = 10;
			cache = new Cache(new CacheConfiguration(cacheName, maxEntriesLocalHeap)
					.memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU).eternal(false).timeToLiveSeconds(60*60)
					.timeToIdleSeconds(60*60)
					.persistence(new PersistenceConfiguration().strategy(Strategy.LOCALTEMPSWAP)));
			cache.initialise();
			manager.addCache(cache);
		}
		return cache;
	}
	
	public void flushToDisk() {
		manager.shutdown();
		JCS.shutdown();
		initManager();
	}
	
	@Override
	public void onApplicationEvent(ApplicationContextEvent event) {
		if (event instanceof ContextRefreshedEvent) {
			initManager();
		}
	}

	private void initManager() {
		String path = new File(configurationService.getStorageDir(), "cache").getAbsolutePath();
		DiskStoreConfiguration diskConfig = new DiskStoreConfiguration().path(path);
		Configuration config = new Configuration().diskStore(diskConfig);
		manager = CacheManager.create(config);
		
		documentCache = JCS.getInstance("default");
		log.debug("Document cache stats: {}", documentCache.getStats());

		perspectiveCache = JCS.getInstance("perspectives");
		log.debug("Perspective cache stats: {}", perspectiveCache.getStats());
	}

	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	@Override
	public void entityWasCreated(Entity entity) {		
	}

	@Override
	public void entityWasUpdated(Entity entity) {
		// TODO Be more clever
		cache.clear();
	}

	@Override
	public void entityWasDeleted(Entity entity) {
		// TODO Be more clever
		cache.clear();
	}

	@Override
	public void relationWasCreated(Relation relation) {
		// TODO Be more clever
		cache.clear();
	}

	@Override
	public void relationWasUpdated(Relation relation) {
		// TODO Be more clever
		cache.clear();
	}

	@Override
	public void relationWasDeleted(Relation relation) {
		// TODO Be more clever
		cache.clear();
	}
}
