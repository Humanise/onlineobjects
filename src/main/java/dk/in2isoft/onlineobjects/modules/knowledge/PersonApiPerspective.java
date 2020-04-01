package dk.in2isoft.onlineobjects.modules.knowledge;

public class PersonApiPerspective implements ApiPerspective {
	private long id;
	private long version;
	private String name;

	public long getVersion() {
		return version;
	}
	
	public void setVersion(long version) {
		this.version = version;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
