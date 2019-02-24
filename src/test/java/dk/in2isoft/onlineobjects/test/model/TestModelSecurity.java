package dk.in2isoft.onlineobjects.test.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.Comment;
import dk.in2isoft.onlineobjects.model.Privilege;
import dk.in2isoft.onlineobjects.model.Question;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;
import dk.in2isoft.onlineobjects.test.EssentialTests;

@Category(EssentialTests.class)
public class TestModelSecurity extends AbstractSpringTestCase {
	
	@Test
	public void testLoad() throws EndUserException {
		Operator publicOperator = modelService.newPublicOperator();
		Operator adminOperator = publicOperator.as(getAdminUser());

		User mainUser = getNewTestUser();
		modelService.create(mainUser, publicOperator);
		Operator mainOperator = publicOperator.as(mainUser);

		User someoneElse = getNewTestUser();
		modelService.create(someoneElse, publicOperator);
		Operator otherOperator = publicOperator.as(someoneElse);
		
		Comment comment = new Comment();
		modelService.create(comment, mainOperator);
		
		Question question = new Question();
		modelService.create(question, mainOperator);
		
		Relation questionCommentRelation = modelService.createRelation(question, comment, mainOperator);
		
		assertTrue(securityService.canModify(questionCommentRelation, mainOperator));
		assertTrue(securityService.canModify(question, mainOperator));
		assertTrue(securityService.canModify(comment, mainOperator));

		// Check that someone else cannot access the content
		assertFalse(securityService.canModify(questionCommentRelation, otherOperator));
		assertFalse(securityService.canView(questionCommentRelation, otherOperator));
		assertFalse(securityService.canDelete(questionCommentRelation, otherOperator));
		
		assertFalse(securityService.canModify(question, otherOperator));
		assertFalse(securityService.canView(question, otherOperator));
		assertFalse(securityService.canDelete(question, otherOperator));

		assertFalse(securityService.canModify(comment, otherOperator));
		assertFalse(securityService.canView(comment, otherOperator));
		assertFalse(securityService.canDelete(comment, otherOperator));
		
		// Check that main user can load the content
		assertNotNull(modelService.get(Comment.class, comment.getId(), mainOperator));
		assertNotNull(modelService.getChild(question, Comment.class, mainOperator));
		assertNotNull(modelService.getParent(comment, Question.class, mainOperator));
		assertTrue(modelService.getRelation(questionCommentRelation.getId(), mainOperator).isPresent());
		assertTrue(modelService.getRelation(question, comment, null, mainOperator).isPresent());
		
		assertEquals(questionCommentRelation, modelService.find().relations(mainOperator).from(question).to(Comment.class).first().get());
		assertEquals(questionCommentRelation, modelService.find().relations(mainOperator).to(comment).from(Question.class).first().get());

		// Check that someone else cannot load the content
		assertNull(modelService.get(Comment.class, comment.getId(), otherOperator));
		assertNull(modelService.getChild(question, Comment.class, otherOperator));
		assertNull(modelService.getParent(comment, Question.class, otherOperator));
		assertFalse(modelService.getRelation(questionCommentRelation.getId(), otherOperator).isPresent());
		assertFalse(modelService.getRelation(question, comment, null, otherOperator).isPresent());

		assertFalse(modelService.find().relations(otherOperator).from(question).to(Comment.class).first().isPresent());
		assertFalse(modelService.find().relations(otherOperator).to(comment).from(Question.class).first().isPresent());
		
		// Someone else cannot load even if it has access to the relation
		securityService.grantFullPrivileges(questionCommentRelation, someoneElse, adminOperator);
		assertFalse(modelService.getRelation(questionCommentRelation.getId(), otherOperator).isPresent());
		assertFalse(modelService.getRelation(question, comment, null, otherOperator).isPresent());
		assertFalse(modelService.find().relations(otherOperator).from(question).to(Comment.class).first().isPresent());
		assertFalse(modelService.find().relations(otherOperator).to(comment).from(Question.class).first().isPresent());

		assertTrue(securityService.isOnlyPrivileged(question, mainUser, adminOperator));
		
		// Someone else still cannot load since only one has access
		securityService.grantFullPrivileges(question, someoneElse, adminOperator);
		assertFalse(modelService.getRelation(questionCommentRelation.getId(), otherOperator).isPresent());
		assertFalse(modelService.getRelation(question, comment, null, otherOperator).isPresent());
		assertFalse(modelService.find().relations(otherOperator).from(question).to(Comment.class).first().isPresent());
		assertFalse(modelService.find().relations(otherOperator).to(comment).from(Question.class).first().isPresent());

		assertFalse(securityService.isOnlyPrivileged(question, mainUser, adminOperator));

		// Now someone else has access since he has access to all three
		securityService.grantFullPrivileges(comment, someoneElse, adminOperator);
		assertTrue(modelService.getRelation(questionCommentRelation.getId(), otherOperator).isPresent());
		assertTrue(modelService.getRelation(question, comment, null, otherOperator).isPresent());
		assertEquals(questionCommentRelation, modelService.find().relations(otherOperator).from(question).to(Comment.class).first().get());
		assertEquals(questionCommentRelation, modelService.find().relations(otherOperator).to(comment).from(Question.class).first().get());
		
		modelService.delete(comment, adminOperator);
		modelService.delete(someoneElse, adminOperator);

		assertTrue(securityService.isOnlyPrivileged(question, mainUser, adminOperator));

		securityService.grantPublicView(question, true, mainOperator);
		
		assertTrue(securityService.isOnlyPrivileged(question, mainUser, adminOperator));

		modelService.delete(mainUser, adminOperator);

		assertFalse(securityService.isOnlyPrivileged(question, mainUser, adminOperator));
		publicOperator.commit();
	}

