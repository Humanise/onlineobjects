package dk.in2isoft.onlineobjects.modules.webdav;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavSessionProvider;
import org.apache.jackrabbit.webdav.WebdavRequest;

public class BasicDavSessionProvider implements DavSessionProvider {


	@Override
	public boolean attachSession(WebdavRequest request) throws DavException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void releaseSession(WebdavRequest request) {
		// TODO Auto-generated method stub
		
	}
}