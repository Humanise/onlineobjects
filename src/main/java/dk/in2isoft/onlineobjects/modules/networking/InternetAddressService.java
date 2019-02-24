package dk.in2isoft.onlineobjects.modules.networking;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import dk.in2isoft.commons.lang.Files;
import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.commons.parsing.HTMLDocument;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.exceptions.ContentNotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.IllegalRequestException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.InternetAddress;
import dk.in2isoft.onlineobjects.model.Property;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.modules.inbox.InboxService;
import dk.in2isoft.onlineobjects.services.StorageService;

public class InternetAddressService {
	
	private StorageService storageService;
	private NetworkService networkService;
	private ModelService modelService;
	private InboxService inboxService;

	public HTMLDocument getHTMLDocument(InternetAddress address, Operator privileged) throws SecurityException, ModelException {
		File original = getContent(address, privileged);
		if (original == null) {
			return null;
		}
		String encoding = address.getPropertyValue(Property.KEY_INTERNETADDRESS_ENCODING);
		if (Strings.isBlank(encoding)) {
			encoding = Strings.UTF8;
		}
		HTMLDocument htmlDocument = new HTMLDocument(Files.readString(original, encoding));
		htmlDocument.setOriginalUrl(address.getAddress());
		return htmlDocument;
	}
	
	public File getContent(InternetAddress address, Operator privileged) throws SecurityException, ModelException {
		File folder = storageService.getItemFolder(address);
		File original = new File(folder, "original");
		if (!original.exists()) {
			NetworkResponse response = networkService.getSilently(address.getAddress());
			if (response != null && response.isSuccess()) {
				File temp = response.getFile();
				if (!Files.copy(temp, original)) {
					response.cleanUp();
					return null;
				}
				String encoding = null;
				if (response.getEncoding()!=null) {
					encoding = response.getEncoding();
					address.overrideFirstProperty(Property.KEY_INTERNETADDRESS_ENCODING, encoding);
				} else {
					address.removeProperties(Property.KEY_INTERNETADDRESS_ENCODING);
				}
				modelService.update(address, privileged);
			}
		}
		
		return original;
	}

	public InternetAddress create(String url, String title, User user, Operator operator) throws IllegalRequestException, ModelException, SecurityException, ContentNotFoundException {
		if (Strings.isBlank(url)) {
			throw new IllegalRequestException("The url is empty");
		}
		URI uri = asURI(url);
		// First check if it exists
		InternetAddress address = findExisting(operator, url);
		if (address != null) {
			return address;
		}

		// Download and redirect
		String resolvedUrl;
		NetworkResponse response = null;
		try {
			response = networkService.get(uri);
			uri = response.getUri();
			uri = networkService.removeTrackingParameters(uri);
			verifyAsHttp(uri);
			resolvedUrl = uri.toString();
			
			// If the URL has changed -> check again
			if (!url.equals(resolvedUrl)) {
				address = findExisting(operator, resolvedUrl);
				if (address != null) {
					return address;
				}
			}
		} catch (IOException e) {
			resolvedUrl = uri.toString();
		}
		address = new InternetAddress();
		address.setAddress(resolvedUrl);
		
		if (Strings.isNotBlank(title)) {
			address.setName(title.trim());
		} else if (response!=null) {
			address.setName(getTitle(response, resolvedUrl));
		}
		if (response!=null && response.getEncoding() != null) {
			address.overrideFirstProperty(Property.KEY_INTERNETADDRESS_ENCODING, response.getEncoding());
		}
		modelService.create(address, operator);
		inboxService.add(user, address, operator);
		if (response != null) {
			changeOriginal(address,response.getFile());
		}
		return address;
	}

	private String getTitle(NetworkResponse response, String resolvedUrl) {
		if (response != null) {
			// TODO: Are we sure that a response has a file?
			String html = Files.readString(response.getFile(), response.getEncoding());
			if (Strings.isNotBlank(html)) {
				// TODO: Try to cache the parsed XHTML
				// TODO: Keep track of state of InternetAddress (downloaded, failed etc)
				HTMLDocument doc = new HTMLDocument(html);
				String title = doc.getTitle();
				if (Strings.isNotBlank(title)) {
					return title.trim();
				}
			}
		}
		return Strings.simplifyURL(resolvedUrl);
	}

	private boolean changeOriginal(InternetAddress address, File temp) {
		File folder = storageService.getItemFolder(address);
		File original = new File(folder, "original");
		return Files.copy(temp, original);
	}

	private URI asURI(String url) throws IllegalRequestException {
		URI uri;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			throw new IllegalRequestException("Invalid URL syntax", e);
		}
		verifyAsHttp(uri);
		return uri;
	}
	
	private InternetAddress findExisting(Operator operator, String url) {
		Query<InternetAddress> query = Query.after(InternetAddress.class).as(operator).withField(InternetAddress.FIELD_ADDRESS, url);
		InternetAddress address = modelService.search(query, operator).getFirst();
		return address;
	}

	private void verifyAsHttp(URI uri) throws IllegalRequestException {
		if (!networkService.isHttpOrHttps(uri)) {
			throw new IllegalRequestException("The scheme of '" + uri + "' is unsupported");
		}
	}
	
	// Wiring...
	
	public void setStorageService(StorageService storageService) {
		this.storageService = storageService;
	}
	
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
	
	public void setNetworkService(NetworkService networkService) {
		this.networkService = networkService;
	}
	
	public void setInboxService(InboxService inboxService) {
		this.inboxService = inboxService;
	}
	
}
