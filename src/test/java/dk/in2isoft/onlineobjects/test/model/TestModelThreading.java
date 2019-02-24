package dk.in2isoft.onlineobjects.test.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import dk.in2isoft.onlineobjects.core.Operator;
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
		Operator adminOperator = modelService.newAdminOperator();
		User user = getNewTestUser();
		modelService.create(user, adminOperator);
		securityService.grantPublicView(user, true, adminOperator);

		Operator userOperator = adminOperator.as(user);
		
		Image image = new Image();
		modelService.create(image, userOperator);
		modelService.createRelation(user, image, Relation.KIND_SYSTEM_USER_IMAGE, userOperator);
		assertTrue(securityService.canModify(image, userOperator));
		securityService.makePublicVisible(image, userOperator);
		List<Relation> list = modelService.getRelationsFrom(user, Image.class, Relation.KIND_SYSTEM_USER_IMAGE, userOperator);
		assertEquals(1, list.size());
		for (Relation relation : list) {
			modelService.delete(relation, adminOperator);
		}
		modelService.createRelation(user, image, Relation.KIND_SYSTEM_USER_IMAGE, userOperator);

		modelService.delete(image, adminOperator);
		modelService.delete(user, adminOperator);
		adminOperator.commit();
	}

	@Test
	public void testMultiThreading() throws EndUserException, InterruptedException {
		Operator adminOperator = modelService.newAdminOperator();

		// Create user
		User user = getNewTestUser();
		modelService.create(user, adminOperator);
		
		// Create image
		Image image = new Image();
		modelService.create(image, adminOperator.as(user));
		modelService.createRelation(user, image, Relation.KIND_SYSTEM_USER_IMAGE, adminOperator.as(user));
		adminOperator.commit();

		CountDownLatch latch = new CountDownLatch(1);
		Runnable run = () -> {
			try {
				// Reload user + image
				User reloadedUser = modelService.get(User.class, user.getId(), adminOperator);
				Operator reloadedOperator = adminOperator.as(reloadedUser);
				Image reloadedImage = modelService.get(Image.class, image.getId(), reloadedOperator);

				securityService.makePublicVisible(reloadedImage, reloadedOperator);
				List<Relation> list = modelService.getRelationsFrom(reloadedUser, Image.class, Relation.KIND_SYSTEM_USER_IMAGE, reloadedOperator);
				for (Relation relation : list) {
					modelService.delete(relation, adminOperator);
				}
				modelService.createRelation(user, reloadedImage, Relation.KIND_SYSTEM_USER_IMAGE, reloadedOperator);

				modelService.delete(reloadedImage, adminOperator);
				modelService.delete(reloadedUser, adminOperator);
				adminOperator.commit();

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
