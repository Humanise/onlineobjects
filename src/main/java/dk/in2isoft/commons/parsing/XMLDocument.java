package dk.in2isoft.commons.parsing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xerces.dom.DOMImplementationImpl;
import org.w3c.dom.Document;

import dk.in2isoft.commons.xml.DOM;
import nu.xom.converters.DOMConverter;

public class XMLDocument extends TextDocument {


	private static Logger log = LogManager.getLogger(XMLDocument.class);
    private Document DOMDocument;
    private nu.xom.Document XOMDocument;

    public XMLDocument(String raw) {
    	super(raw);
    }

    public XMLDocument(nu.xom.Document doc) {
    	super(null);
    	XOMDocument = doc;
    }

    public Document getDOMDocument() {
		if (DOMDocument==null) {
            nu.xom.Document xom = getXOMDocument();
            if (xom!=null) {
            	DOMDocument = DOMConverter.convert(xom,DOMImplementationImpl.getDOMImplementation());
            	if (DOMDocument==null) {
            		log.info(xom.toXML());
            	}
            }
        }
	    return DOMDocument;
	}

	public nu.xom.Document getXOMDocument() {
		if (XOMDocument == null) {
			String rawString = getRawString();
			return DOM.parseWildHhtml(rawString);
		}
		return XOMDocument;
	}
}
