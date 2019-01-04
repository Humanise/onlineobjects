package dk.in2isoft.onlineobjects.test.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.Hypothesis;
import dk.in2isoft.onlineobjects.model.Privilege;
import dk.in2isoft.onlineobjects.model.Question;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.Statement;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;

public class TestModelService extends AbstractSpringTestCase {
	
	@Test
	public void testGenerally() throws EndUserException {
		User user = getNewTestUser();
		modelService.create(user, getPublicUser());
		
		Question question = new Question();
		question.setText("My question");
		modelService.create(question, user);

		Statement statement = new Statement();
		statement.setText("My statement");
		modelService.createOrUpdate(statement, user);
		
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
		modelService.update(question, user);
		Map<String, Integer> properties = modelService.getProperties("myKey", Question.class, user);
		assertEquals(1, properties.size());
		
		List<Relation> relationsFromQuestion = modelService.find().relations(user).from(question).list();
		assertEquals(1, relationsFromQuestion.size());
		
		modelService.delete(question, user);
		modelService.delete(statement, user);
		modelService.delete(user, getAdminUser());

		assertTrue(modelService.isDirty());
		modelService.commit();
		assertFalse(modelService.isDirty());
	}


	@Test
	public void testDistinct() throws EndUserException {
		User user = getNewTestUser();
		modelService.create(user, getPublicUser());
		
		Question question = new Question();
		question.addProperty("test-prop", "test-value");
		question.setName("My question");
		modelService.create(question, user);

		Statement statement = new Statement();
		statement.setText("My statement");
		modelService.create(statement, user);

		Statement statement2 = new Statement();
		statement2.setText("My other statement");
		modelService.create(statement2, user);
		Hypothesis hypothesis = new Hypothesis();
		hypothesis.setName("My hypothesis");
		modelService.create(hypothesis, user);
		
		modelService.createRelation(question, statement, user);
		modelService.createRelation(question, statement2, user);
		modelService.createRelation(hypothesis, question, user);
		
		securityService.makePublicVisible(question, user);
		
		modelService.commit();

		List<Relation> relations = modelService.getRelationsFrom(question, Statement.class, user);
		assertEquals(2, relations.size());

		Query<Question> query = Query.of(Question.class).as(user).from(hypothesis).to(statement).to(statement2).withPublicView();
		query.withCustomProperty("test-prop", "test-value");
		query.withWords("question");
		List<Question> list = modelService.list(query);
		assertEquals(1, list.size());
		
		question = modelService.getRequired(Question.class, question.getId(), getAdminUser());
		statement = modelService.getRequired(Statement.class, statement.getId(), getAdminUser());
		statement2 = modelService.getRequired(Statement.class, statement2.getId(), getAdminUser());
		user = modelService.getRequired(User.class, user.getId(), getAdminUser());
		
		modelService.delete(question, user);
		modelService.delete(statement, user);
		modelService.delete(statement2, user);
		modelService.delete(user, getAdminUser());

		assertTrue(modelService.isDirty());
		modelService.commit();
}
}
