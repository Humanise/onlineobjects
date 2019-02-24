package dk.in2isoft.onlineobjects.test.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.UserSession;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.IllegalRequestException;
import dk.in2isoft.onlineobjects.model.EmailAddress;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.Person;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.modules.user.MemberService;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;
import dk.in2isoft.onlineobjects.test.EssentialTests;

@Category(EssentialTests.class)
public class TestMemberService extends AbstractSpringTestCase {
	
	@Autowired
	private MemberService memberService;

	@Test
	public void testCreateMemberFails() throws EndUserException {
		Operator publicOperator = modelService.newPublicOperator();
		String username = "test"+System.currentTimeMillis();
		String password = "zup4$seKr8";
		String fullName = "Dummy Test User";
		String email = "name@domain.com";
		
		assertFails(() -> memberService.createMember(null, null, null, null, null));

		assertFails(() -> memberService.createMember(null, username, password, fullName, email));

		assertFails(() -> memberService.createMember(publicOperator, "a", password, fullName, email));
		
		assertFails(() -> memberService.createMember(publicOperator, "admin", password, fullName, email));
		assertFails(() -> memberService.createMember(publicOperator, "public", password, fullName, email));

		assertFails(() -> memberService.createMember(publicOperator, username, "x", fullName, email));

		for (String invalidMail : new String[] {null, "invalid email"}) {
			assertFails(() -> memberService.createMember(publicOperator, username, password, fullName, invalidMail));
		}
		publicOperator.commit();
	}

	@Test
	public void testCreateMember() throws Exception {
		Operator publicOperator = modelService.newPublicOperator();
		
		try {
			String username = "test"+System.currentTimeMillis();
			String password = "zup4$seKr8";
			String fullName = "Dummy Test User";
			String email = Strings.generateRandomString(5)+"@domain.com";
	
			assertEquals(0, modelService.getActiveOperationCount());
			User user = memberService.createMember(publicOperator, username, password, fullName, email);
			assertNotEquals(password, user.getPassword());
			Operator userOperator = publicOperator.as(user);
			
			assertThatOnlyUserHasAccess(user, user, publicOperator);
	
			Person person = memberService.getUsersPerson(user, userOperator);
			assertEquals(fullName, person.getFullName());
			assertThatOnlyUserHasAccess(person, user, publicOperator);
			
			// Check that the email is the users primary
			EmailAddress primaryEmail = memberService.getUsersPrimaryEmail(user, userOperator);
			assertEquals(email, primaryEmail.getAddress());
			assertEquals(email, primaryEmail.getName());
			assertThatOnlyUserHasAccess(primaryEmail, user, publicOperator);
			
			// Check that the same e-mail is attached to the person
			List<EmailAddress> mailsOfPerson = modelService.getChildren(person, EmailAddress.class, userOperator);
			assertEquals(1, mailsOfPerson.size());
			assertEquals(primaryEmail, mailsOfPerson.get(0));
	
			// Find the user by the e-mail
			User foundByEmail = memberService.getUserByPrimaryEmail(email, userOperator);
			assertEquals(user, foundByEmail);
			
			try {
				memberService.createMember(publicOperator, username+"2", password, fullName, email);
				fail("It should not be possible to use the same e-mail again");
			} catch (IllegalRequestException e) {
				assertEquals("emailExists",e.getCode());
			}
	
			// Try logging in with the user
			UserSession session = new UserSession(getPublicUser());
			securityService.changeUser(session, username, password, publicOperator);
			assertEquals(user.getIdentity(), session.getIdentity());
			
			String newPassword = "new$ecr8p4$s";
			// TODO Maybe test that public cannot do this
			securityService.changePassword(username, password, newPassword, userOperator);
			
			assertFalse(securityService.changeUser(session, username, password, publicOperator));
			assertEquals(user.getIdentity(), session.getIdentity());
			
			publicOperator.commit();
			Thread.sleep(1000);
			
			user = modelService.get(User.class, user.getId(), publicOperator.as(getAdminUser()));
			assertNotNull(user);
			
			// Clean up
			memberService.deleteMember(user, publicOperator.as(getAdminUser()));
			
			assertNull(modelService.get(User.class, user.getId(), publicOperator.as(getAdminUser())));
			publicOperator.commit();
		} catch (Exception e) {
			e.printStackTrace(System.err);
			publicOperator.rollBack();
			assertNull(e);
		}
	}

	private void assertThatOnlyUserHasAccess(Entity entity, User user, Operator operator) {
		// Check that public cannot access the user
		assertFalse(securityService.canView(entity, operator.as(getPublicUser())));
		assertFalse(securityService.canModify(entity, operator.as(getPublicUser())));
		assertFalse(securityService.canDelete(entity, operator.as(getPublicUser())));

		// Check that public cannot access the user
		assertTrue(securityService.canView(entity, operator.as(user)));
		assertTrue(securityService.canModify(entity, operator.as(user)));
		assertTrue(securityService.canDelete(entity, operator.as(user)));

	}

	public void setMemberService(MemberService memberService) {
		this.memberService = memberService;
	}
}
