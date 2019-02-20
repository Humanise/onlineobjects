package dk.in2isoft.onlineobjects.test.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.Image;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;
import dk.in2isoft.onlineobjects.test.EssentialTests;

@Category(EssentialTests.class)
public class TestModelThreading extends AbstractSpringTestCase {

	@Test
	public void testSingleThreading() throws EndUserException, InterruptedException {
		Privileged admin = securityService.getAdminPrivileged();
		User user = getNewTestUser();
		modelService.create(user, admin);
		securityService.grantPublicView(user, true, admin);

		Image image = new Image();
		modelService.create(image, user);
		modelService.createRelation(user, image, Relation.KIND_SYSTEM_USER_IMAGE, user);
		assertTrue(securityService.canModify(image, user));
		securityService.makePublicVisible(image, user);
		List<Relation> list = modelService.getRelationsFrom(user, Image.class, Relation.KIND_SYSTEM_USER_IMAGE, user);
		assertEquals(1, list.size());
		for (Relation relation : list) {
			modelService.delete(relation, admin);
		}
		modelService.createRelation(user, image, Relation.KIND_SYSTEM_USER_IMAGE, user);

		modelService.delete(image, getAdminUser());
		modelService.delete(user, getAdminUser());
		modelService.commit();
	}

	@Test
	public void testMultiThreading() throws EndUserException, InterruptedException {
		Privileged admin = securityService.getAdminPrivileged();

		// Create user
		User user = getNewTestUser();
		modelService.create(user, admin);
		
		// Create image
		Image image = new Image();
		modelService.create(image, user);
		modelService.createRelation(user, image, Relation.KIND_SYSTEM_USER_IMAGE, user);
		modelService.commit();

		CountDownLatch latch = new CountDownLatch(1);
		Runnable run = () -> {
			try {
				// Reload user + image
				User reloadedUser = modelService.get(User.class, user.getId(), admin);
				Image reloadedImage = modelService.get(Image.class, image.getId(), reloadedUser);

				securityService.makePublicVisible(reloadedImage, reloadedUser);
				List<Relation> list = modelService.getRelationsFrom(reloadedUser, Image.class, Relation.KIND_SYSTEM_USER_IMAGE, reloadedUser);
				for (Relation relation : list) {
					modelService.delete(relation, admin);
				}
				modelService.createRelation(user, reloadedImage, Relation.KIND_SYSTEM_USER_IMAGE, reloadedUser);

				modelService.delete(reloadedImage, admin);
				modelService.delete(reloadedUser, admin);
				modelService.commit();

			} catch (Exception e) {
				e.printStackTrace();
				fail();
			} finally {
				latch.countDown();
			}
		};
		
		(new Thread(run)).start();
		latch.await();
	}
}
