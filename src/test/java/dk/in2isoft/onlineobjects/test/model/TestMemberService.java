package dk.in2isoft.onlineobjects.test.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.core.UserSession;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.IllegalRequestException;
import dk.in2isoft.onlineobjects.model.EmailAddress;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.Person;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.modules.user.MemberService;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;

public class TestMemberService extends AbstractSpringTestCase {
	
	@Autowired
	private MemberService memberService;

	@Test
	public void testCreateMemberFails() throws EndUserException {

		String username = "test"+System.currentTimeMillis();
		String password = "zup4$seKr8";
		String fullName = "Dummy Test User";
		String email = "name@domain.com";

		assertFails(() -> memberService.createMember(null, null, null, null, null));

		assertFails(() -> memberService.createMember(null, username, password, fullName, email));

		assertFails(() -> memberService.createMember(getPublicUser(), "a", password, fullName, email));
		
		assertFails(() -> memberService.createMember(getPublicUser(), "admin", password, fullName, email));
		assertFails(() -> memberService.createMember(getPublicUser(), "public", password, fullName, email));

		assertFails(() -> memberService.createMember(getPublicUser(), username, "x", fullName, email));

		for (String invalidMail : new String[] {null, "invalid email"}) {
			assertFails(() -> memberService.createMember(getPublicUser(), username, password, fullName, invalidMail));
		}
	}

	@Test
	public void testCreateMember() throws EndUserException {
		try {
			String username = "test"+System.currentTimeMillis();
			String password = "zup4$seKr8";
			String fullName = "Dummy Test User";
			String email = Strings.generateRandomString(5)+"@domain.com";
	
			User user = memberService.createMember(getPublicUser(), username, password, fullName, email);
			assertNotEquals(password, user.getPassword());
			
			assertThatOnlyUserHasAccess(user, user);
	
			Person person = memberService.getUsersPerson(user, user);
			assertEquals(fullName, person.getFullName());
			assertThatOnlyUserHasAccess(person, user);
			
			// Check that the email is the users primary
			EmailAddress primaryEmail = memberService.getUsersPrimaryEmail(user, user);
			assertEquals(email, primaryEmail.getAddress());
			assertEquals(email, primaryEmail.getName());
			assertThatOnlyUserHasAccess(primaryEmail, user);
			
			// Check that the same e-mail is attached to the person
			List<EmailAddress> mailsOfPerson = modelService.getChildren(person, EmailAddress.class, user);
			assertEquals(1, mailsOfPerson.size());
			assertEquals(primaryEmail, mailsOfPerson.get(0));
	
			// Find the user by the e-mail
			User foundByEmail = memberService.getUserByPrimaryEmail(email, user);
			assertEquals(user, foundByEmail);
			
			try {
				memberService.createMember(getPublicUser(), username+"2", password, fullName, email);
				fail("It should not be possible to use the same e-mail again");
			} catch (IllegalRequestException e) {
				assertEquals("emailExists",e.getCode());
			}
	
			// Try logging in with the user
			UserSession session = new UserSession(getPublicUser());
			securityService.changeUser(session, username, password);
			assertEquals(user.getIdentity(), session.getIdentity());
			
			String newPassword = "new$ecr8p4$s";
			// TODO Maybe test that public cannot do this
			securityService.changePassword(username, password, newPassword, user);
			
			assertFalse(securityService.changeUser(session, username, password));
			assertEquals(user.getIdentity(), session.getIdentity());
			
			modelService.commit();
			Thread.sleep(5000);
			
			user = modelService.get(User.class, user.getId(), user);
			
			// Clean up
			memberService.deleteMember(user, getAdminUser());
			
			assertNull(modelService.get(User.class, user.getId(), getAdminUser()));
			modelService.commit();
		} catch (Exception e) {
			e.printStackTrace(System.err);
			modelService.rollBack();
			assertNull(e);
		}
	}

	private void assertThatOnlyUserHasAccess(Entity entity, User user) {
		// Check that public cannot access the user
		assertFalse(securityService.canView(entity, getPublicUser()));
		assertFalse(securityService.canModify(entity, getPublicUser()));
		assertFalse(securityService.canDelete(entity, getPublicUser()));

		// Check that public cannot access the user
		assertTrue(securityService.canView(entity, user));
		assertTrue(securityService.canModify(entity, user));
		assertTrue(securityService.canDelete(entity, user));

	}

	public void setMemberService(MemberService memberService) {
		this.memberService = memberService;
	}
}
