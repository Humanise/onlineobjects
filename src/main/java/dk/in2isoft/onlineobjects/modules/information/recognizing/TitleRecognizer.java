package dk.in2isoft.onlineobjects.modules.information.recognizing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.commons.xml.DOM;
import info.debatty.java.stringsimilarity.NormalizedLevenshtein;
import nu.xom.Document;
import nu.xom.Element;

public class TitleRecognizer implements Recognizer {

	private static final Logger log = LogManager.getLogger(TitleRecognizer.class);

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
		StopWatch watch = new StopWatch();
		watch.start();
		Set<String> titles = findTitles(document);
		watch.stop();
		log.trace("findTitle: " + watch.getTime());
		watch.reset();
		watch.start();

		List<Element> candidates = findCandidates(document);
		log.trace("candidate count: {}", candidates.size());
		watch.stop();
		log.trace("findCandidates: " + watch.getTime());
		watch.reset();
		watch.start();

		Element main = findMain(candidates, titles, watch);
		watch.stop();
		log.trace("findMain: " + watch.getTime());

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
			if (DOM.isAny(element, "title","body","head","html","meta","img","hr","br","table","link","code","dt","dd","dfn","pre","td","tr","th","tbody","cite","var","blockquote")) {
				return false;
			}
			if (DOM.isAny(element, "h1")) {
				return true;
			}
			if (element.getChildElements().size() == 0) {
				// TODO: Just check for first non-blank chars
				String text = DOM.getText(element).trim();
				if (Strings.isNotBlank(text) && text.length() > 3 && text.length() < 500) {
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
		return titles;
	}

	private String normalize(String str) {
		return str.toLowerCase().replaceAll("\\W", "");
	}

	private Element findMain(List<Element> elements, Set<String> titles, StopWatch watch) {
		if (elements.isEmpty()) return null;

		if (!titles.isEmpty()) {
			Optional<Element> found = elements.stream().filter(el -> {
				return "h1".equals(el.getLocalName()) && titles.contains(normalize(DOM.getText(el)));
			}).findFirst();
			if (found.isPresent()) {
				return found.get();
			}
		}
		watch.stop();
		log.trace("Check all: {}", watch.getTime());
		watch.reset(); watch.start();

		List<Candidate> candidates = make(elements);
		watch.stop();
		log.trace("Build candidates: {}", watch.getTime());
		watch.reset(); watch.start();

		int titleLength = 50;
		if (!titles.isEmpty()) {
			titleLength = 0;
			titleLength = (int) titles.stream().mapToInt(s -> s.length()).average().getAsDouble();
		}
		int x = titleLength;

		if (candidates.size() > 1000) {
			candidates.sort((a,b) -> {
				if (!a.rank.equals(b.rank)) {
					return b.rank.compareTo(a.rank);
				}
				int al = Math.abs(x - a.text.length());
				int bl = Math.abs(x - b.text.length());
				if (al == bl) return 0;
				return al < bl ? -1 : 1;
			});
			candidates = candidates.subList(0, 1000);
		}

		compare(candidates, titles);

		candidates.stream().map(c -> c.element.getLocalName()).distinct().forEach(str -> log.trace(str));

		candidates.sort((a,b) -> {
			int textComparison = b.comparison.compareTo(a.comparison);
			if (textComparison != 0) {
				return textComparison;
			}
			return b.rank.compareTo(a.rank);
		});
		// TODO: Consider more than just the top compared if many have almost the same similarity (the lowest position among the top similarities)
		// If top result has low rank and a single H1 exists then use the H1
		Candidate topSimilar = candidates.get(0);
		if (topSimilar.comparison < 0.8 && topSimilar.rank < 1.0) {
			List<Candidate> topRankedBySimilarity = candidates.stream().
			sorted((a,b) -> {
				int compareTo = b.rank.compareTo(a.rank);
				if (compareTo != 0) return compareTo;
				return b.comparison.compareTo(a.comparison);
			}).collect(Collectors.toList());

			if (!topRankedBySimilarity.isEmpty()) {
				Candidate topRank = topRankedBySimilarity.get(0);
				if (topRank.position < topSimilar.position) {
					return topRank.element;
				}
			}
		}
		return topSimilar.element;
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
		List<Element> all = DOM.findElements(candidates.get(0).getDocument(), node -> true);
		List<Candidate> out = new ArrayList<>();
		for (Element element : candidates) {
			Candidate candidate = new Candidate();
			candidate.element = element;
			candidate.text = normalize(DOM.getText(element));
			candidate.rank = rank(element);
			candidate.position = all.indexOf(element);
			out.add(candidate);
		}
		return out;
	}

	private double rank(Element element) {
		double rank = element.getLocalName().toLowerCase().equals("h1") ? 1.0 : 0.0;
		boolean isInsideMain = DOM.getAncestors(element).stream().anyMatch(node -> DOM.isAny(node, "article","main"));
		if (isInsideMain) rank += 0.1;
		return rank;
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
		int position = 0;

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return element.getLocalName() + ": " + text + " = " + comparison;
		}
	}
}