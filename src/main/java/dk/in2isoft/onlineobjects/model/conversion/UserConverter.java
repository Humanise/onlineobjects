package dk.in2isoft.onlineobjects.model.conversion;

import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.User;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Text;

public class UserConverter extends EntityConverter {

	@Override
	protected Node generateSubXML(Entity entity, Operator privileged) {
		User user = (User) entity;
		Element root = new Element("User",User.NAMESPACE);
		Element username = new Element("username",User.NAMESPACE);
		username.appendChild(new Text(user.getUsername()));
		root.appendChild(username);
		return root;
	}

	@Override
	public Class<? extends Entity> getType() {
		return User.class;
	}
}
