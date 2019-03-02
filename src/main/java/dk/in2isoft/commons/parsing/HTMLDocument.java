
package dk.in2isoft.commons.parsing;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.commons.xml.DOM;
import dk.in2isoft.commons.xml.DocumentToText;
import dk.in2isoft.onlineobjects.modules.information.RecognizingContentExtractor;
import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.XPathContext;

public class HTMLDocument extends XMLDocument {
	
	//private static Logger log = LogManager.getLogger(HTMLDocument.class);

	private String title;
    private String originalUrl;
	
	public HTMLDocument(String raw) {
		super(raw);
	}
	
    public HTMLDocument(nu.xom.Document doc) {
    	super(doc);
    }

	
	public String getTitle() {
		if (this.title==null) {
			nu.xom.Document doc = getXOMDocument();
			if (doc!=null) {
				List<Element> titles = DOM.findElements(doc, node -> DOM.isAny(node, "title") );
				for (Element element : titles) {
					String text = DOM.getText(element);
					if (Strings.isNotBlank(text)) {
						this.title = text;
						break;
					}
				}
			}
		}
		return this.title;
	}
    	        
    public String getText() {
        nu.xom.Document doc = getXOMDocument();
        return new DocumentToText().getText(doc);
    }
        
    public String getExtractedText() {
		String rawString = getRawString();
		if (Strings.isNotBlank(rawString)) {
			nu.xom.Document document = new RecognizingContentExtractor().extract(getXOMDocument());
			if (document != null) {
				DocumentToText doc2text = new DocumentToText();
				return doc2text.getText(document);
			}
		}
    	return null;
    }
    
    public List<HTMLReference> getFeeds() {
		List<HTMLReference> refs = Lists.newArrayList();
        nu.xom.Document doc = getXOMDocument();
        XPathContext context = new XPathContext("html", "http://www.w3.org/1999/xhtml");
        Nodes titles = doc.query("//html:link[@type='application/rss+xml']", context);
        for (int i = 0; i < titles.size(); i++) {
			nu.xom.Node node = titles.get(i);
			if (node instanceof Element) {
				Element element = (Element) node;
				HTMLReference reference = new HTMLReference();
				Attribute href = element.getAttribute("href");
				if (href!=null) {
					reference.setUrl(href.getValue());
				}
				refs.add(reference);
			}
		}
    	return refs;
    }
	
	public List<HTMLReference> getReferences() {
		Document doc = getDOMDocument();
		List<HTMLReference> refs = new ArrayList<HTMLReference>();
		if (doc==null) {
			return refs;
		}
		NodeList list = doc.getElementsByTagName("a");
	    for (int i=0;i<list.getLength();i++) {
	    	Node link = list.item(i);
	    	NamedNodeMap atts = link.getAttributes();
	    	Node href = atts.getNamedItem("href");
	    	Node title = atts.getNamedItem("title");
	    	HTMLReference ref = new HTMLReference();
	    	if (href!=null) {
	    		ref.setUrl(href.getNodeValue());
	    	}
	    	if (title!=null) {
	    		ref.setTitle(title.getNodeValue());
	    	}
	    	String text = "";
	    	NodeList children = link.getChildNodes();
	    	for (int j=0;j<children.getLength();j++) {
	    		Node child = children.item(j);
	    		if (child!=null && child.getNodeType()==Node.TEXT_NODE) {
	    			text+=child.getNodeValue();
	    		}
	    	}
	    	ref.setText(text);
	    	refs.add(ref);
	    }
	    return refs;
	}
	
	public static HTMLDocument fromContent(String content) {
		
		return null;
	}

	public String getOriginalUrl() {
		return originalUrl;
	}

	public void setOriginalUrl(String originalUrl) {
		this.originalUrl = originalUrl;
	}
}