	@Test
	public void testOwner() throws EndUserException {
		Operator adminOperator = modelService.newAdminOperator();

		User mainUser = getNewTestUser();
		modelService.create(mainUser, adminOperator);
		Operator mainOperator = adminOperator.as(mainUser);

		securityService.grantFullPrivileges(mainUser, mainUser, adminOperator);

		{	// Check that user has access to itself
			List<Privilege> privileges = modelService.getPrivileges(mainUser, adminOperator);
			assertEquals(1, privileges.size());
			Privilege privilege = privileges.get(0);
			assertTrue(privilege.isAlter());
			assertTrue(privilege.isView());
			assertTrue(privilege.isDelete());
			assertFalse(privilege.isReference()); // TODO
			assertEquals(mainUser.getId(),privilege.getSubject());
		}

		// Create another user
		User someoneElse = getNewTestUser();
		modelService.create(someoneElse, adminOperator.as(getPublicUser()));
		Operator otherOperator = adminOperator.as(someoneElse);

		// The other user should not be able to see the main user
		assertFalse(securityService.canView(mainUser, adminOperator.as(someoneElse)));
		
		// Create a comment
		Comment comment = new Comment();
		modelService.create(comment, mainOperator);
		
		// Check that only the main user can see the comments owner
		assertEquals(mainUser, modelService.getOwner(comment, mainOperator));
		assertNull(modelService.getOwner(comment, otherOperator));

		// After making the main user public the other user can see him as owner of the comment
		securityService.makePublicVisible(mainUser, adminOperator);
		assertEquals(mainUser, modelService.getOwner(comment, otherOperator));

		// Check revert of public visibility
		securityService.makePublicHidden(mainUser, adminOperator);
		assertNull(modelService.getOwner(comment, otherOperator));
		
		{ // Check that granting the other user access makes him the owner from his perspective 
			securityService.grantFullPrivileges(comment, someoneElse, adminOperator);
			List<User> owners = modelService.getOwners(comment, otherOperator);
			assertEquals(1, owners.size());
			assertEquals(someoneElse, owners.get(0));

			// Admin sees both owners
			assertEquals(2, modelService.getOwners(comment, adminOperator).size());
		}

		// Making the main user public makes it visible as owner to the other user
		securityService.makePublicVisible(mainUser, adminOperator);
		assertEquals(2, modelService.getOwners(comment, otherOperator).size());

		// Main user is the "single" owner since he came first
		assertEquals(mainUser, modelService.getOwner(comment, otherOperator));

		
		// Clean up
		modelService.delete(mainUser, adminOperator);
		modelService.delete(someoneElse, adminOperator);
		modelService.delete(comment, adminOperator);
		adminOperator.commit();
	}
}
