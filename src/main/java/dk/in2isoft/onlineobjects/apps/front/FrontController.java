package dk.in2isoft.onlineobjects.apps.front;

import java.io.IOException;

import dk.in2isoft.onlineobjects.core.Path;
import dk.in2isoft.onlineobjects.ui.Request;

public class FrontController extends FrontControllerBase {

	@Path(expression="/(en|da)/about")
	public void about(Request request) throws IOException {
		String url = "https://info.onlineobjects.com/"+request.getLocalPath()[0]+"/";
		request.movedPermanently(url);
	}
}
