package dk.in2isoft.onlineobjects.modules.index;

public class IndexDescription {

	private String name;

	private Long userId;

	public IndexDescription() {
	}

	public IndexDescription(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}
}
