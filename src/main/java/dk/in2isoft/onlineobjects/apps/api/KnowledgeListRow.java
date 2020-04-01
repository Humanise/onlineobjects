package dk.in2isoft.onlineobjects.apps.api;

import dk.in2isoft.onlineobjects.modules.knowledge.ApiPerspective;

public class KnowledgeListRow implements ApiPerspective {
	private long id;
	private long version;
	private String type = "";
	private String text = "";
	private String url;
	private boolean favorite;
	private boolean inbox;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Override
	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type == null ? "" : type;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text == null ? "" : text;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isFavorite() {
		return favorite;
	}

	public void setFavorite(boolean favorite) {
		this.favorite = favorite;
	}

	public boolean isInbox() {
		return inbox;
	}

	public void setInbox(boolean inbox) {
		this.inbox = inbox;
	}

}
