package dk.in2isoft.onlineobjects.model.conversion;

import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.WebSite;
import nu.xom.Element;
import nu.xom.Node;

public class WebSiteConverter extends EntityConverter {

	@Override
	protected Node generateSubXML(Entity entity, Operator privileged) {
		WebSite webSite = (WebSite) entity;
		webSite.getIcon();
		Element root = new Element("WebSite",WebSite.NAMESPACE);
		return root;
	}

	@Override
	public Class<? extends Entity> getType() {
		return WebSite.class;
	}
}
