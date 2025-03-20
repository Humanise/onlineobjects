package dk.in2isoft.onlineobjects.test.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.apps.api.APIController;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.UserQuery;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.BadRequestException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.model.Client;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.modules.user.ClientInfo;
import dk.in2isoft.onlineobjects.modules.user.MemberService;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;
import dk.in2isoft.onlineobjects.test.EssentialTests;

@Category(EssentialTests.class)
public class TestClientSecret extends AbstractSpringTestCase {

	@Autowired
	private MemberService memberService;

	@Autowired
	private APIController apiController;

	@Test
	public void testGettingSecret() throws EndUserException {
		Operator publicOperator = modelService.newPublicOperator();
		try {
			User user = createMember(publicOperator);
			Operator userOperator = publicOperator.as(user);

			String clientId = UUID.randomUUID().toString();
			ClientInfo clientInfo = new ClientInfo("Test client");
			clientInfo.setUUID(clientId);
			String secret = securityService.getSecret(clientInfo, user, userOperator);
			assertNotNull(secret);

			String altClientId = UUID.randomUUID().toString();
			ClientInfo altClientInfo = new ClientInfo("Test client");
			altClientInfo.setUUID(altClientId);
			String altSecret = securityService.getSecret(altClientInfo, user, userOperator);
			assertNotNull(altSecret);

			UserQuery q = new UserQuery();
			q.setSecret(altSecret);
			{
				User found = modelService.getFirst(q, userOperator);
				assertEquals(user.getId(), found.getId());
			}
			{
				List<Client> clients = modelService.getChildren(user, Client.class, userOperator);
				assertEquals(clients.size(), 2);
			}
			String secretAgain = securityService.getSecret(clientInfo, user, userOperator);

			assertEquals(secret, secretAgain);

			User otherUser = createMember(publicOperator);
			Operator otherOperator = publicOperator.as(otherUser);
			ClientInfo otherClientInfo = new ClientInfo("Test client");
			otherClientInfo.setUUID(UUID.randomUUID().toString());
			String otherSecret = securityService.getSecret(otherClientInfo, otherUser, otherOperator);

			assertNotEquals(secret, otherSecret);

			User otherReloaded = securityService.getUserBySecret(otherSecret, publicOperator);
			assertEquals(otherReloaded.getId(), otherUser.getId());

			{
				List<Client> clients = modelService.getChildren(user, Client.class, userOperator);
				assertEquals(clients.size(), 2);
			}

			modelService.delete(user, publicOperator.as(getAdminUser()));
			modelService.delete(otherUser, publicOperator.as(getAdminUser()));
			publicOperator.commit();
		} catch (Exception e) {
			publicOperator.rollBack();
		}
	}

	@Test
	public void testAPI() throws EndUserException, IOException {
		assertNotNull(apiController);

		// TODO Test the API endpoint
		// Request request = null;
		// apiController.authentication(request);
	}

	private User createMember(Operator operator) throws BadRequestException, EndUserException, ModelException {
		String username = getUniqueTestUserName();
		String password = "zup4$seKr8";
		String fullName = "Dummy Test User";
		String email = Strings.generateRandomString(5) + "@domain.com";

		User user = memberService.createMember(operator, username, password, fullName, email);
		return user;
	}

	// Wiring

	public void setMemberService(MemberService memberService) {
		this.memberService = memberService;
	}
}
