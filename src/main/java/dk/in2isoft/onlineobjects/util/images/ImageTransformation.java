package dk.in2isoft.onlineobjects.util.images;

public class ImageTransformation {
	private int width;
	private int height;
	private boolean cropped;
	private float sepia;
	private float sharpen;
	private float rotation;
	private float quality;
	private boolean flipVertically;
	private boolean flipHorizontally;
	private String format;
	private boolean debug;

	public void setWidth(int width) {
		this.width = width;
	}

	public int getWidth() {
		return width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getHeight() {
		return height;
	}

	public void setCropped(boolean cropped) {
		this.cropped = cropped;
	}

	public boolean isCropped() {
		return cropped;
	}

	public void setSepia(float sepia) {
		this.sepia = sepia;
	}

	public float getSepia() {
		return sepia;
	}

	public float getSharpen() {
		return sharpen;
	}

	public void setSharpen(float sharpen) {
		this.sharpen = sharpen;
	}

	public boolean isTransformed() {
		return width > 0 || height > 0 || cropped || sharpen > 0 || sepia > 0;
	}

	public float getRotation() {
		return rotation;
	}

	public void setRotation(float rotation) {
		this.rotation = rotation;
	}

	public boolean isFlipVertically() {
		return flipVertically;
	}

	public void setFlipVertically(boolean flipVertically) {
		this.flipVertically = flipVertically;
	}

	public boolean isFlipHorizontally() {
		return flipHorizontally;
	}

	public void setFlipHorizontally(boolean flipHorizontally) {
		this.flipHorizontally = flipHorizontally;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public float getQuality() {
		return quality;
	}

	public void setQuality(float quality) {
		this.quality = quality;
	}
}
