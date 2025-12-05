package dk.in2isoft.onlineobjects.apps.developer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.onlineobjects.modules.intelligence.LanguageModel;
import org.springframework.beans.factory.annotation.Autowired;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.apps.ApplicationController;
import dk.in2isoft.onlineobjects.core.Path;
import dk.in2isoft.onlineobjects.core.Path.Method;
import dk.in2isoft.onlineobjects.core.View;
import dk.in2isoft.onlineobjects.core.exceptions.BadRequestException;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.NotFoundException;
import dk.in2isoft.onlineobjects.services.DispatchingService;
import dk.in2isoft.onlineobjects.ui.Request;

public class DeveloperController extends ApplicationController {

	private static Logger log = LogManager.getLogger(DeveloperController.class);

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

	@Path(of = "/documents")
	@View(ui = {"documents", "documents.xml"})
	public void documents(Request request) {
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
		throw new NotFoundException();
	}

	@Path(exactly={"bad-request"})
	public void throwBadRequest(Request request) throws IOException, EndUserException {
		throw new BadRequestException();
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
	public void promptStream(Request request) throws IOException, BadRequestException {
		promptStreamPost(request);
	}

	@Path(exactly={"intelligence", "prompt", "stream"}, method = Method.POST)
	public void promptStreamPost(Request request) throws IOException, BadRequestException {
		String prompt = request.getString("prompt", "Missing prompt");
		String model = request.getString("model");
		LanguageModel m = intelligence.getModelById(model).orElse(intelligence.getDefaultModel());
		OutputStream stream = request.getResponse().getOutputStream();
		intelligence.prompt(prompt, m, stream);
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

	@Path(expression = "/dav")
	public void davRoot(Request request) throws IOException {
		dav(request);
	}

	@Path(expression = "/dav<any>")
	public void dav(Request request) throws IOException {
		HttpServletResponse response = request.getResponse();
		HttpServletRequest httpServletRequest = request.getRequest();
		String method = httpServletRequest.getMethod();
		if ("OPTIONS".equals(method)) {
	        response.addHeader("Allow", "GET, PUT, DELETE, PROPFIND, MKCOL, OPTIONS");
	        response.addHeader("DAV", "1");
		} else if ("PROPFIND".equals(method)) {
			response.addHeader("Content-Type", "application/xml; charset=utf-8");
			response.getWriter().write("""
					<?xml version="1.0" encoding="utf-8"?>
	<D:multistatus xmlns:D="DAV:">
	<D:response>
	    <D:href>/dav/</D:href>
	    <D:propstat>
	      <D:prop>
	        <D:displayname>documents</D:displayname>
	        <D:resourcetype>
	          <D:collection/>
	        </D:resourcetype>
	      </D:prop>
	      <D:status>HTTP/1.1 200 OK</D:status>
	    </D:propstat>
	  </D:response>
		<D:response>
    <D:href>/dav/notes.txt</D:href>
    <D:propstat>
      <D:prop>
        <D:displayname>notes.txt</D:displayname>
        <D:getcontentlength>234567</D:getcontentlength>
        <D:getlastmodified>Tue, 10 Sep 2024 14:32:00 GMT</D:getlastmodified>
        <D:resourcetype/>
      </D:prop>
      <D:status>HTTP/1.1 200 OK</D:status>
    </D:propstat>
  </D:response>
    </D:multistatus>
					""".trim());

		} else if ("GET".equals(method)) {
			response.setHeader("Content-Type", "text/plain");
			response.getWriter().println("[ ] Work");
			response.getWriter().println("[ ] Eat");
			response.getWriter().println("[ ] Sleep");
		} else {
			log.warn("Unknown webdav request...");
			log.warn(request.getRequest());
			log.warn(request.getBody());
		}
	}
}
