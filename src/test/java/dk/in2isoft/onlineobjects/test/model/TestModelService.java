package dk.in2isoft.onlineobjects.test.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.Privilege;
import dk.in2isoft.onlineobjects.model.Question;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.Statement;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;

public class TestModelService extends AbstractSpringTestCase {
	
	@Test
	public void testLoad() throws EndUserException {
		User user = getNewTestUser();
		modelService.createItem(user, getPublicUser());
		
		Question question = new Question();
		question.setText("My question");
		modelService.createItem(question, user);

		Statement statement = new Statement();
		question.setText("My statement");
		modelService.createOrUpdateItem(statement, user);
		
		modelService.createRelation(question, statement, user);

		List<Relation> statementRelations = modelService.getRelations(statement, getAdminUser());
		assertEquals(1, statementRelations.size());
		List<Relation> questionRelations = modelService.getRelations(question, getAdminUser());
		assertEquals(1, questionRelations.size());

		Privilege privilege = modelService.getPriviledge(question, user);
		assertNotNull(privilege);
		assertEquals(1, modelService.getPrivileges(question).size());
		
		assertEquals(1, modelService.getOwners(question, getAdminUser()).size());
		assertEquals(1, modelService.getOwners(question, user).size());

		modelService.removePrivileges(question, user);
		modelService.grantPrivileges(question, user, true, true, true, getAdminUser());
		
		question.addProperty("myKey", "theValue");
		modelService.updateItem(question, user);
		Map<String, Integer> properties = modelService.getProperties("myKey", Question.class, user);
		assertEquals(1, properties.size());
		
		modelService.deleteEntity(question, user);
		modelService.deleteEntity(statement, user);
		modelService.deleteEntity(user, getAdminUser());

		assertTrue(modelService.isDirty());
		modelService.commit();
		assertFalse(modelService.isDirty());
	}
}
