package dk.in2isoft.onlineobjects.model;

import dk.in2isoft.in2igui.Icons;
import dk.in2isoft.onlineobjects.model.annotations.Appearance;

@Appearance(icon=Icons.COMMON_FOLDER)
public class Tag extends Entity {

	public static String TYPE = Entity.TYPE+"/Tag";
		
	public Tag() {
		super();
	}

	public String getType() {
		return TYPE;
	}
}
