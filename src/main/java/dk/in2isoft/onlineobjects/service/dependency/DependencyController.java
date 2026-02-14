package dk.in2isoft.onlineobjects.service.dependency;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dk.in2isoft.commons.util.RestUtil;
import dk.in2isoft.onlineobjects.core.Path;
import dk.in2isoft.onlineobjects.core.exceptions.NotFoundException;
import dk.in2isoft.onlineobjects.service.ServiceController;
import dk.in2isoft.onlineobjects.ui.DependencyService;
import dk.in2isoft.onlineobjects.ui.Request;

public class DependencyController extends ServiceController {

	private static final String SCRIPT_PATH = "/(<integer>)/(<integer>).js";

	private static final String STYLE_PATH = "/(<integer>)/(<integer>).css";
	private final Logger clientSideLog = LogManager.getLogger("client");

	private DependencyService dependencyService;

	private Pattern scriptPattern = RestUtil.compile(SCRIPT_PATH);
	private Pattern stylePattern = RestUtil.compile(STYLE_PATH);

	public DependencyController() {
		super("dependency");
	}

	@Path(expression=SCRIPT_PATH)
	public void script(Request request) throws IOException, NotFoundException {
		String path = request.getLocalPathAsString();
		Matcher matcher = scriptPattern.matcher(path);
		if (matcher.matches()) {
			String stamp = matcher.group(1);
			String hash = matcher.group(2);
			dependencyService.respondScripts(stamp,hash,request);
		} else {
			throw new NotFoundException();
		}
	}

	@Path(expression=STYLE_PATH)
	public void style(Request request) throws IOException, NotFoundException {
		String path = request.getLocalPathAsString();
		Matcher matcher = stylePattern.matcher(path);
		if (matcher.matches()) {
			String stamp = matcher.group(1);
			String hash = matcher.group(2);
			dependencyService.respondStyles(stamp,hash,request);
		} else {
			throw new NotFoundException();
		}
	}

	@Path(exactly="error")
	public void logClientError(Request request) throws IOException, NotFoundException {
		String message = request.getString("message");
		String line = request.getString("line");
		String column = request.getString("column");
		String file = request.getString("file");
		String url = request.getString("url");
		String agent = request.getRequest().getHeader("User-Agent");
		clientSideLog.error("Client side error: msg:{}, line:{}, column:{}, file:{}, url:{}, agent:{}", message, line, column, file, url, agent);
	}

	public void setDependencyService(DependencyService dependencyService) {
		this.dependencyService = dependencyService;
	}
}
