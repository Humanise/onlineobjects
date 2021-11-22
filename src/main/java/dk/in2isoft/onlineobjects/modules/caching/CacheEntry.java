package dk.in2isoft.onlineobjects.modules.caching;

import java.util.Collection;

public class CacheEntry<T> {

	private long id;
	private long privileged;
	private T value;
	private Collection<Long> ids;
	private Collection<Class<?>> types;

	public CacheEntry() {
	}
	
	public CacheEntry(long id, long privileged, Collection<Long> ids, T value) {
		super();
		this.id = id;
		this.privileged = privileged;
		this.value = value;
		this.ids = ids;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public Collection<Long> getIds() {
		return ids;
	}
	
	public void setIds(Collection<Long> ids) {
		this.ids = ids;
	}

	public long getPrivileged() {
		return privileged;
	}

	public void setPrivileged(long privileged) {
		this.privileged = privileged;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public Collection<Class<?>> getTypes() {
		return types;
	}

	public void setTypes(Collection<Class<?>> types) {
		this.types = types;
	}


}
