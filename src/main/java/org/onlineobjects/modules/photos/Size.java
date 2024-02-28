package org.onlineobjects.modules.photos;

public class Size {
	private int width;
	private int height;

	public static Size of(int width, int height) {
		Size o = new Size();
		o.setWidth(width);
		o.setHeight(height);
		return o;
	}

	@Override
	public String toString() {
		return "{width: " + getWidth() + ", height: " + getHeight() + "}";
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}
}