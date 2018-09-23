package dk.in2isoft.onlineobjects.modules.organic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Cell implements HeartBeating {
	
	protected static final Logger log = LogManager.getLogger(Cell.class); 

	private String id;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
