package dk.in2isoft.onlineobjects.model.validation;

import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.model.Entity;

public interface EntityValidator {
	
	void validate(Entity entity) throws ModelException;
}
