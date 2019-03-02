package dk.in2isoft.onlineobjects.modules.caching;

import java.io.File;
import java.io.Serializable;
import java.util.concurrent.Callable;

import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import dk.in2isoft.commons.lang.Code;
import dk.in2isoft.onlineobjects.model.Entity;
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

public class CacheService implements ApplicationListener<ApplicationContextEvent> {

	private ConfigurationService configurationService;
	private CacheManager manager;
	private static final Logger log = LogManager.getLogger(CacheService.class);	
	private CacheAccess<String, String> documentCache = null;
	
	
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
		log.debug("String cache stats: {}", documentCache.getStats());
	}

	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}
}
