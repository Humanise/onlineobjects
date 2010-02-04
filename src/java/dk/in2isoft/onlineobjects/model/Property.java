package dk.in2isoft.onlineobjects.model;

import java.util.Date;

public class Property {
	
	public static final String KEY_COMMON_TAG = "common.tag";
	public static final String KEY_COMMON_DESCRIPTION = "common.description";
	public static final String KEY_STYLE_MARGIN_TOP = "style.margin.top";
	public static final String KEY_STYLE_MARGIN_BOTTOM = "style.margin.bottom";
	public static final String KEY_STYLE_MARGIN_LEFT = "style.margin.left";
	public static final String KEY_STYLE_MARGIN_RIGHT = "style.margin.right";
	public static final String KEY_HUMAN_INTEREST = "human.interest";
	public static final String KEY_HUMAN_FAVORITE_MUSIC = "human.favorite.music";
	public static final String KEY_HUMAN_FAVORITE_MOVIE = "human.favorite.movie";
	public static final String KEY_HUMAN_FAVORITE_TELEVISIONPROGRAM = "human.favorite.televisionprogram";
	public static final String KEY_HUMAN_FAVORITE_BOOK = "human.favorite.book";
	public static final String KEY_HUMAN_RESUME = "human.resume";
	public static final String KEY_PHOTO_CAMERA_MODEL = "photo.camera.model";
	public static final String KEY_PHOTO_CAMERA_MAKE = "photo.camera.make";
	public static final String KEY_PHOTO_TAKEN = "photo.taken";

	private long id;

	private String key;
	private String value;
	private Double doubleValue;
	private Date dateValue;

	public Property() {
		super();
	}

	public Property(String key,String value) {
		super();
		this.value = value;
		this.key = key;
	}
	public Property(String key, Date value) {
		super();
		this.dateValue = value;
		this.key = key;
	}

	@Override
	public String toString() {
		String str = key+"=";
		if (value!=null) {
			str+=value;
		} else if (dateValue!=null) {
			str+=dateValue;
		} else if (doubleValue!=null) {
			str+=doubleValue;
		}
		return str;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setDoubleValue(Double doubleValue) {
		this.doubleValue = doubleValue;
	}

	public Double getDoubleValue() {
		return doubleValue;
	}

	public void setDateValue(Date dateValue) {
		this.dateValue = dateValue;
	}

	public Date getDateValue() {
		return dateValue;
	}
	
}