package dk.in2isoft.onlineobjects.apps.reader.views;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import dk.in2isoft.commons.jsf.AbstractView;
import dk.in2isoft.commons.parsing.HTMLDocument;
import dk.in2isoft.commons.xml.DocumentCleaner;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Pair;
import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.model.InternetAddress;
import dk.in2isoft.onlineobjects.model.Property;
import dk.in2isoft.onlineobjects.modules.information.InspectingContentExtractor;
import dk.in2isoft.onlineobjects.modules.language.TextDocumentAnalytics;
import dk.in2isoft.onlineobjects.modules.language.TextDocumentAnalyzer;
import dk.in2isoft.onlineobjects.modules.networking.InternetAddressService;
import dk.in2isoft.onlineobjects.ui.Request;
import nu.xom.Document;
import nu.xom.tests.XOMTestCase;

public class ReaderExtractionView extends AbstractView implements InitializingBean {
	
	private ModelService modelService;
	private TextDocumentAnalyzer textDocumentAnalyzer;
	private InternetAddressService internetAddressService;
	
	private InternetAddress internetAddress;
	private String rawHtml;
	private String cleaned;
	private String extracted;
		
	public ReaderExtractionView() {
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		Request request = getRequest();
		Long id = request.getId();
		Privileged privileged = request.getSession();
		internetAddress = modelService.getRequired(InternetAddress.class, id, privileged);
		
		HTMLDocument htmlDocument = internetAddressService.getHTMLDocument(internetAddress, privileged);
		
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
		
	public void setTextDocumentAnalyzer(TextDocumentAnalyzer textDocumentAnalyzer) {
		this.textDocumentAnalyzer = textDocumentAnalyzer;
	}
	
	public void setInternetAddressService(InternetAddressService internetAddressService) {
		this.internetAddressService = internetAddressService;
	}
}
