package dk.in2isoft.onlineobjects.modules.networking;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import dk.in2isoft.commons.lang.Files;
import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.commons.parsing.HTMLDocument;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.exceptions.IllegalRequestException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.InternetAddress;
import dk.in2isoft.onlineobjects.model.Property;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.modules.inbox.InboxService;
import dk.in2isoft.onlineobjects.services.StorageService;

public class InternetAddressService {
	
	StorageService storageService;
	NetworkService networkService;
	ModelService modelService;
	HTMLService htmlService;
	InboxService inboxService;

	public HTMLDocument getHTMLDocument(InternetAddress address, Privileged privileged) throws SecurityException, ModelException {

		File folder = storageService.getItemFolder(address);
		File original = new File(folder, "original");
		String encoding = address.getPropertyValue(Property.KEY_INTERNETADDRESS_ENCODING);
		if (!original.exists()) {
			NetworkResponse response = networkService.getSilently(address.getAddress());
			if (response != null && response.isSuccess()) {
				File temp = response.getFile();
				if (!Files.copy(temp, original)) {
					response.cleanUp();
					return null;
				}
				if (response.getEncoding()!=null) {
					encoding = response.getEncoding();
				}
				address.overrideFirstProperty(Property.KEY_INTERNETADDRESS_ENCODING, encoding);
				modelService.updateItem(address, privileged);
			}
		}
		if (Strings.isBlank(encoding)) {
			encoding = Strings.UTF8;
		}
		HTMLDocument htmlDocument = new HTMLDocument(Files.readString(original, encoding));
		htmlDocument.setOriginalUrl(address.getAddress());
		return htmlDocument;
	}
	
	public InternetAddress importAddress(String urlString, User user) throws ModelException, SecurityException, IllegalRequestException {
		if (Strings.isBlank(urlString)) {
			throw new IllegalRequestException("The url is empty");
		}
		urlString = urlString.trim();
		String url;
		try {
			URI uri = new URI(urlString);
			uri = networkService.resolveRedirects(uri);
			uri = networkService.removeTrackingParameters(uri);
			if (!networkService.isHttpOrHttps(uri)) {
				throw new IllegalRequestException("The scheme of '" + uri + "' is unsupported");
			}
			url = uri.toString();
		} catch (URISyntaxException e) {
			throw new IllegalRequestException("", e);
		}
		Query<InternetAddress> query = Query.after(InternetAddress.class).as(user).withField(InternetAddress.FIELD_ADDRESS, url);
		InternetAddress address = modelService.search(query).getFirst();
		if (address == null) {
			address = new InternetAddress();
			address.setAddress(url);
			HTMLDocument doc = htmlService.getDocumentSilently(url);
			if (doc != null) {
				address.setName(doc.getTitle());
			} else {
				address.setName(Strings.simplifyURL(url));
			}
			modelService.createItem(address, user);

			inboxService.add(user, address);
		}
		return address;
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
	
	public void setHtmlService(HTMLService htmlService) {
		this.htmlService = htmlService;
	}
}
