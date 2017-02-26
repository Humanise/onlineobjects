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
		assertFails(()->modelService.createItem(comment, getPublicUser()));
		
		User user = new User();
		// Public can create a User
		modelService.createItem(user, getPublicUser());

		assertFails(()->modelService.deleteEntity(user, getPublicUser()));
		
		modelService.deleteEntity(user, getAdminUser());
	}

	@Test
	public void testGrantUsingUnsavedObjects() throws EndUserException {
		User user = new User();
		Comment comment = new Comment();
		assertFails(() -> securityService.grantFullPrivileges(comment, user));

		modelService.createItem(user, getAdminUser());
		
		assertFails(() -> securityService.grantFullPrivileges(comment, user));
		
		modelService.commit();
	}
	
	@Test
	public void testPublicAccess() throws EndUserException {
		User publicUser = securityService.getPublicUser();
		User user = new User();
		
		modelService.createItem(user, getAdminUser());
		securityService.makePublicVisible(user, getAdminUser());
		
		assertFails(()->securityService.grantFullPrivileges(user, publicUser));
		
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
		
		assertFails(()->modelService.deleteEntity(user, publicUser));
		
		securityService.grantFullPrivileges(user, user);
		
		assertTrue(securityService.canDelete(user, user));
		assertTrue(securityService.canModify(user, user));
		assertTrue(securityService.canView(user, user));
		
		modelService.deleteEntity(user, user);

		assertFails(()->modelService.createItem(user, publicUser));
		
		modelService.commit();
	}

	@Test
	public void testPublicDelete() throws EndUserException {

		User publicUser = securityService.getPublicUser();
		User user = new User();
		
		modelService.createItem(user, getAdminUser());
		
		assertFalse(securityService.canDelete(user, publicUser));
		assertFalse(securityService.canDelete(user, user));
		
		modelService.deleteEntity(user, getAdminUser());
		
		modelService.commit();
	}

	@Test
	public void testPublicDeleteComment() throws EndUserException {

		User publicUser = securityService.getPublicUser();
		User user = new User();
		Comment comment = new Comment();
		
		modelService.createItem(user, getAdminUser());
		securityService.grantFullPrivileges(user, user);
		modelService.createItem(comment, user);

		// The public user cannot delete anything
		assertFalse(securityService.canDelete(comment, publicUser));

		// The user can delete the comment since it created it
		assertTrue(securityService.canDelete(comment, user));
		
		modelService.removePrivileges(comment, user);
		
		// The user can no longer delete the comment
		assertFalse(securityService.canDelete(comment, user));
		
		// Test that the user cannot delete the comment
		assertFails(()->modelService.deleteEntity(comment, user));
		
		modelService.grantPrivileges(comment, publicUser, true, true, true);

		// Now the user can delete again
		assertTrue(securityService.canDelete(comment, user));

		modelService.deleteEntity(comment, user);
		modelService.deleteEntity(user, user);
		
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
