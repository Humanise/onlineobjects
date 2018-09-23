package dk.in2isoft.onlineobjects.test.traffic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import dk.in2isoft.commons.lang.Files;
import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.modules.networking.NetworkResponse;
import dk.in2isoft.onlineobjects.modules.networking.NetworkService;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;

public class TestNetworkService extends AbstractSpringTestCase {
	
	//private static Logger log = LogManager.getLogger(TestNetworkService.class);
	
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
		URI url = new URI("http://feedproxy.google.com/~r/alistapart/main/~3/BU6iDJCwiVY/i-dont-need-help");
		URI real = networkService.resolveRedirects(url);
		assertEquals("https://alistapart.com/article/i-dont-need-help", real.toString());
	}
	
	

	@Test
	public void testResolveUrlAgain() throws Exception {
		URI url = new URI("http://feedproxy.google.com/~r/37signals/beMH/~3/xozycK64-YI/the-richest-man-in-town-f115f0eb227");
		URI real = networkService.resolveRedirects(url);
		assertTrue(real.toString().startsWith("https://m.signalvnoise.com/the-richest-man-in-town-f115f0eb227"));
	}

	@Test
	public void testResolveInfinite() throws Exception {
		URI url = new URI("http://dev.humanise.dk/redirect.php?infinite=true");
		URI resolved = networkService.resolveRedirects(url);
		assertEquals("http://dev.humanise.dk/redirect.php?infinite=true", resolved.toString());
	}

	@Test
	public void testResolveCountdown() throws Exception {
		URI url = new URI("http://dev.humanise.dk/redirect.php?countdown=10");
		URI resolved = networkService.resolveRedirects(url);
		assertEquals("http://dev.humanise.dk/redirect.php?countdown=5", resolved.toString());
	}
	
	@Test
	public void testRemoveTrackingParameters() throws MalformedURLException, URISyntaxException {
		URI url = new URI("https://www.nngroup.com/articles/cards-component/?utm_source=Alertbox&im=ok&utm_campaign=43da43b3f9-Cards_UI_Component_Chinese+Complex_2016_11_07&utm_medium=email&utm_term=0_7f29a2b335-43da43b3f9-40181465#hello");
		URI cleaned = networkService.removeTrackingParameters(url);
		assertEquals(new URI("https://www.nngroup.com/articles/cards-component/?im=ok#hello"), cleaned);
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