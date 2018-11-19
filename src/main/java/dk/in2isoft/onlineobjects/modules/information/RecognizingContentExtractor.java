package dk.in2isoft.onlineobjects.modules.information;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.commons.xml.DOM;
import dk.in2isoft.commons.xml.DocumentCleaner;
import dk.in2isoft.onlineobjects.modules.information.recognizing.ListRecognizer;
import dk.in2isoft.onlineobjects.modules.information.recognizing.Recognizer;
import info.debatty.java.stringsimilarity.NormalizedLevenshtein;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;

public class RecognizingContentExtractor implements ContentExtractor {

	private static final Logger log = LogManager.getLogger(RecognizingContentExtractor.class);

	private List<Recognizer> recognizers;
	private boolean debug = false;

	public RecognizingContentExtractor() {
		recognizers = Lists.newArrayList(new TitleRecognizer(), new BodyTextRecognizer(), new ActionsRecognizer());
		recognizers.add(new CommentRecognizer());
		recognizers.add(new FooterRecognizer());
		recognizers.add(new AsideRecognizer());
		recognizers.add(new NavigationRecognizer());
		recognizers.add(new ListRecognizer());
	}

	public static RecognizingContentExtractor debug() {
		RecognizingContentExtractor extractor = new RecognizingContentExtractor();
		extractor.debug = true;
		return extractor;
	}

	public Document extract(Document document) {
		document = (Document) document.copy();
		// log.info(document.toXML());
		simplify(document);
		recognize(document);
		if (!debug) {
			clean(document);
		}
		return document;
	}

	private void clean(Document document) {
		Set<String> x = Sets.newHashSet("html","body","head","title","br");
		List<Element> toRemove = DOM.findElements(document, element -> {
			if (x.contains(element.getLocalName())) {
				return false;
			}
			double total = 0;
			int num = 0;
			for (Recognizer recognizer : recognizers) {
				String value = element.getAttributeValue("data-" + recognizer.getName());
				if (Strings.isNotBlank(value)) {
					total += Double.parseDouble(value);
					num++;
				}
			}
			if (total > 0) {
				return false;
			}
			return num > 0;
		});
		DOM.removeDeep(toRemove);
	}

	private boolean hasDataAttribute(Element element) {
		for (int i = 0; i < element.getAttributeCount(); i++) {
			if (element.getAttribute(i).getLocalName().startsWith("data-")) {
				return true;
			}
		}
		return false;
	}

	private void recognize(Document document) {
		DOM.travel(document.getRootElement(), node -> {
			if (node instanceof Element) {
				Element p = (Element) node;
				for (Recognizer recognizer : recognizers) {
					double value = recognizer.recognize(p);
					if (value != 0) {
						p.addAttribute(new Attribute("data-" + recognizer.getName(), String.valueOf(value)));
					}
				}
			}
		});
	}

	private Document simplify(Document document) {
		DocumentCleaner cleaner = new DocumentCleaner();
		cleaner.setAllowClasses(true);
		cleaner.clean(document);
		return document;
	}

	private class TitleRecognizer implements Recognizer {

		@Override
		public String getName() {
			return "title";
		}

		@Override
		public double recognize(Element element) {
			double points = 0;
			if (element.getLocalName().equals("h1"))
				points++;
			return points;
		}

	}

	private class CommentRecognizer implements Recognizer {

		@Override
		public String getName() {
			return "comment";
		}

		@Override
		public double recognize(Element element) {
			if (DOM.isAny(element, "html","head","body")) {
				return 0;
			}
			String cls = element.getAttributeValue("class");
			if (cls!=null && cls.toLowerCase().indexOf("comment") != -1) {
				return -1;
			}
			return 0;
		}

	}

	private class AsideRecognizer implements Recognizer {

		@Override
		public String getName() {
			return "aside";
		}

		@Override
		public double recognize(Element element) {
			if (element.getLocalName().equals("aside")) {
				return -1;
			}
			String cls = element.getAttributeValue("class");
			if (cls!=null && cls.toLowerCase().matches("sidebar|aside|side\\-bar")) {
				return -1;
			}
			return 0;
		}

	}

	private class NavigationRecognizer implements Recognizer {

		@Override
		public String getName() {
			return "navigation";
		}

		@Override
		public double recognize(Element element) {
			if (element.getLocalName().equals("nav")) {
				return -1;
			}
			String cls = element.getAttributeValue("class");
			if (cls!=null && cls.toLowerCase().matches("nav|menu")) {
				return -1;
			}
			return 0;
		}

	}

	private class FooterRecognizer implements Recognizer {

		@Override
		public String getName() {
			return "footer";
		}

		@Override
		public double recognize(Element element) {
			if (element.getLocalName().equals("footer")) {
				return -1;
			}
			String cls = element.getAttributeValue("class");
			if (cls!=null && cls.toLowerCase().indexOf("footer") != -1) {
				return -1;
			}
			return 0;
		}

	}



	private class ActionsRecognizer implements Recognizer {

		@Override
		public String getName() {
			return "action";
		}

		@Override
		public double recognize(Element element) {
			if (element.getChildElements().size() > 0) return 0;
			String[] x = {"log ind", "abonnement", "gå til hovedindhold", "søg", "mail-adresse",
					"adgangskode", "opret konto", "bestil ny adgangskode", "søgefelt", "abonnement",
					"facebook", "twitter", "del", "rss", "del artklen",
					"cookies", "privatlivspolitik",
					"nyhedsbrev",
					"annonce","artiklen fortsætter efter annoncen", "læs også:"
				};
			String text = DOM.getText(element).toLowerCase().trim();
			for (String string : x) {
				
				if (text.equals(string)) {
					return -1;
				}
			}
			// Wikipedia...
			if (DOM.isAny(element, "a")) {
				if (text.equals("edit") || text.equals("^")) {
					return -1;
				}
			}
			if (DOM.isAny(element, "a") && text.equals("edit")) {
				return -1;
			}
			
			String cls = element.getAttributeValue("class");
			if (cls!=null && cls.matches("newsletter")) {
				return -1;
			}
			return 0;
		}

	}

	private class BodyTextRecognizer implements Recognizer {

		private HashSet<String> inlines;

		public BodyTextRecognizer() {
			inlines = Sets.newHashSet("strong", "em", "a", "img", "br", "hr", "sub", "sup", "abbr", "dfn", "del", "ins",
					"cite", "q", "code", "tt", "kbd", "samp", "var", "time");
		}

		@Override
		public String getName() {
			return "text";
		}

		@Override
		public double recognize(Element element) {
			if (element.getLocalName().equals("p")) {
				return scoreByTextLength(element);
			}
			if (element.getLocalName().equals("div")) {
				boolean hasOnlyInline = DOM.findElement(element, other -> {
					return !inlines.contains(other.getLocalName());
				}) == null;
				if (hasOnlyInline) {
					return scoreByTextLength(element);
				}
			}
			return 0;
		}

		private double scoreByTextLength(Element element) {
			double x = ((double) DOM.getText(element).length()) / 100.0;
			return Math.min(1, x);
		}

	}
}
