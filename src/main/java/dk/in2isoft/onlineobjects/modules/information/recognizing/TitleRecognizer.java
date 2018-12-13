package dk.in2isoft.onlineobjects.modules.information.recognizing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
		return 0;
	}

	@Override
	public Map<Element,Double> recognize(Document document) {
		Set<String> titles = findTitles(document);
		
		List<Element> candidates = findCandidates(document);

		Element main = findMain(candidates, titles);

		Map<Element,Double> nodes = new HashMap<>();
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

	private List<Element> findCandidates(Document document) {
		return DOM.findElements(document, element -> {
			if (DOM.isAny(element, "title","body","head","html")) {
				return false;
			}
			if (DOM.isAny(element, "h1")) {
				return true;
			}
			if (element.getChildElements().size() == 0) {
				// TODO: Just check for first non-blank chars
				String text = DOM.getText(element);
				if (Strings.isNotBlank(text)) {
					// TODO: Try to limit possible candidates
					// Maybe exclude items with only block children
					return true;
				}
			}
			return false;
		});
	}

	private Set<String> findTitles(Document document) {
		String mainTitle = getMainTitle(document);
		String openGraphSiteTitle = getOpenGraphSiteName(document);

		String siteTitle = openGraphSiteTitle;
		if (mainTitle.endsWith("Wikipedia")) {
			siteTitle = "Wikipedia";
		}
		
		mainTitle = extractActualTitle(mainTitle, siteTitle);
		
		String openGraphTitle = getOpenGraphTitle(document);
		String twitterTitle = getTwitterTitle(document);
		String facebookTitle = getFacebookTitle(document);
		
		Set<String> titles = new HashSet<>();
		if (Strings.isNotBlank(mainTitle)) titles.add(normalize(mainTitle));
		if (Strings.isNotBlank(openGraphTitle)) titles.add(normalize(openGraphTitle));
		if (Strings.isNotBlank(twitterTitle)) titles.add(normalize(twitterTitle));
		if (Strings.isNotBlank(facebookTitle)) titles.add(normalize(facebookTitle));
		System.out.println(titles);
		return titles;
	}
	
	private String normalize(String str) {
		return str.toLowerCase().replaceAll("\\W", "");
	}
	
	private Element findMain(List<Element> candidates, Set<String> titles) {
		if (candidates.isEmpty()) return null;
		
		if (!titles.isEmpty()) {
			Optional<Element> found = candidates.stream().filter(el -> {
				return "h1".equals(el.getLocalName()) && titles.contains(normalize(DOM.getText(el)));
			}).findFirst();
			if (found.isPresent()) {
				return found.get();
			}
		}
		
		List<Candidate> cs = make(candidates);
		compare(cs, titles);
		
		cs.sort((a,b) -> {
			int textComparison = b.comparison.compareTo(a.comparison);
			if (textComparison != 0) {
				return textComparison;
			}
			return b.rank.compareTo(a.rank);
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
			candidate.rank = element.getLocalName().toLowerCase().equals("h1") ? 1.0 : 0.0;
			out.add(candidate);
		}
		return out;
	}
	
	private String extractActualTitle(String title, String siteName) {
		if (title == null) return null;
		if (siteName != null) {
			if (title.endsWith(siteName)) {
				String withoutSite = title.substring(0, title.length() - siteName.length());
				return withoutSite.replaceAll("[ -:|]+$", "");
			}
		}
		if (title.contains("|")) {
			title = title.split("\\|")[0].trim();
		}
		return title;
	}

	private String getMainTitle(Document doc) {
		Element title = DOM.findElement(doc, element -> DOM.isAny(element, "title"));
		return DOM.getText(title);
	}

	private String getOpenGraphTitle(Document doc) {
		return findMetaTag(doc, "property", "og:title");
	}

	private String getOpenGraphSiteName(Document doc) {
		return findMetaTag(doc, "property", "og:site_name");
	}

	private String getTwitterTitle(Document doc) {
		return findMetaTag(doc, "name", "twitter:title");
	}

	private String getFacebookTitle(Document doc) {
		return findMetaTag(doc, "name", "fb_title");
	}

	private String findMetaTag(Document doc, String attribute, String value) {
		Element found = DOM.findElement(doc, element -> {
			return DOM.isAny(element, "meta") && DOM.hasAttribute(element, attribute, value);
		});
		return found!=null ? found.getAttributeValue("content") : null;
	}

	private class Candidate {
		Element element;
		String text;
		Double comparison = 0.0;
		Double rank = 0.0;
		
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return element.getLocalName() + ": " + text + " = " + comparison;
		}
	}
}