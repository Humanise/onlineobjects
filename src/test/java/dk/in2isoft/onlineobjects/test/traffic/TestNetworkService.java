package dk.in2isoft.onlineobjects.test.traffic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import dk.in2isoft.commons.lang.Files;
import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.modules.networking.NetworkResponse;
import dk.in2isoft.onlineobjects.modules.networking.NetworkService;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;

public class TestNetworkService extends AbstractSpringTestCase {
	
	//private static Logger log = Logger.getLogger(TestNetworkService.class);
	
	@Autowired
	private NetworkService networkService;
	
	@Test
	public void testGetString() throws Exception {
		String string = networkService.getStringSilently("http://dev.humanise.dk/files/html/simple_utf8.html");
		assertTrue(Strings.isNotBlank(string));
	}
	
	@Test
	public void testGetStringFail() throws Exception {
		assertNull(networkService.getStringSilently("http://dev.humanise.dk/jashfjkafhdasjfahkfdsd.html"));
		assertNull(networkService.getStringSilently(null));
		assertNull(networkService.getStringSilently(""));
		assertNull(networkService.getStringSilently(""));
		assertNull(networkService.getStringSilently("fasjfhajkfhdka"));
	}

	@Test
	public void testResolveUrl() throws Exception {
		URL url = new URL("http://feedproxy.google.com/~r/alistapart/main/~3/BU6iDJCwiVY/i-dont-need-help");
		URL real = networkService.resolveToReal(url);
		assertEquals("https://alistapart.com/article/i-dont-need-help", real.toString());
	}

	@Test
	public void testResponseUTF8() throws Exception {
		NetworkResponse response = networkService.get("http://dev.humanise.dk/files/html/simple_utf8.html");
		assertTrue(response.isSuccess());
		assertEquals("text/html", response.getMimeType());
		assertEquals(Strings.UTF8,response.getEncoding());
		assertEquals(NetworkResponse.State.SUCCESS, response.getState());

		String string = Files.readString(response.getFile(), response.getEncoding());
		assertTrue(string.contains(Strings.UNICODE_AE_LARGE+Strings.UNICODE_OE_LARGE+Strings.UNICODE_AA_LARGE));
	}

	@Test
	public void testResponseISO() throws Exception {
		NetworkResponse response = networkService.get("http://dev.humanise.dk/files/html/simple_iso-8859-1.html");
		assertTrue(response.isSuccess());
		assertEquals("text/html", response.getMimeType());
		assertEquals(Strings.ISO_8859_1,response.getEncoding());
		assertEquals(NetworkResponse.State.SUCCESS, response.getState());
				
		String string = Files.readString(response.getFile(), response.getEncoding());
		assertTrue(string.contains(Strings.UNICODE_AE_LARGE+Strings.UNICODE_OE_LARGE+Strings.UNICODE_AA_LARGE));
	}

	public void setNetworkService(NetworkService networkService) {
		this.networkService = networkService;
	}
}