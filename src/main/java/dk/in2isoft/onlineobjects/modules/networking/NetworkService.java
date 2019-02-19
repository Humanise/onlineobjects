package dk.in2isoft.onlineobjects.modules.networking;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Sets;

import dk.in2isoft.commons.http.HeaderUtil;
import dk.in2isoft.commons.lang.Files;
import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.modules.networking.NetworkResponse.State;

public class NetworkService {
	
	private static final Logger log = LogManager.getLogger(NetworkService.class);
	
	private static final Set<String> TRACKING_PARAMS = Sets.newHashSet("utm_source","utm_medium","utm_campaign","utm_term","utm_content"); 
	
	public String getStringSilently(String url) {
		try {
			return getString(new URL(url));
		} catch (IOException | URISyntaxException e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	public String getString(URL url) throws IOException, URISyntaxException {
		NetworkResponse response = null;
		try {
			response = get(url.toURI());
			if (response.isSuccess()) {
				String string = Files.readString(response.getFile(),response.getEncoding());
				return string;
			}
		} catch (IOException | URISyntaxException e) {
			throw e;
		} finally {
			response.cleanUp();
		}
		return null;
	}
	
	public NetworkResponse get(String spec) throws URISyntaxException, IOException {
		return get(new URI(spec));
	}
	
	public NetworkResponse getSilently(String url) {
		try {
			return get(new URI(url));
		} catch (IOException | URISyntaxException e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	public URI resolveRedirects(URI url) {
		int i = 0;
		while (i < 5) {
			i++;
			try {
				HttpClient client = HttpClients.custom().disableRedirectHandling().build();
				HttpContext context = new BasicHttpContext();
	
				// connect and receive 
				HttpGet get = new HttpGet(url);
				HttpResponse response = client.execute(get, context);
	
				// obtain redirect target
				Header locationHeader = response.getFirstHeader("location");
				if (locationHeader!=null) {
					String value = locationHeader.getValue();
					if (value.startsWith("/")) {
						url = new URIBuilder().setPath(value).setScheme(url.getScheme()).setHost(url.getHost()).setPort(url.getPort()).build();
					} else {
						url = new URI(locationHeader.getValue());
					}
					log.info("Redirect: "+url);
				} else {
					break;
				}
				
			} catch (URISyntaxException | IOException e) {
				break;
			}
		}
		return url;
	}

	/**
	 * Removes tracking parameters and #fragments from an URL
	 * @param resolved
	 * @return
	 * @throws URISyntaxException
	 * @throws MalformedURLException
	 */
	public URI removeTrackingParameters(URI uri) {
		try {
			List<NameValuePair> parameters = URLEncodedUtils.parse(uri, Strings.UTF8);
			parameters = parameters.stream().filter(pair -> !TRACKING_PARAMS.contains(pair.getName())).collect(Collectors.toList());
			URIBuilder builder = new URIBuilder(uri).clearParameters();
			if (!parameters.isEmpty()) {
				builder.addParameters(parameters);
			}
			return builder.build();
		} catch (URISyntaxException e) {
			log.warn("Unable to remove tracking params", e);
		}
		return uri;
	}

	public NetworkResponse get(URI uri) throws IOException {
		return get(uri, 5);
	}

	private NetworkResponse get(URI uri, int redirects) throws IOException {
		NetworkResponse response = new NetworkResponse();
		InputStream input = null;
		InputStreamReader reader = null;
		HttpGet method = null;
		OutputStream output = null;
		CloseableHttpClient client = null;
		try {
			String encoding = null;
			String contentType = null;
			if (isHttpOrHttps(uri)) {
				client = HttpClients.createDefault();
				method = new HttpGet(uri);
				if (uri.getHost().endsWith("sundhed.dk")) {
					method.addHeader("User-Agent", "Googlebot/2.1 (+http://www.googlebot.com/bot.html)");					
				} else {
					method.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36");
				}
				CloseableHttpResponse res = client.execute(method);
				if (redirects > 0) {
					Header locationHeader = res.getFirstHeader("location");
					if (locationHeader!=null) {
						try {
							URI red = new URI(locationHeader.getValue());
							return get(red, redirects - 1);
						} catch (URISyntaxException e) {
							
						}
						
					}
				}
				int code = res.getStatusLine().getStatusCode();
				if (code == 200) {
					org.apache.http.Header header = res.getFirstHeader("Content-Type");
					if (header!=null) {
						contentType = HeaderUtil.getContentTypesMimeType(header.getValue());
						String contentTypeEncoding = HeaderUtil.getContentTypeEncoding(header.getValue());
						if (contentTypeEncoding!=null) {
							encoding = contentTypeEncoding;
						}
					}
					response.setState(State.SUCCESS);
				}
				HttpEntity entity = res.getEntity();
				input = entity.getContent();
			} else {
				input = uri.toURL().openStream();
				response.setState(State.SUCCESS);
			}

			File file = File.createTempFile("networkservice", "tmp");
			file.deleteOnExit();
			output = new FileOutputStream(file);
			reader = new InputStreamReader(input,encoding==null ? Strings.UTF8 : encoding);
			IOUtils.copy(input, output);
			response.setMimeType(contentType);
			response.setEncoding(encoding);
			response.setFile(file);
			response.setUri(uri);
			return response;
		} catch (IOException e) {
			throw e;
		} finally {
			IOUtils.closeQuietly(input);
			IOUtils.closeQuietly(reader);
			if (method!=null) {
				method.releaseConnection();
			}
			if (client != null) client.close();
		}		
	}

	public boolean isHttpOrHttps(URI uri) {
		String scheme = uri.getScheme();
		return scheme!=null && (scheme.equals("http") || scheme.equals("https"));
	}
	
}
