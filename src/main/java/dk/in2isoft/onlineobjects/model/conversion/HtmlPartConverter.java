package dk.in2isoft.onlineobjects.model.conversion;

import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.HtmlPart;
import nu.xom.Element;
import nu.xom.Node;

public class HtmlPartConverter extends EntityConverter {

	@Override
	protected Node generateSubXML(Entity entity, Operator privileged) {
		HtmlPart header = (HtmlPart) entity;
		Element root = new Element("HtmlPart",HtmlPart.NAMESPACE);
		Element text = new Element("html",HtmlPart.NAMESPACE);
		root.appendChild(text);
		text.appendChild(header.getHtml());
		return root;
	}

	@Override
	public Class<? extends Entity> getType() {
		return HtmlPart.class;
	}
}
