package dk.in2isoft.onlineobjects.modules.information.recognizing;

import java.util.Map;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.commons.xml.DOM;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Text;

public class ListRecognizer implements Recognizer {
	
	private String[] headings = {"kilder","henvisninger","references","see also"};

	@Override
	public String getName() {
		return "list";
	}

	@Override
	public double recognize(Element element) {
		
		if (DOM.isAny(element, "ul","ol")) {
			for (int i = 0; i < element.getChildCount(); i++) {
				Node child = element.getChild(i);
				if (child instanceof Text && Strings.isNotBlank(child.getValue())) {
					return 0;
				}
				if (child instanceof Element) {
					Element childElement = (Element) child;
					if (DOM.isAny(childElement, "li")) {
						boolean hasLink = false;
						for (int j = 0; j < childElement.getChildCount(); j++) {
							Node childOfLi = childElement.getChild(j);
							if (childOfLi instanceof Text) {
								Text text = (Text) childOfLi;
								if (Strings.isNotBlank(text.getValue())) {
									return 0;
								}
							}
							else if (childOfLi instanceof Element) {
								Element el = (Element) childOfLi;
								if (DOM.isAny(el, "a")) {
									if (hasLink) return 0;
									hasLink = true;
								} else {
									return 0;
								}
							}
						}
						if (!hasLink) {
							return 0;
						}
					} else {
						return 0;
					}
				}
			}
			String heading = getHeading(element);
			
			if (isOkHeading(heading)) {
				return 0;
			}
			return -1;
		}
		return 0;
	}
	
	private boolean isOkHeading(String heading) {
		for (String string : headings) {
			if (heading.startsWith(string)) {
				return true;
			}
		}
		return false;
	}

	private String getHeading(Element element) {
		Element prev = DOM.getPrevious(element);
		if (prev !=null && DOM.isAny(prev, "h2","h3","h4","h5","h6")) {
			return DOM.getText(prev).trim().toLowerCase();
		}
		return "";
	}

	@Override
	public Map<Element,Double> recognize(Document document) {
		return null;
	}

}