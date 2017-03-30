package dk.in2isoft.onlineobjects.test.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.Comment;
import dk.in2isoft.onlineobjects.model.Privilege;
import dk.in2isoft.onlineobjects.model.Question;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;

public class TestModelSecurity extends AbstractSpringTestCase {
	
	@Test
	public void testLoad() throws EndUserException {
		User mainUser = new User();
		modelService.createItem(mainUser, getPublicUser());

		User someoneElse = new User();
		modelService.createItem(someoneElse, getPublicUser());
		
		Comment comment = new Comment();
		modelService.createItem(comment, mainUser);
		
		Question question = new Question();
		modelService.createItem(question, mainUser);
		
		Relation questionCommentRelation = modelService.createRelation(question, comment, mainUser);
		
		assertTrue(securityService.canModify(questionCommentRelation, mainUser));
		assertTrue(securityService.canModify(question, mainUser));
		assertTrue(securityService.canModify(comment, mainUser));

		// Check that someone else cannot access the content
		assertFalse(securityService.canModify(questionCommentRelation, someoneElse));
		assertFalse(securityService.canView(questionCommentRelation, someoneElse));
		assertFalse(securityService.canDelete(questionCommentRelation, someoneElse));
		
		assertFalse(securityService.canModify(question, someoneElse));
		assertFalse(securityService.canView(question, someoneElse));
		assertFalse(securityService.canDelete(question, someoneElse));

		assertFalse(securityService.canModify(comment, someoneElse));
		assertFalse(securityService.canView(comment, someoneElse));
		assertFalse(securityService.canDelete(comment, someoneElse));
		
		// Check that main user can load the content
		assertNotNull(modelService.get(Comment.class, comment.getId(), mainUser));
		assertNotNull(modelService.getChild(question, Comment.class, mainUser));
		assertNotNull(modelService.getParent(comment, Question.class, mainUser));
		assertTrue(modelService.getRelation(questionCommentRelation.getId(), mainUser).isPresent());
		assertTrue(modelService.getRelation(question, comment, null, mainUser).isPresent());
		
		assertEquals(questionCommentRelation, modelService.find().relations(mainUser).from(question).to(Comment.class).first().get());
		assertEquals(questionCommentRelation, modelService.find().relations(mainUser).to(comment).from(Question.class).first().get());

		// Check that someone else cannot load the content
		assertNull(modelService.get(Comment.class, comment.getId(), someoneElse));
		assertNull(modelService.getChild(question, Comment.class, someoneElse));
		assertNull(modelService.getParent(comment, Question.class, someoneElse));
		assertFalse(modelService.getRelation(questionCommentRelation.getId(), someoneElse).isPresent());
		assertFalse(modelService.getRelation(question, comment, null, someoneElse).isPresent());

		assertFalse(modelService.find().relations(someoneElse).from(question).to(Comment.class).first().isPresent());
		assertFalse(modelService.find().relations(someoneElse).to(comment).from(Question.class).first().isPresent());
		
		// Someone else cannot load even if it has access to the relation
		securityService.grantFullPrivileges(questionCommentRelation, someoneElse, getAdminUser());
		assertFalse(modelService.getRelation(questionCommentRelation.getId(), someoneElse).isPresent());
		assertFalse(modelService.getRelation(question, comment, null, someoneElse).isPresent());
		assertFalse(modelService.find().relations(someoneElse).from(question).to(Comment.class).first().isPresent());
		assertFalse(modelService.find().relations(someoneElse).to(comment).from(Question.class).first().isPresent());

		// Someone else still cannot load since only one has access
		securityService.grantFullPrivileges(question, someoneElse, getAdminUser());
		assertFalse(modelService.getRelation(questionCommentRelation.getId(), someoneElse).isPresent());
		assertFalse(modelService.getRelation(question, comment, null, someoneElse).isPresent());
		assertFalse(modelService.find().relations(someoneElse).from(question).to(Comment.class).first().isPresent());
		assertFalse(modelService.find().relations(someoneElse).to(comment).from(Question.class).first().isPresent());

		// Now someone else has access since he has access to all three
		securityService.grantFullPrivileges(comment, someoneElse, getAdminUser());
		assertTrue(modelService.getRelation(questionCommentRelation.getId(), someoneElse).isPresent());
		assertTrue(modelService.getRelation(question, comment, null, someoneElse).isPresent());
		assertEquals(questionCommentRelation, modelService.find().relations(someoneElse).from(question).to(Comment.class).first().get());
		assertEquals(questionCommentRelation, modelService.find().relations(someoneElse).to(comment).from(Question.class).first().get());
		
		modelService.deleteEntity(comment, getAdminUser());
		modelService.deleteEntity(mainUser, getAdminUser());
		modelService.deleteEntity(someoneElse, getAdminUser());
		modelService.commit();
	}

	@Test
	public void testOwner() throws EndUserException {
		User mainUser = new User();
		modelService.createItem(mainUser, getAdminUser());
		securityService.grantFullPrivileges(mainUser, mainUser, getAdminUser());

		{	// Check that user has access to itself
			List<Privilege> privileges = modelService.getPrivileges(mainUser);
			assertEquals(1, privileges.size());
			Privilege privilege = privileges.get(0);
			assertTrue(privilege.isAlter());
			assertTrue(privilege.isView());
			assertTrue(privilege.isDelete());
			assertFalse(privilege.isReference()); // TODO
			assertEquals(mainUser.getId(),privilege.getSubject());
		}

		// Create another user
		User someoneElse = new User();
		modelService.createItem(someoneElse, getPublicUser());

		// The other user should not be able to see the main user
		assertFalse(securityService.canView(mainUser, someoneElse));
		
		// Create a comment
		Comment comment = new Comment();
		modelService.createItem(comment, mainUser);
		
		// Check that only the main user can see the comments owner
		assertEquals(mainUser, modelService.getOwner(comment, mainUser));
		assertNull(modelService.getOwner(comment, someoneElse));

		// After making the main user public the other user can see him as owner of the comment
		securityService.makePublicVisible(mainUser, getAdminUser());
		assertEquals(mainUser, modelService.getOwner(comment, someoneElse));

		// Check revert of public visibility
		securityService.makePublicHidden(mainUser, getAdminUser());
		assertNull(modelService.getOwner(comment, someoneElse));
		
		{ // Check that granting the other user access makes him the owner from his perspective 
			securityService.grantFullPrivileges(comment, someoneElse, getAdminUser());
			List<User> owners = modelService.getOwners(comment, someoneElse);
			assertEquals(1, owners.size());
			assertEquals(someoneElse, owners.get(0));

			// Admin sees both owners
			assertEquals(2, modelService.getOwners(comment, getAdminUser()).size());
		}

		// Making the main user public makes it visible as owner to the other user
		securityService.makePublicVisible(mainUser, getAdminUser());
		assertEquals(2, modelService.getOwners(comment, someoneElse).size());

		// Main user is the "single" owner since he came first
		assertEquals(mainUser, modelService.getOwner(comment, someoneElse));

		
		// Clean up
		modelService.deleteEntity(mainUser, getAdminUser());
		modelService.deleteEntity(someoneElse, getAdminUser());
		modelService.deleteEntity(comment, getAdminUser());
		modelService.commit();
	}
}
