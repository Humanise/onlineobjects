package dk.in2isoft.onlineobjects.modules.photos;

import java.io.Serializable;

public class SimplePhotoPerspective implements Serializable {

	private static final long serialVersionUID = -8457766110089182716L;

	private long id;
	private int width;
	private int height;
	private String title;
	private long ownerId;
	private Float rotation;
	private String colors;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public long getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(long ownerId) {
		this.ownerId = ownerId;
	}

	public Float getRotation() {
		return rotation;
	}

	public void setRotation(Float rotation) {
		this.rotation = rotation;
	}

	public String getColors() {
		return colors;
	}

	public void setColors(String colors) {
		this.colors = colors;
	}

}
