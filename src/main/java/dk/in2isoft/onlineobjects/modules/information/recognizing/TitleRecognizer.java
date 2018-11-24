package dk.in2isoft.onlineobjects.modules.information.recognizing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.commons.xml.DOM;
import info.debatty.java.stringsimilarity.NormalizedLevenshtein;
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
		String twitterTitle = getTwitterTitle(document);
		Set<String> titles = new HashSet<>();
		if (Strings.isNotBlank(title)) titles.add(normalize(title));
		if (Strings.isNotBlank(openGraphTitle)) titles.add(normalize(openGraphTitle));
		if (Strings.isNotBlank(twitterTitle)) titles.add(normalize(twitterTitle));
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
					if (titles.contains(normalize(text))) {
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
	
	private String normalize(String str) {
		return str.toLowerCase().replaceAll("\\W", "");
	}
	
	private Element findMain(List<Element> candidates, Set<String> titles) {
		if (candidates.isEmpty()) return null;
		if (!titles.isEmpty()) {
			List<Element> list = candidates.stream().filter(el -> titles.contains(normalize(DOM.getText(el)))).collect(Collectors.toList());
			if (!list.isEmpty()) {
				return list.get(0);
			}
		}
		
		List<Candidate> cs = make(candidates);
		compare(cs, titles);
		
		cs.sort((a,b) -> {
			return b.comparison.compareTo(a.comparison);
		});
		return cs.get(0).element;
	}

	private void compare(List<Candidate> cs, Set<String> titles) {
		NormalizedLevenshtein l = new NormalizedLevenshtein();
		for (Candidate candidate : cs) {
			double comparison = 0;
			for (String title : titles) {
				comparison = Math.max(comparison, 1 - l.distance(title, candidate.text));
			}
			candidate.comparison = comparison;
		}
		
	}

	private List<Candidate> make(List<Element> candidates) {
		List<Candidate> out = new ArrayList<>();
		for (Element element : candidates) {
			Candidate candidate = new Candidate();
			candidate.element = element;
			candidate.text = normalize(DOM.getText(element));
			out.add(candidate);
		}
		return out;
	}

	private String getMainTitle(Document doc) {
		Element title = DOM.findElement(doc, element -> DOM.isAny(element, "title"));
		return DOM.getText(title);
	}

	private String getOpenGraphTitle(Document doc) {
		Element title = DOM.findElement(doc, element -> DOM.isAny(element, "meta") && DOM.hasAttribute(element,"property","og:title"));
		return title!=null ? title.getAttributeValue("content") : null;
	}

	private String getTwitterTitle(Document doc) {
		Element title = DOM.findElement(doc, element -> DOM.isAny(element, "meta") && DOM.hasAttribute(element,"name","twitter:title"));
		return title!=null ? title.getAttributeValue("content") : null;
	}

	private class Candidate {
		Element element;
		String text;
		Double comparison = 0.0;
	}
}