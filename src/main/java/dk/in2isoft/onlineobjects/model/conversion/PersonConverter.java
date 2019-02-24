package dk.in2isoft.onlineobjects.model.conversion;

import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.Person;
import nu.xom.Element;
import nu.xom.Node;

public class PersonConverter extends EntityConverter {

	@Override
	protected Node generateSubXML(Entity entity, Operator privileged) {
		Person person = (Person) entity;
		Element root = new Element("Person",Person.NAMESPACE);
		addSimpleNode(root,"namePrefix",person.getNamePrefix(),Person.NAMESPACE);
		addSimpleNode(root,"givenName",person.getGivenName(),Person.NAMESPACE);
		addSimpleNode(root,"familyName",person.getFamilyName(),Person.NAMESPACE);
		addSimpleNode(root,"nameSuffix",person.getNameSuffix(),Person.NAMESPACE);
		return root;
	}

	@Override
	public Class<? extends Entity> getType() {
		return Person.class;
	}
}
