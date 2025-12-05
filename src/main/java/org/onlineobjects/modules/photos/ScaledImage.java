package org.onlineobjects.modules.photos;

import com.github.jsonldjava.shaded.com.google.common.base.Objects;

public class ScaledImage extends Size {
	private String url;

	public static ScaledImage of(Size s, String url) {
		ScaledImage si = new ScaledImage();
		si.setWidth(s.getWidth());
		si.setHeight(s.getHeight());
		si.setUrl(url);
		return si;
	}

	@Override
	public String toString() {
		return "{width: " + getWidth() + ", height: " + getHeight() + "}";
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ScaledImage) {
			ScaledImage other = (ScaledImage) obj;
			return Objects.equal(other.getWidth(), this.getWidth())
					&& Objects.equal(other.getHeight(), this.getHeight())
					&& Objects.equal(other.getUrl(), this.getUrl());
		}
		return super.equals(obj);
	}
}