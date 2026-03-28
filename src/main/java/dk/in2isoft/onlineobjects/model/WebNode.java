package dk.in2isoft.onlineobjects.model;


public class WebNode extends Entity {

	public static String TYPE = Entity.TYPE+"/WebNode";
	public static String NAMESPACE = Entity.NAMESPACE+"WebNode/";

	public WebNode() {
		super();
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getIcon() {
		return "monochrome/globe";
	}

}
