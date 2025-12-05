package dk.in2isoft.onlineobjects.model.conversion;

import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.model.Entity;
import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Text;

public class EntityConverter {

	public final Node generateXML(Entity entity, Operator privileged) throws ModelException {
		Element root = new Element("Entity",Entity.NAMESPACE);
		root.addAttribute(new Attribute("id",String.valueOf(entity.getId())));
		root.addAttribute(new Attribute("type",entity.getType()));
		Element name = new Element("name",Entity.NAMESPACE);
		name.appendChild(new Text(entity.getName()));
		root.appendChild(name);
		Node sub = generateSubXML(entity, privileged);
		if (sub!=null) {
			root.appendChild(sub);
		}
		return root;
	}

	public Class<? extends Entity> getType() {
		return Entity.class;
	}

	protected Node generateSubXML(Entity entity, Operator privileged) throws ModelException {
		return null;
	}

	protected void addSimpleNode(Element parent, String name, String value, String namespace) {
		Element element = new Element(name,namespace);
		element.appendChild(new Text(value));
		parent.appendChild(element);
	}
}
