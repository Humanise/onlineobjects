package dk.in2isoft.onlineobjects.publishing;

import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.Entity;
import nu.xom.Node;

public abstract class DocumentBuilder {

	public DocumentBuilder() {
	}

	public abstract Node build(Document document, Operator operator) throws EndUserException;

	public abstract Entity create(Operator operator) throws EndUserException;

	public abstract Class<? extends Entity> getEntityType();
}
