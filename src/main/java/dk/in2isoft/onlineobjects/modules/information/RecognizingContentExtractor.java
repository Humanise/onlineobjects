package dk.in2isoft.onlineobjects.modules.information;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.commons.xml.DOM;
import dk.in2isoft.commons.xml.DocumentCleaner;
import dk.in2isoft.onlineobjects.modules.information.recognizing.ActionsRecognizer;
import dk.in2isoft.onlineobjects.modules.information.recognizing.AsideRecognizer;
import dk.in2isoft.onlineobjects.modules.information.recognizing.BodyTextRecognizer;
import dk.in2isoft.onlineobjects.modules.information.recognizing.CommentRecognizer;
import dk.in2isoft.onlineobjects.modules.information.recognizing.FooterRecognizer;
import dk.in2isoft.onlineobjects.modules.information.recognizing.ListRecognizer;
import dk.in2isoft.onlineobjects.modules.information.recognizing.NavigationRecognizer;
import dk.in2isoft.onlineobjects.modules.information.recognizing.Recognizer;
import dk.in2isoft.onlineobjects.modules.information.recognizing.TitleRecognizer;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;

public class RecognizingContentExtractor implements ContentExtractor {

	//private static final Logger log = LogManager.getLogger(RecognizingContentExtractor.class);

	private List<Recognizer> recognizers;
	private boolean debug = false;

	public RecognizingContentExtractor() {
		recognizers = Lists.newArrayList();
		recognizers.add(new TitleRecognizer());
		recognizers.add(new BodyTextRecognizer());
		recognizers.add(new ActionsRecognizer());
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
		simplify(document);
		recognize(document);
		if (!debug) {
			clean(document);
		}
		return document;
	}

	private void clean(Document document) {
		Set<String> ignore = Sets.newHashSet("html", "body", "head", "title");
		List<Element> toRemove = DOM.findElements(document, element -> {
			if (ignore.contains(element.getLocalName())) {
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
		for (Recognizer recognizer : recognizers) {
			Map<Element, Double> map = recognizer.recognize(document);
			if (map!=null) {
				for (Entry<Element, Double> entry : map.entrySet()) {
					Double value = entry.getValue();
					if (value!=null && value != 0) {
						entry.getKey().addAttribute(new Attribute("data-" + recognizer.getName(), value.toString()));
					}
				}
			}
		}
	}

	private Document simplify(Document document) {
		DocumentCleaner cleaner = new DocumentCleaner();
		cleaner.setAllowClasses(true);
		cleaner.setAllowMetaTags(true);
		cleaner.setAllowSemanticAttributes(true);
		cleaner.setAllowStructureTags(true);
		cleaner.clean(document);
		return document;
	}

}
