package dk.in2isoft.onlineobjects.apps.knowledge.views;

import org.springframework.beans.factory.InitializingBean;

import dk.in2isoft.commons.jsf.AbstractView;
import dk.in2isoft.commons.parsing.HTMLDocument;
import dk.in2isoft.commons.xml.DocumentCleaner;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.model.InternetAddress;
import dk.in2isoft.onlineobjects.modules.information.InspectingContentExtractor;
import dk.in2isoft.onlineobjects.modules.networking.InternetAddressService;
import dk.in2isoft.onlineobjects.ui.Request;
import nu.xom.Document;

public class KnowledgeExtractionView extends AbstractView implements InitializingBean {

	private ModelService modelService;
	private InternetAddressService internetAddressService;

	private InternetAddress internetAddress;
	private String rawHtml;
	private String cleaned;
	private String extracted;

	@Override
	protected void before(Request request) throws Exception {
		Long id = request.getId();
		internetAddress = modelService.getRequired(InternetAddress.class, id, request);

		HTMLDocument htmlDocument = internetAddressService.getHTMLDocument(internetAddress, request);

		DocumentCleaner cleaner = new DocumentCleaner();
		cleaner.setUrl(htmlDocument.getOriginalUrl());
		Document xom = (Document) htmlDocument.getXOMDocument().copy();
		cleaner.clean(xom);
		this.cleaned = xom.toXML();

		this.rawHtml = htmlDocument.getRawString();

		InspectingContentExtractor extractor = new InspectingContentExtractor();
		Document extract = extractor.extract(htmlDocument.getXOMDocument());
		this.extracted = extract.toXML();
	}

	public String getRawHtml() {
		return this.rawHtml;
	}

	public String getCleaned() {
		return cleaned;
	}

	public String getExtracted() {
		return extracted;
	}


	// Wiring...

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	public void setInternetAddressService(InternetAddressService internetAddressService) {
		this.internetAddressService = internetAddressService;
	}
}
