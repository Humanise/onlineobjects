package dk.in2isoft.onlineobjects.modules.webdav;

import java.io.File;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.DavServletRequest;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.DavSession;

public class BasicDavResourceFactory implements DavResourceFactory {

	private final File rootDirectory;

	public BasicDavResourceFactory() {
		// Define the root directory for your WebDAV resources
		this.rootDirectory = new File("/path/to/webdav/root");
		if (!rootDirectory.exists()) {
			rootDirectory.mkdirs();
		}
	}

	@Override
	public DavResource createResource(DavResourceLocator locator, DavSession session) throws DavException {
		return null;
	}

	@Override
	public DavResource createResource(DavResourceLocator locator, DavServletRequest request,
			DavServletResponse response) throws DavException {

		return null;
	}
}