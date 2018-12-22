package dk.in2isoft.onlineobjects.test.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;

import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.model.Comment;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;

public class TestPublicAccessibility extends AbstractSpringTestCase {
	
	@Test
	public void testCreation() throws EndUserException {
		Comment comment = new Comment();
		// Public cannot create anything but Users
		assertFails(()->modelService.create(comment, getPublicUser()));
		
		User user = getNewTestUser();
		// Public can create a User
		modelService.create(user, getPublicUser());

		// Public cannot delete the user again
		assertFails(()->modelService.delete(user, getPublicUser()));
		
		modelService.delete(user, getAdminUser());
	}

	@Test
	public void testGrantUsingUnsavedObjects() throws EndUserException {
		User user = getNewTestUser();
		Comment comment = new Comment();
		assertFails(() -> securityService.grantFullPrivileges(comment, user, getAdminUser()));

		modelService.create(user, getAdminUser());
		
		// Cannot grant privileges since the comment is unsaved
		assertFails(() -> securityService.grantFullPrivileges(comment, user, getAdminUser()));
		
		modelService.commit();
	}
	
	@Test
	public void testPublicAccess() throws EndUserException {
		User publicUser = securityService.getPublicUser();
		User user = getNewTestUser();
		
		modelService.create(user, getAdminUser());
		securityService.makePublicVisible(user, getAdminUser());
		
		assertFails(()->securityService.grantFullPrivileges(user, publicUser, getAdminUser()));
		
		// Public should now only be able to view the user
		assertTrue(securityService.canView(user, publicUser));
		assertFalse(securityService.canDelete(user, publicUser));
		assertFalse(securityService.canModify(user, publicUser));

		// Test that user can access if "public" has access 
		assertTrue(securityService.canView(user, user));
		assertFalse(securityService.canDelete(user, user));
		assertFalse(securityService.canModify(user, user));
		
		// You should not be able to just remove privileges as public user
		assertFails(()->modelService.removePrivileges(user, publicUser));

		securityService.makePublicHidden(user, getAdminUser());

		assertFalse(securityService.canView(user, publicUser));
		assertFalse(securityService.canDelete(user, publicUser));
		assertFalse(securityService.canModify(user, publicUser));
		
		assertFalse(securityService.canDelete(user, user));
		assertFalse(securityService.canModify(user, user));
		assertFalse(securityService.canView(user, user));
		
		assertFails(()->modelService.delete(user, publicUser));
		
		securityService.grantFullPrivileges(user, user, getAdminUser());
		
		assertTrue(securityService.canDelete(user, user));
		assertTrue(securityService.canModify(user, user));
		assertTrue(securityService.canView(user, user));
		
		modelService.delete(user, user);

		assertFails(()->modelService.create(user, publicUser));
		
		modelService.commit();
	}

	@Test
	public void testPublicDelete() throws EndUserException {

		User publicUser = securityService.getPublicUser();
		User user = getNewTestUser();
		
		modelService.create(user, getAdminUser());
		
		assertFalse(securityService.canDelete(user, publicUser));
		assertFalse(securityService.canDelete(user, user));
		
		modelService.delete(user, getAdminUser());
		
		modelService.commit();
	}

	@Test
	public void testPublicDeleteComment() throws EndUserException {

		User publicUser = securityService.getPublicUser();
		User user = getNewTestUser();
		Comment comment = new Comment();
		
		modelService.create(user, getAdminUser());
		securityService.grantFullPrivileges(user, user, getAdminUser());
		modelService.create(comment, user);

		// The public user cannot delete anything
		assertFalse(securityService.canDelete(comment, publicUser));

		// The user can delete the comment since it created it
		assertTrue(securityService.canDelete(comment, user));
		
		modelService.removePrivileges(comment, user);
		
		// The user can no longer delete the comment
		assertFalse(securityService.canDelete(comment, user));
		
		// Test that the user cannot delete the comment
		assertFails(()->modelService.delete(comment, user));
		
		modelService.grantPrivileges(comment, publicUser, true, true, true, getAdminUser());

		// Now the user can delete again
		assertTrue(securityService.canDelete(comment, user));

		modelService.delete(comment, user);
		modelService.delete(user, user);
		
		checkNotPersistent(user, publicUser);
		checkNotPersistent(comment, publicUser);
		
		modelService.commit();

		checkNotPersistent(user, publicUser);
		checkNotPersistent(comment, publicUser);
	}
	
	private void checkNotPersistent(Entity item, Privileged privileged) throws ModelException {
		Assert.assertNull(modelService.get(Entity.class, item.getId(), privileged));
	}
	
}
