package dk.in2isoft.onlineobjects.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dk.in2isoft.commons.jsf.DependencyGraph;
import dk.in2isoft.onlineobjects.core.exceptions.ContentNotFoundException;
import dk.in2isoft.onlineobjects.services.ConfigurationService;

public class DependencyService {
	
	public static final String[] TAIL_PATH = new String[] {"core","js","tail.js"};
	private Map<String,String[]> storedScripts = new HashMap<>();
	private Map<String,String[]> storedStyles = new HashMap<>();
	private static final Logger log = LogManager.getLogger(DependencyService.class);

	private ConfigurationService configurationService;

	public String handleScripts(DependencyGraph graph) {
		String[] scripts = graph.getScripts();
		String hash = buildHash(scripts);
		storedScripts.put(hash, scripts);
		return "/service/dependency/"+configurationService.getDeploymentId()+"/"+hash+".js";
	}

	public String handleStyles(DependencyGraph graph) {
		String[] styles = graph.getStyles();
		String hash = buildHash(styles);
		storedStyles.put(hash, styles);
		return "/service/dependency/"+configurationService.getDeploymentId()+"/"+hash+".css";
	}

	private String buildHash(String[] strings) {
		StringBuilder sb = new StringBuilder();
		for (String string : strings) {
			sb.append(string);
		}
		String hash = String.valueOf(Math.abs(sb.toString().hashCode()));
		return hash;
	}

	public void respondScripts(String stamp, String hash, Request request) throws ContentNotFoundException, IOException {
		String[] urls = storedScripts.get(hash);
		ScriptWriter w = new ScriptWriter(request, configurationService);
		if (urls==null) {
			if (w.writeCache(stamp, hash)) {
				log.info("Served cached JS: {}.{}.js", stamp, hash);
				return;
			} else {
				throw new ContentNotFoundException();
			}
		}
		List<String[]> paths = new ArrayList<>();
		for (String url : urls) {
			paths.add(url.split("\\/"));
		}
		paths.add(TAIL_PATH);
		w.write(paths, hash);
	}

	public void respondStyles(String stamp, String hash, Request request) throws ContentNotFoundException, IOException {
		String[] urls = storedStyles.get(hash);
		StylesheetWriter w = new StylesheetWriter(request, configurationService);
		if (urls==null) {
			if (w.writeCache(stamp, hash)) {
				log.info("Served cached CSS: {}.{}.css", stamp, hash);
				return;
			} else {
				throw new ContentNotFoundException();
			}
		}
		List<String[]> paths = new ArrayList<>();
		for (String url : urls) {
			paths.add(url.split("\\/"));
		}
		w.write(paths, hash);
	}
	
	public static String pathToUrl(String path) {
		if (path.startsWith("/apps/")) {
			return path.replaceFirst("/apps/","/");
		}
		if (path.startsWith("/services/")) {
			return path.replaceFirst("/services/","/service/");
		}
		return path;
	}
	
	// Wiring...
	
	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}
}
