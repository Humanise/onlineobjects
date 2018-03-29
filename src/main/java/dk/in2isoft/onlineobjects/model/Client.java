package dk.in2isoft.onlineobjects.model;

public class Client extends Entity {

	public static String TYPE = Entity.TYPE+"/Client";
	public static String NAMESPACE = Entity.NAMESPACE+"Client/";
	private static String ICON = "common/object";
	
	private String UUID;

	public Client() {
		super();
	}

	public String getType() {
		return TYPE;
	}

	public String getIcon() {
		return ICON;
	}

	public String getUUID() {
		return UUID;
	}

	public void setUUID(String uUID) {
		UUID = uUID;
	}

	
}
