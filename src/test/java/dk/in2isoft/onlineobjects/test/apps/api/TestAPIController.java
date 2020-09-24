package dk.in2isoft.onlineobjects.test.apps.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

import dk.in2isoft.onlineobjects.apps.api.APIController;
import dk.in2isoft.onlineobjects.apps.api.APISearchResult;
import dk.in2isoft.onlineobjects.apps.api.AuthenticationResponse;
import dk.in2isoft.onlineobjects.apps.api.KnowledgeListRow;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.Error;
import dk.in2isoft.onlineobjects.core.exceptions.IllegalRequestException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.modules.knowledge.InternetAddressApiPerspective;
import dk.in2isoft.onlineobjects.modules.networking.NetworkService;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;
import dk.in2isoft.onlineobjects.test.TestableNetworkService;
import dk.in2isoft.onlineobjects.ui.Request;

//@RunWith(PowerMockRunner.class)
//@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
public class TestAPIController extends AbstractSpringTestCase {

	private static final String PASSWORD = "ZA8oTRRbYBBjGXG2m8R6";

	private static final String USERNAME = "tester";

	@Autowired
	private ModelService modelService;
	
	@Autowired
	private SecurityService securityService;

	@Autowired
	private APIController apiController;

	private String key;
	
    @Configuration
    static class ContextConfiguration {

    	@Bean
        @Primary //may omit this if this is the only SomeBean defined/visible
        public NetworkService networkService () {
            return new TestableNetworkService();
        }
    }

    @Before
	public void createUser() throws IOException, EndUserException {
		MockHttpServletRequest httpRequest = new MockHttpServletRequest("POST", "/v1/signup");
		Request request = mock(httpRequest);
		httpRequest.addParameter("username", USERNAME);
		httpRequest.addParameter("password", PASSWORD);
		httpRequest.addParameter("email", "tester@humanise.dk");
		try {
			key = apiController.authentication(request).getSecret();
		} catch (SecurityException e) {
			key = apiController.signup(request).getSecret();
		}
		request.commit();
	}
	
	@Test
	public void testSignup() throws IOException, EndUserException {
		MockHttpServletRequest httpRequest = new MockHttpServletRequest("POST", "/v1/signup");
		Request request = mock(httpRequest);
		try {
			apiController.signup(request);
			Assert.fail();
		} catch (IllegalRequestException e) {
			assertEquals(Error.noUsername.toString(), e.getCode());
		}
		httpRequest.addParameter("username", "someone");
		try {
			apiController.signup(request);
			Assert.fail();
		} catch (IllegalRequestException e) {
			assertEquals(Error.noPassword.toString(), e.getCode());
		}
		httpRequest.addParameter("password", "jfdskfjdsaljl");
		try {
			apiController.signup(request);
			Assert.fail();
		} catch (IllegalRequestException e) {
			assertEquals(Error.noEmail.toString(), e.getCode());
		}
		httpRequest.addParameter("email", "someone@somewhere.com");
		AuthenticationResponse response = apiController.signup(request);
		assertTrue(response.getSecret().length() > 10);
		request.commit();
	}

	@Test
	public void testAuthenticate() throws IOException, EndUserException {
		MockHttpServletRequest httpRequest = new MockHttpServletRequest("POST", "/v1.0/authentication");
		Request request = mock(httpRequest);
		httpRequest.addParameter("username", USERNAME);
		httpRequest.addParameter("password", PASSWORD);
		AuthenticationResponse authentication = apiController.authentication(request);
		assertTrue(authentication.getSecret().length() > 10);
		request.commit();
	}

	@Test
	public void testCreateAddress() throws IOException, EndUserException {
		MockHttpServletRequest httpRequest = new MockHttpServletRequest("POST", "/");
		Request request = mock(httpRequest);
		String url = "https://en.wikipedia.org/wiki/Knowledge";
		String quote = "knowledge is power";
		httpRequest.addParameter("url", url);
		httpRequest.addParameter("quote", quote);
		
		// Should fail without authorization
		try {
			apiController.addInternetAddress(request);
			Assert.fail();
		} catch (SecurityException e) {
			assertEquals(Error.userNotFound.toString(), e.getCode());
		}
		// Add authorization
		httpRequest.addHeader("Authorization", "Bearer " + key);
		securityService.ensureUserSession(request);
		
		InternetAddressApiPerspective response = apiController.addInternetAddress(request);
		assertEquals(url, response.getUrl());
		assertEquals("Knowledge - Wikipedia", response.getTitle());
		request.commit();
		
		httpRequest.removeAllParameters();
		APISearchResult result = apiController.knowledgeList(request);
		assertEquals(2, result.getTotalCount());

		KnowledgeListRow statement = firstRowByType(result, "Statement");
		assertEquals(quote, statement.getText());

		KnowledgeListRow address = firstRowByType(result, "InternetAddress");
		assertEquals("Knowledge - Wikipedia", address.getText());
		
		httpRequest.removeAllParameters();
		httpRequest.addParameter("id", String.valueOf(response.getId()));
		InternetAddressApiPerspective addressPerspective = apiController.viewAddress(request);
		assertEquals("", addressPerspective.getText().replaceAll("[\\s]+", "").substring(0, 30));
		request.commit();
	}

	private KnowledgeListRow firstRowByType(APISearchResult result, String type) {
		return result.getList().stream().filter(x -> x.getType().equals(type)).findFirst().get();
	}
	
	private Request mock(MockHttpServletRequest httpRequest) throws SecurityException {
		HttpServletResponse httpResponse = EasyMock.createMock(HttpServletResponse.class);
		MockHttpSession httpSession = new MockHttpSession();
		httpRequest.setSession(httpSession);
		httpRequest.setServletPath("/");
		Request request = Request.get(httpRequest, httpResponse);
		request.setOperationProvider(modelService);
		securityService.ensureUserSession(request);
		return request;
	}
}
