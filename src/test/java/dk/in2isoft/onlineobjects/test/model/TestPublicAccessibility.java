package dk.in2isoft.onlineobjects.test.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.model.Comment;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;
import dk.in2isoft.onlineobjects.test.EssentialTests;

@Category(EssentialTests.class)
public class TestPublicAccessibility extends AbstractSpringTestCase {
	
	@Test
	public void testCreation() throws EndUserException {
		Operator publicOperator = modelService.newPublicOperator();
		Comment comment = new Comment();
		// Public cannot create anything but Users
		assertFails(()->modelService.create(comment, publicOperator));
		
		User user = getNewTestUser();
		// Public can create a User
		modelService.create(user, publicOperator);

		// Public cannot delete the user again
		assertFails(()->modelService.delete(user, publicOperator));
		
		modelService.delete(user, publicOperator.as(getAdminUser()));
		publicOperator.commit();
	}

	@Test
	public void testGrantUsingUnsavedObjects() throws EndUserException {
		Operator adminOperator = modelService.newAdminOperator();
		User user = getNewTestUser();
		Comment comment = new Comment();
		assertFails(() -> securityService.grantFullPrivileges(comment, user, adminOperator));

		modelService.create(user, adminOperator);
		
		// Cannot grant privileges since the comment is unsaved
		assertFails(() -> securityService.grantFullPrivileges(comment, user, adminOperator));
		
		adminOperator.commit();
	}
	
	@Test
	public void testPublicAccess() throws EndUserException {
		Operator admin = modelService.newAdminOperator();
		User publicUser = securityService.getPublicUser();
		Operator publicOperator = admin.as(publicUser);
		User user = getNewTestUser();
		
		modelService.create(user, admin);
		Operator userOperator = admin.as(user);

		securityService.makePublicVisible(user, admin);
		
		assertFails(()->securityService.grantFullPrivileges(user, publicUser, admin));
		
		// Public should now only be able to view the user
		assertTrue(securityService.canView(user, publicOperator));
		assertFalse(securityService.canDelete(user, publicOperator));
		assertFalse(securityService.canModify(user, publicOperator));

		// Test that user can access if "public" has access 
		assertTrue(securityService.canView(user, userOperator));
		assertFalse(securityService.canDelete(user, userOperator));
		assertFalse(securityService.canModify(user, userOperator));
		
		// You should not be able to just remove privileges as public user
		assertFails(()->modelService.removePrivileges(user, publicUser, publicOperator));

		securityService.makePublicHidden(user, admin);

		assertFalse(securityService.canView(user, publicOperator));
		assertFalse(securityService.canDelete(user, publicOperator));
		assertFalse(securityService.canModify(user, publicOperator));
		
		assertFalse(securityService.canDelete(user, userOperator));
		assertFalse(securityService.canModify(user, userOperator));
		assertFalse(securityService.canView(user, userOperator));
		
		assertFails(()->modelService.delete(user, publicOperator));
		
		securityService.grantFullPrivileges(user, user, admin);
		
		assertTrue(securityService.canDelete(user, userOperator));
		assertTrue(securityService.canModify(user, userOperator));
		assertTrue(securityService.canView(user, userOperator));
		
		modelService.delete(user, userOperator);

		assertFails(()->modelService.create(user, publicOperator));
		
		admin.commit();
	}

	@Test
	public void testPublicDelete() throws EndUserException {
		Operator publicOperator = modelService.newPublicOperator();
		User user = getNewTestUser();
		
		modelService.create(user, publicOperator.as(getAdminUser()));
		
		assertFalse(securityService.canDelete(user, publicOperator));
		assertFalse(securityService.canDelete(user, publicOperator.as(user)));
		
		modelService.delete(user, publicOperator.as(getAdminUser()));
		
		publicOperator.commit();
	}

	@Test
	public void testPublicDeleteComment() throws EndUserException {

		Operator publicOperator = modelService.newPublicOperator();
		Operator adminOperator = publicOperator.as(getAdminUser());

		User publicUser = securityService.getPublicUser();
		User user = getNewTestUser();
		Comment comment = new Comment();
		
		modelService.create(user, adminOperator);
		Operator userOperator = publicOperator.as(user);

		securityService.grantFullPrivileges(user, user, adminOperator);
		modelService.create(comment, userOperator);

		// The public user cannot delete anything
		assertFalse(securityService.canDelete(comment, publicOperator));

		// The user can delete the comment since it created it
		assertTrue(securityService.canDelete(comment, userOperator));
		
		modelService.removePrivileges(comment, user, userOperator);
		
		// The user can no longer delete the comment
		assertFalse(securityService.canDelete(comment, userOperator));
		
		// Test that the user cannot delete the comment
		assertFails(()->modelService.delete(comment, userOperator));
		
		modelService.grantPrivileges(comment, publicUser, true, true, true, adminOperator);

		// Now the user can delete again
		assertTrue(securityService.canDelete(comment, userOperator));

		modelService.delete(comment, userOperator);
		modelService.delete(user, userOperator);
		
		checkNotPersistent(user, publicOperator);
		checkNotPersistent(comment, publicOperator);
		
		checkNotPersistent(user, publicOperator);
		checkNotPersistent(comment, publicOperator);
		userOperator.commit();
	}
	
	private void checkNotPersistent(Entity item, Operator privileged) throws ModelException {
		Assert.assertNull(modelService.get(Entity.class, item.getId(), privileged));
	}
	
}
