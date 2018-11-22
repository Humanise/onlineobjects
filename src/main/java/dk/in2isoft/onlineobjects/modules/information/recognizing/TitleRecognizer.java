package dk.in2isoft.onlineobjects.modules.information.recognizing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.commons.xml.DOM;
import nu.xom.Document;
import nu.xom.Element;

public class TitleRecognizer implements Recognizer {

	@Override
	public String getName() {
		return "title";
	}

	@Override
	public double recognize(Element element) {
		double points = 0;
		//if (element.getLocalName().equals("h1"))
		//	points++;
		return points;
	}

	@Override
	public Map<Element,Double> recognize(Document document) {
		String title = getMainTitle(document);
		String openGraphTitle = getOpenGraphTitle(document);
		Set<String> titles = new HashSet<>();
		if (Strings.isNotBlank(title)) titles.add(title.toLowerCase());
		if (Strings.isNotBlank(openGraphTitle)) titles.add(openGraphTitle.toLowerCase().trim());
		Map<Element,Double> nodes = new HashMap<>();
		
		List<Element> candidates = DOM.findElements(document, element -> {
			if (DOM.isAny(element, "title")) {
				return false;
			}
			if (DOM.isAny(element, "h1")) {
				return true;
			}
			if (element.getChildElements().size() == 0) {
				String text = DOM.getText(element);
				if (Strings.isNotBlank(text)) {
					if (titles.contains(text.toLowerCase().trim())) {
						return true;
					}
				}
			}
			return false;
		});
		Element main = findMain(candidates, titles);
		if (main!=null) {
			List<Element> before = DOM.findBefore(main);
			for (Element other : before) {
				if (!DOM.isAny(other, "html", "head", "body", "meta")) {
					nodes.put(other, -1.0);
				}
			}
			nodes.put(main, 1.0);
		}
		return nodes;
	}
	
	private Element findMain(List<Element> candidates, Set<String> titles) {
		if (candidates.isEmpty()) return null;
		if (!titles.isEmpty()) {
			List<Element> list = candidates.stream().filter(el -> titles.contains(DOM.getText(el).toLowerCase())).collect(Collectors.toList());
			if (!list.isEmpty()) {
				return list.get(0);
			}
		}
		
		candidates.sort((a,b) -> {
			int lengthA = DOM.getText(a).length();
			int lengthB = DOM.getText(b).length();
			return lengthB - lengthA;
		});
		return candidates.get(0);
	}

	private String getMainTitle(Document doc) {
		Element title = DOM.findElement(doc, element -> DOM.isAny(element, "title"));
		return DOM.getText(title);
	}

	private String getOpenGraphTitle(Document doc) {
		Element title = DOM.findElement(doc, element -> DOM.isAny(element, "meta") && DOM.hasAttribute(element,"property","og:title"));
		return title!=null ? title.getAttributeValue("content") : null;
	}
}