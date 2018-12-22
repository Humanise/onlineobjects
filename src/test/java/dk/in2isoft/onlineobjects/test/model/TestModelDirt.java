package dk.in2isoft.onlineobjects.test.model;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;

public class TestModelDirt extends AbstractSpringTestCase {

	@Test
	public void testThis() throws EndUserException {
		Privileged priviledged = securityService.getAdminPrivileged();
		User user = getNewTestUser();
		modelService.create(user, priviledged);
		modelService.delete(user, priviledged);
		assertTrue(modelService.isDirty());
		modelService.commit();
		assertFalse(modelService.isDirty());
	}
}
