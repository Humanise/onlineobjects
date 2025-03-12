package dk.in2isoft.onlineobjects.apps.developer;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.onlineobjects.modules.index.SolrService;
import org.onlineobjects.modules.index.SolrService.Collection;
import org.onlineobjects.modules.intelligence.Intelligence;
import org.springframework.beans.factory.annotation.Autowired;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.apps.ApplicationController;
import dk.in2isoft.onlineobjects.core.Path;
import dk.in2isoft.onlineobjects.core.Path.Method;
import dk.in2isoft.onlineobjects.core.View;
import dk.in2isoft.onlineobjects.core.exceptions.ContentNotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.IllegalRequestException;
import dk.in2isoft.onlineobjects.services.DispatchingService;
import dk.in2isoft.onlineobjects.ui.Request;

public class DeveloperController extends ApplicationController {
	
	@Autowired
	Intelligence intelligence;
	
	@Autowired
	SolrService solr;

	public DeveloperController() {
		super("developer");
	}

	@Path(expression = "/")
	@View(jsf = "index.xhtml")
	public void front(Request request) {
		request.setVariable("test", "hest");
	}

	@Path(exactly = {"components.html"})
	@View(jsf = "components.xhtml")
	public void components(Request request) {
	}

	@Path(exactly = {"jsf.html"})
	@View(jsf = "jsf.xhtml")
	public void jsf(Request request) {
	}

	@Path(exactly = {"finder.html"})
	@View(jsf = "finder.xhtml")
	public void finder(Request request) {}
	
	@Path(exactly = {"users"})
	@View(jsf = "users.xhtml")
	public void users(Request request) {
	}

	@Path(exactly = {"hui-test"})
	@View(ui = {"hui.xml"})
	public void hui(Request request) {
	}

	@Path(exactly = {"settings"})
	@View(ui = {"settings.xml"})
	public void settings(Request request) {
	}

	@Path(expression = "/intelligence")
	@View(jsf = "intelligence.xhtml")
	public void intelligence(Request request) {}

	@Path(expression = "/intelligence-raw")
	@View(ui = {"intelligence.xml"})
	public void intelligenceRaw(Request request) {}

	@Path(exactly = "mail")
	public void mail(Request request) throws IOException {
		File file = new File(configurationService.getTempDir(), request.getString("address")+".html");
		DispatchingService.pushFile(request.getResponse(), file);
	}

	public List<Locale> getLocales() {
		return null;
	}
	
	@Override
	public boolean isAllowed(Request request) {
		return configurationService.isDevelopmentMode() || configurationService.isTestMode();
	}

	@Path(exactly={"not-found"})
	public void throwNotFound(Request request) throws IOException, EndUserException {
		throw new ContentNotFoundException();
	}
	
	@Path(exactly={"bad-request"})
	public void throwBadRequest(Request request) throws IOException, EndUserException {
		throw new IllegalRequestException();
	}

	@Path(exactly={"settings", "data"}, method = Method.POST)
	public void saveSettings(Request request) {
		request.optionalBoolean("errors").ifPresent(value -> {
			configurationService.setSimulateSporadicServerError(value);
		});
		request.optionalBoolean("slow").ifPresent(value -> {
			configurationService.setSimulateSlowRequest(value);
		});
	}

	@Path(exactly={"settings", "data"}, method = Method.GET)
	public Map<String,Object> readSettings(Request request) {
		return Map.of(
			"errors", configurationService.isSimulateSporadicServerError(),
			"slow", configurationService.isSimulateSlowRequest()
		);
	}

	@Path(exactly={"intelligence", "vectorize"}, method = Method.GET)
	public List<Double> vectorize(Request request) {
		return intelligence.vectorize(request.getString("text"));
	}

	@Path(exactly={"intelligence", "prompt"}, method = Method.GET)
	public void prompt(Request request) throws IOException {
		String prompt = intelligence.prompt(request.getString("prompt"));
		request.getResponse().getWriter().print(prompt);
	}

	@Path(exactly={"intelligence", "prompt", "stream"}, method = Method.GET)
	public void promptStream(Request request) throws IOException {
		intelligence.streamPrompt(request.getString("prompt"), request.getResponse().getOutputStream());
	}

	@Path(exactly={"intelligence", "summarize"}, method = Method.GET)
	public void summarize(Request request) throws IOException {
		intelligence.summarize(request.getString("text"), request.getResponse().getOutputStream());
	}

	@Path(expression = "/solr", method = Method.GET)
	public Object solr(Request request) throws SolrServerException, IOException {
		SolrClient client = solr.getClient();
		final SolrQuery query = new SolrQuery("*:*");
		query.addField("id");
		query.addField("title");
		query.setSort("id", ORDER.asc);
		query.setRows(10);
		QueryResponse response = client.query("knowledge", query);
		SolrDocumentList results = response.getResults();
		return results;
	}

	@Path(expression = "/solr", method = Method.POST)
	public void putSolr(Request request) throws SolrServerException, IOException {
		var doc = new SolrInputDocument();
		doc.addField("id", Strings.generateRandomString(10));
		doc.addField("title", Strings.generateRandomString(10));
		doc.addField("vector", intelligence.vectorize("test"));
		solr.add(Collection.knowledge, doc);
	}

}
