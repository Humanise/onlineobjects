package dk.in2isoft.onlineobjects.modules.webdav;

import javax.servlet.annotation.WebServlet;

import org.apache.jackrabbit.webdav.DavLocatorFactory;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavSessionProvider;
import org.apache.jackrabbit.webdav.WebdavRequest;
import org.apache.jackrabbit.webdav.server.AbstractWebdavServlet;

@WebServlet(urlPatterns = "/webdav/*")
public class SimpleWebdavServlet extends AbstractWebdavServlet {

    private DavSessionProvider sessionProvider;
    private DavResourceFactory resourceFactory;

    public SimpleWebdavServlet() {
        // Create a simple file-system based implementation
        this.sessionProvider = new BasicDavSessionProvider();
        this.resourceFactory = new BasicDavResourceFactory();
    }

    @Override
    public DavSessionProvider getDavSessionProvider() {
        return sessionProvider;
    }

	@Override
	protected boolean isPreconditionValid(WebdavRequest request, DavResource resource) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setDavSessionProvider(DavSessionProvider davSessionProvider) {
		this.sessionProvider = davSessionProvider;
	}

	@Override
	public DavLocatorFactory getLocatorFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLocatorFactory(DavLocatorFactory locatorFactory) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DavResourceFactory getResourceFactory() {
		return resourceFactory;
	}

	@Override
	public void setResourceFactory(DavResourceFactory resourceFactory) {
		this.resourceFactory = resourceFactory;
	}
}