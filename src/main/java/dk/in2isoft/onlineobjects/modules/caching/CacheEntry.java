package dk.in2isoft.onlineobjects.modules.caching;

public class CacheEntry<T> {

	private long id;
	private long privileged;
	private T value;

	public CacheEntry() {
	}
	
	public CacheEntry(long id, long privileged, T value) {
		super();
		this.id = id;
		this.privileged = privileged;
		this.value = value;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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

}
