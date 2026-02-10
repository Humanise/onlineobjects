package dk.in2isoft.onlineobjects.apps.front;

import java.io.IOException;

import dk.in2isoft.onlineobjects.core.Path;
import dk.in2isoft.onlineobjects.core.View;
import dk.in2isoft.onlineobjects.ui.Request;

public class FrontController extends FrontControllerBase {

	@Path(expression="/<language>/about")
	public void about(Request request) throws IOException {
		String url = "https://info.onlineobjects.com/"+request.getLocalPath()[0]+"/";
		request.movedPermanently(url);
	}

	@Path(expression="/")
	@View(jsf="front.xhtml")
	public void front(Request request) {}

	@Path(expression="/<language>")
	@View(jsf="front.xhtml")
	public void frontLanguage(Request request) {}

	@Path(expression="/<language>/mac")
	@View(jsf="mac.xhtml")
	public void mac(Request request) {}

	@Path("/<language>/<folder>/<integer>")
	@View("entity.xhtml")
	public void entity(Request request) {}

}
