package dk.in2isoft.onlineobjects.ui.jsf.model;

public class GalleryLink {

	public String title;
	public int photoCount;
	public int[] photoIds;
	public long id;
	
	public long getId() {
		return id;
	}
	
	public String getTitle() {
		return title;
	}
	
	public int getPhotoCount() {
		return photoCount;
	}
	
	public int[] getPhotoIds() {
		int[] tempArray = new int[4];
		System.arraycopy(photoIds, 0, tempArray, 0, Math.min(4, photoIds.length));
		return tempArray;
	}
}
