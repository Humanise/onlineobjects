package dk.in2isoft.onlineobjects.model.conversion;

import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.model.EmailAddress;
import dk.in2isoft.onlineobjects.model.Entity;
import nu.xom.Element;
import nu.xom.Node;

public class EmailAddressConverter extends EntityConverter {

	@Override
	protected Node generateSubXML(Entity entity, Operator privileged) {
		EmailAddress address = (EmailAddress) entity;
		Element root = new Element("EmailAddress",EmailAddress.NAMESPACE);
		addSimpleNode(root,"address",address.getAddress(),EmailAddress.NAMESPACE);
		addSimpleNode(root,"context",address.getContext(),EmailAddress.NAMESPACE);
		return root;
	}

	@Override
	public Class<? extends Entity> getType() {
		return EmailAddress.class;
	}
}
