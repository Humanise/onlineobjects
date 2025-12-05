package dk.in2isoft.onlineobjects.model;

import dk.in2isoft.in2igui.Icons;
import dk.in2isoft.onlineobjects.model.annotations.Appearance;

@Appearance(icon=Icons.COMMON_FOLDER)
public class Project extends Entity {

	public static String TYPE = Entity.TYPE+"/Project";

	public Project() {
		super();
	}

	public String getType() {
		return TYPE;
	}
}
