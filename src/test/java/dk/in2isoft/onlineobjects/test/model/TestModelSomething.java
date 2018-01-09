package dk.in2isoft.onlineobjects.test.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.Image;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;

public class TestModelSomething extends AbstractSpringTestCase {

	@Test
	public void testPlain() throws EndUserException, InterruptedException {
		Privileged admin = securityService.getAdminPrivileged();
		User user = getNewTestUser();
		modelService.createItem(user, admin);
		securityService.grantPublicView(user, true, admin);
		Image image = new Image();
		modelService.createItem(image, user);
		modelService.createRelation(user, image, Relation.KIND_SYSTEM_USER_IMAGE, user);
		assertTrue(securityService.canModify(image, user));
		securityService.makePublicVisible(image, user);
		List<Relation> list = modelService.getRelationsFrom(user, Image.class, Relation.KIND_SYSTEM_USER_IMAGE, user);
		assertEquals(1, list.size());
		for (Relation relation : list) {
			modelService.deleteRelation(relation, admin);
		}
		modelService.createRelation(user, image, Relation.KIND_SYSTEM_USER_IMAGE, user);

		modelService.deleteEntity(image, getAdminUser());
		modelService.deleteEntity(user, getAdminUser());
		modelService.commit();
	}

	//@Test
	public void testThis() throws EndUserException, InterruptedException {
		Privileged priviledged = securityService.getAdminPrivileged();
		User user = getNewTestUser();
		modelService.createItem(user, priviledged);
		
		Image image = new Image();
		modelService.createItem(image, user);
		modelService.createRelation(user, image, Relation.KIND_SYSTEM_USER_IMAGE, user);
		CountDownLatch latch = new CountDownLatch(1);
		Runnable run = new Runnable() {
			
			@Override
			public void run() {
				try {
					securityService.makePublicVisible(image, user);
					List<Relation> list = modelService.getRelationsFrom(user, Image.class, Relation.KIND_SYSTEM_USER_IMAGE, user);
					for (Relation relation : list) {
						modelService.deleteRelation(relation, securityService.getAdminPrivileged());
					}
					modelService.createRelation(user, image, Relation.KIND_SYSTEM_USER_IMAGE, user);
	
					modelService.deleteEntity(image, getAdminUser());
					modelService.deleteEntity(user, getAdminUser());
					modelService.commit();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					latch.countDown();
				}
			}
		};
		(new Thread(run)).start();
		latch.await();
	}
}
