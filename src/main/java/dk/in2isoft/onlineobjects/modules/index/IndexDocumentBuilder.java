package dk.in2isoft.onlineobjects.modules.index;

import org.apache.lucene.document.Document;

import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.Entity;

public interface IndexDocumentBuilder<E extends Entity> {

	Document build(E entity) throws EndUserException;
}
