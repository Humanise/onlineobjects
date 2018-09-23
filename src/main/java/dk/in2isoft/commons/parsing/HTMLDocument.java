
package dk.in2isoft.commons.parsing;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.commons.xml.DocumentToText;
import dk.in2isoft.onlineobjects.modules.information.SimpleContentExtractor;
import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.XPathContext;

public class HTMLDocument extends XMLDocument {
	
	//private static Logger log = LogManager.getLogger(HTMLDocument.class);

	private String title;
    private String contentType;
    private String originalUrl;
	
	public HTMLDocument(String raw) {
		super(raw);
	}
	
	public String getTitle() {
		if (this.title==null) {
			Document doc = getDOMDocument();
			if (doc!=null) {
				NodeList titles = doc.getElementsByTagName("title");
				if (titles.getLength()>0) {
					Node titleNode = titles.item(0);
					Node text = titleNode.getFirstChild();
					if (text!=null) {
						this.title = text.getNodeValue().trim();
					}
				}
			}
		}
		return this.title;
	}
    
    public String getMeta(String key) {
        String value = null;
        Document doc = getDOMDocument();
		if (doc!=null) {
	        NodeList metas = doc.getElementsByTagName("meta");
	        for (int i=0;i<metas.getLength();i++) {
	            Node meta = metas.item(i);
	            if (getAttributeValue(meta,"name").equalsIgnoreCase(key)) {
	                value=getAttributeValue(meta,"content");
	            }
	            else if (getAttributeValue(meta,"http-equiv").equalsIgnoreCase(key)) {
	                value=getAttributeValue(meta,"content");
	            }
	        }
		}
        return value;
    }
	
	public String getContentType() {
		if (this.contentType==null) {
			Document doc = getDOMDocument();
			if (doc!=null) {
				NodeList metas = doc.getElementsByTagName("meta");
				for (int i=0;i<metas.getLength();i++) {
					Node meta = metas.item(i);
					if (getAttributeValue(meta,"http-equiv").equalsIgnoreCase("content-type")) {
						contentType=getAttributeValue(meta,"content");
					}
				}
			}
		}
		return this.contentType;
	}
    
    public String getFullText() {
        nu.xom.Document doc = getXOMDocument();
        return doc.getValue();
    }
    
    public String getText() {
        nu.xom.Document doc = getXOMDocument();
        return new DocumentToText().getText(doc);
    }
        
    public String getExtractedText() {
		String rawString = getRawString();
		if (Strings.isNotBlank(rawString)) {
			nu.xom.Document document = new SimpleContentExtractor().extract(getXOMDocument());
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
	
	private String getAttributeValue(Node node, String name) {
		String out="";
    	NamedNodeMap atts = node.getAttributes();
    	Node att = atts.getNamedItem(name);
    	if (att!=null) {
    		out=att.getNodeValue();
    	}
		return out;
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
