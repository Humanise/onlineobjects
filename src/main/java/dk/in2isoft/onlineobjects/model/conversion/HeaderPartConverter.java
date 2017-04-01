package dk.in2isoft.onlineobjects.model.conversion;

import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.HeaderPart;
import nu.xom.Element;
import nu.xom.Node;

public class HeaderPartConverter extends EntityConverter {

	@Override
	protected Node generateSubXML(Entity entity, Privileged privileged) {
		HeaderPart header = (HeaderPart) entity;
		Element root = new Element("HeaderPart",HeaderPart.NAMESPACE);
		Element text = new Element("text",HeaderPart.NAMESPACE);
		root.appendChild(text);
		text.appendChild(header.getText());
		return root;
	}

	@Override
	public Class<? extends Entity> getType() {
		return HeaderPart.class;
	}
}
