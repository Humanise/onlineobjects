package dk.in2isoft.onlineobjects.test.model;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.Hypothesis;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.modules.knowledge.KnowledgeService;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;
import dk.in2isoft.onlineobjects.test.EssentialTests;

@Category(EssentialTests.class)
public class TestKnowledgeService extends AbstractSpringTestCase {
	
	@Autowired
	private KnowledgeService knowledgeService; 

	@Test
	public void testCreateHypothesis() throws EndUserException {
		Operator publicOperator = modelService.newPublicOperator();
		Operator adminOperator = publicOperator.as(getAdminUser());

		User user = getNewTestUser();
		modelService.create(user, publicOperator);
		Operator userOperator = publicOperator.as(user);
		
		assertFails(() -> knowledgeService.createHypothesis("", userOperator));
		assertFails(() -> knowledgeService.createHypothesis("    ", userOperator));
		assertFails(() -> knowledgeService.createHypothesis("  \t\n  ", userOperator));
		assertFails(() -> knowledgeService.createHypothesis(null, userOperator));
		assertFails(() -> knowledgeService.createHypothesis(Strings.generateRandomString(10001), userOperator));
		
		{
			Hypothesis hypothesis = knowledgeService.createHypothesis("  The world is flat	\n", userOperator);
			Assert.assertEquals(hypothesis.getText(), "The world is flat");
			Assert.assertFalse(hypothesis.isNew());
			modelService.delete(hypothesis, adminOperator);
		}
		{
			String longText = Strings.generateRandomString(10000);
			Hypothesis hypothesis = knowledgeService.createHypothesis(longText, userOperator);
			hypothesis = modelService.get(Hypothesis.class, hypothesis.getId(), userOperator);
			Assert.assertEquals(hypothesis.getText(), longText);
			modelService.delete(hypothesis, adminOperator);
		}

		modelService.delete(user, adminOperator);

		publicOperator.commit();
		
	}

	public void setKnowledgeService(KnowledgeService knowledgeService) {
		this.knowledgeService = knowledgeService;
	}
}
