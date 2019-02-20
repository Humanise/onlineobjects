package dk.in2isoft.onlineobjects.test.model;

import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.List;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Person;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.WebNode;
import dk.in2isoft.onlineobjects.model.WebPage;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;
import dk.in2isoft.onlineobjects.test.EssentialTests;

@Category(EssentialTests.class)
public class TestRelations extends AbstractSpringTestCase {

	@Test
	public void testRelations() throws SQLException, ModelException, SecurityException {
		Privileged privileged = getAdminUser();

		WebPage page = new WebPage();
		modelService.create(page, privileged);
		assertTrue(page.getId() > 0);
		modelService.commit();

		WebNode node = new WebNode();
		modelService.create(node, privileged);
		modelService.createRelation(page, node, privileged);

		Person person = new Person();
		modelService.create(person, privileged);
		modelService.createRelation(page, person, privileged);
		{
			List<Relation> childRelations = modelService.find().relations(privileged).from(page).list();
			assertTrue(childRelations.size() == 2);
		}
		{
			List<Relation> childRelations = modelService.getRelationsFrom(page, WebNode.class, privileged);
			assertTrue(childRelations.size() == 1);
			assertTrue(childRelations.get(0).getTo() instanceof WebNode);
		}
		{
			List<Relation> childRelations = modelService.getRelationsFrom(page, Person.class, privileged);
			assertTrue(childRelations.size() == 1);
			assertTrue(childRelations.get(0).getTo() instanceof Person);
		}
		modelService.delete(page, privileged);
		modelService.delete(node, privileged);
		modelService.delete(person, privileged);
		modelService.commit();
	}

}
