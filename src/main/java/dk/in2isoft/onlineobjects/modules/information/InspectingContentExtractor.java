package dk.in2isoft.onlineobjects.modules.information;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;

public class InspectingContentExtractor implements ContentExtractor {
	
	//private static final Logger log = LogManager.getLogger(InspectingContentExtractor.class);

	public Document extract(Document document) {
		document = (Document) document.copy();
		Nodes nodes = document.query("//*");
		for (int i = 0; i < nodes.size(); i++) {
			Node node = nodes.get(i);
			if (node instanceof Element) {
				Element element = (Element) node;
				if (element.getLocalName().equals("ul")) {
					element.addAttribute(new Attribute("data-menu", "1"));
				}
			}
		}
		return document;
	}
}
