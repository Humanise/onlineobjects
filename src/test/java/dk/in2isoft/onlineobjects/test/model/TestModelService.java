package dk.in2isoft.onlineobjects.test.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.Results;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.Hypothesis;
import dk.in2isoft.onlineobjects.model.Privilege;
import dk.in2isoft.onlineobjects.model.Question;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.Statement;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;
import dk.in2isoft.onlineobjects.test.EssentialTests;

@Category(EssentialTests.class)
public class TestModelService extends AbstractSpringTestCase {
	
	@Test
	public void testGenerally() throws EndUserException {
		Operator publicOperator = modelService.newPublicOperator();
		Operator adminOperator = publicOperator.as(getAdminUser());

		User user = getNewTestUser();
		modelService.create(user, publicOperator);
		Operator userOperator = publicOperator.as(user);
		
		Question question = new Question();
		question.setText("My question");
		modelService.create(question, userOperator);

		Statement statement = new Statement();
		statement.setText("My statement");
		modelService.createOrUpdate(statement, userOperator);
		
		modelService.createRelation(question, statement, userOperator);

		List<Relation> statementRelations = modelService.getRelations(statement, adminOperator);
		assertEquals(1, statementRelations.size());
		List<Relation> questionRelations = modelService.getRelations(question, adminOperator);
		assertEquals(1, questionRelations.size());

		Privilege privilege = modelService.getPriviledge(question, userOperator);
		assertNotNull(privilege);
		assertEquals(1, modelService.getPrivileges(question,adminOperator).size());
		
		assertEquals(1, modelService.getOwners(question, adminOperator).size());
		assertEquals(1, modelService.getOwners(question, userOperator).size());

		modelService.removePrivileges(question, user, userOperator);
		modelService.grantPrivileges(question, user, true, true, true, adminOperator);
		
		question.addProperty("myKey", "theValue");
		modelService.update(question, userOperator);
		Map<String, Integer> properties = modelService.getProperties("myKey", Question.class, userOperator);
		assertEquals(1, properties.size());
		
		List<Relation> relationsFromQuestion = modelService.find().relations(userOperator).from(question).list();
		assertEquals(1, relationsFromQuestion.size());
		
		Results<Statement> scroll = modelService.scroll(Query.after(Statement.class), userOperator);
		assertTrue(scroll.next());
		assertEquals(statement, scroll.get());
		assertFalse(scroll.next());
		assertNull(scroll.get());
		
		modelService.delete(question, userOperator);
		modelService.delete(statement, userOperator);
		modelService.delete(user, adminOperator);

		publicOperator.commit();
	}


	@Test
	public void testDistinct() throws EndUserException {
		Operator publicOperator = modelService.newPublicOperator();
		Operator adminOperator = publicOperator.as(getAdminUser());

		User user = getNewTestUser();
		modelService.create(user, publicOperator);
		
		Operator userOperator = publicOperator.as(user);
		
		Question question = new Question();
		question.addProperty("test-prop", "test-value");
		question.setName("My question");
		modelService.create(question, userOperator);

		Statement statement = new Statement();
		statement.setText("My statement");
		modelService.create(statement, userOperator);

		Statement statement2 = new Statement();
		statement2.setText("My other statement");
		modelService.create(statement2, userOperator);
		Hypothesis hypothesis = new Hypothesis();
		hypothesis.setName("My hypothesis");
		modelService.create(hypothesis, userOperator);
		
		modelService.createRelation(question, statement, userOperator);
		modelService.createRelation(question, statement2, userOperator);
		modelService.createRelation(hypothesis, question, userOperator);
		
		securityService.makePublicVisible(question, userOperator);
		
		publicOperator.commit();

		List<Relation> relations = modelService.getRelationsFrom(question, Statement.class, userOperator);
		assertEquals(2, relations.size());

		Query<Question> query = Query.of(Question.class).as(user).from(hypothesis).to(statement).to(statement2).withPublicView();
		query.withCustomProperty("test-prop", "test-value");
		query.withWords("question");
		List<Question> list = modelService.list(query, userOperator);
		assertEquals(1, list.size());
		
		question = modelService.getRequired(Question.class, question.getId(), adminOperator);
		statement = modelService.getRequired(Statement.class, statement.getId(), adminOperator);
		statement2 = modelService.getRequired(Statement.class, statement2.getId(), adminOperator);
		user = modelService.getRequired(User.class, user.getId(), adminOperator);
		
		modelService.delete(question, userOperator);
		modelService.delete(statement, userOperator);
		modelService.delete(statement2, userOperator);
		modelService.delete(user, adminOperator);

		publicOperator.commit();
	}
}
