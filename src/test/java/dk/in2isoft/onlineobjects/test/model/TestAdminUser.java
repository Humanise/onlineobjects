package dk.in2isoft.onlineobjects.test.model;

import static org.junit.Assert.assertFalse;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;
import dk.in2isoft.onlineobjects.test.EssentialTests;

@Category(EssentialTests.class)
public class TestAdminUser extends AbstractSpringTestCase {
	
	@Test
	public void testLoadUser() throws EndUserException {
		Operator operator = modelService.newAdminOperator();
		User admin = modelService.getUser(SecurityService.ADMIN_USERNAME, operator);
		Assert.assertNotNull(admin);
		Assert.assertEquals(SecurityService.ADMIN_USERNAME, admin.getUsername());
		operator.commit();
	}
	
	@Test
	public void testDeleteUser() throws EndUserException {
		Operator adminOperator = modelService.newAdminOperator();
		User admin = modelService.getUser(SecurityService.ADMIN_USERNAME, adminOperator);
		assertFalse(securityService.canDelete(admin, adminOperator));
		assertFails(() -> modelService.delete(admin, adminOperator));

		assertFalse(securityService.canDelete(admin, adminOperator.as(getPublicUser())));
		assertFails(() -> modelService.delete(admin, adminOperator.as(getPublicUser())));
		adminOperator.commit();
	}

	@Test
	public void testModifyUser() throws EndUserException {
		Operator adminOperator = modelService.newAdminOperator();
		User admin = modelService.getUser(SecurityService.ADMIN_USERNAME, adminOperator);

		assertFalse(securityService.canModify(admin, adminOperator.as(getPublicUser())));
		assertFails(() -> modelService.update(admin, adminOperator.as(getPublicUser())));
		adminOperator.commit();
	}

}
