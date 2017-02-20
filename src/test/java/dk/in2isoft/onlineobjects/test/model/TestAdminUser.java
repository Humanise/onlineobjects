package dk.in2isoft.onlineobjects.test.model;

import static org.junit.Assert.assertFalse;

import org.junit.Assert;
import org.junit.Test;

import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;

public class TestAdminUser extends AbstractSpringTestCase {
	
	@Test
	public void testLoadUser() throws EndUserException {
		User admin = modelService.getUser(SecurityService.ADMIN_USERNAME);
		Assert.assertNotNull(admin);
		Assert.assertEquals(SecurityService.ADMIN_USERNAME, admin.getUsername());
	}
	
	@Test
	public void testDeleteUser() throws EndUserException {
		User admin = modelService.getUser(SecurityService.ADMIN_USERNAME);
		assertFalse(securityService.canDelete(admin, admin));
		assertFails(() -> modelService.deleteEntity(admin, admin));

		assertFalse(securityService.canDelete(admin, getPublicUser()));
		assertFails(() -> modelService.deleteEntity(admin, getPublicUser()));
	}

	@Test
	public void testModifyUser() throws EndUserException {
		User admin = modelService.getUser(SecurityService.ADMIN_USERNAME);

		assertFalse(securityService.canModify(admin, getPublicUser()));
		assertFails(() -> modelService.updateItem(admin, getPublicUser()));
	}

}
