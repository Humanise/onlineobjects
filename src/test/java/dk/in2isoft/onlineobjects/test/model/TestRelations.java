package dk.in2isoft.onlineobjects.test.model;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.List;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import dk.in2isoft.onlineobjects.core.Operator;
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
		Operator adminOperator = modelService.newAdminOperator();
		
		WebPage page = new WebPage();
		modelService.create(page, adminOperator);
		assertTrue(page.getId() > 0);

		WebNode node = new WebNode();
		modelService.create(node, adminOperator);

		assertNull(modelService.createRelation(null, null, adminOperator));
		assertNull(modelService.createRelation(page, null, adminOperator));
		assertNull(modelService.createRelation(null, node, adminOperator));
		assertNull(modelService.createRelation(null, null, Relation.KIND_COMMON_SOURCE, adminOperator));
		assertNull(modelService.createRelation(page, null, Relation.KIND_COMMON_SOURCE, adminOperator));
		assertNull(modelService.createRelation(null, node, Relation.KIND_COMMON_SOURCE, adminOperator));

		modelService.createRelation(page, node, adminOperator);

		Person person = new Person();
		modelService.create(person, adminOperator);
		modelService.createRelation(page, person, adminOperator);
		{
			List<Relation> childRelations = modelService.find().relations(adminOperator).from(page).list();
			assertTrue(childRelations.size() == 2);
		}
		{
			List<Relation> childRelations = modelService.getRelationsFrom(page, WebNode.class, adminOperator);
			assertTrue(childRelations.size() == 1);
			assertTrue(childRelations.get(0).getTo() instanceof WebNode);
		}
		{
			List<Relation> childRelations = modelService.getRelationsFrom(page, Person.class, adminOperator);
			assertTrue(childRelations.size() == 1);
			assertTrue(childRelations.get(0).getTo() instanceof Person);
		}
		modelService.delete(page, adminOperator);
		modelService.delete(node, adminOperator);
		modelService.delete(person, adminOperator);
		adminOperator.commit();
	}

}
