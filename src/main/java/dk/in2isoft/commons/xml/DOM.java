package dk.in2isoft.commons.xml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ccil.cowan.tagsoup.Parser;
import org.eclipse.jdt.annotation.Nullable;
import org.jdom2.input.DOMBuilder;
import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import dk.in2isoft.commons.lang.Strings;
import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.ParentNode;
import nu.xom.ParsingException;
import nu.xom.Text;
import nu.xom.ValidityException;
import nu.xom.XMLException;
import nu.xom.converters.DOMConverter;

public class DOM {
	
	private static final Logger log = LogManager.getLogger(DOM.class);

	public static List<Element> getAncestors(Node node) {
		List<Element> ancestors = new ArrayList<Element>();
		while (node.getParent() != null) {
			ParentNode parent = node.getParent();
			if (parent instanceof Element) {
				ancestors.add((Element) parent);
			}
			node = node.getParent();
		}
		return ancestors;
	}

	public static void travel(Node node, Consumer<Node> consumer) {
		int count = node.getChildCount();
		for (int i = 0; i < count; i++) {
			Node child = node.getChild(i);
			consumer.accept(child);
			travel(child, consumer);
		}
	}
	
	public static void getTextStart(Node node, StringBuilder sb, int length) {
		if (node != null) {
			if (node instanceof Text) {
				String value = node.getValue();
				if (value != null) {
					sb.append(value);
				}
			} else {
				int count = node.getChildCount();
				for (int i = 0; i < count; i++) {
					if (Strings.countVisible(sb.toString()) < length) {
						getTextStart(node.getChild(i), sb, length);
					}
				}
			}
		}
	}
	public static String getTextStart(Node node, int length) {
		StringBuilder sb = new StringBuilder();
		getTextStart(node, sb, length);
		return sb.toString();
	}
	
	public static String getText(Node node) {
		String text = "";
		if (node != null) {
			if (node instanceof Text) {
				String value = node.getValue();
				text += value==null ? "" : value;
			} else {
				int count = node.getChildCount();
				for (int i = 0; i < count; i++) {
					text+=getText(node.getChild(i));
				}
			}
		}
		return text;
	}

	public static void travelElements(Node node, Consumer<Element> consumer) {
		int count = node.getChildCount();
		for (int i = 0; i < count; i++) {
			Node child = node.getChild(i);
			if (child instanceof Element) {
				consumer.accept((Element) child);
			}
			travelElements(child, consumer);
		}
	}

	public static void removeDeep(Collection<? extends Node> nodesToRemove) {
		for (Node toRemove : nodesToRemove) {
			ParentNode parent = toRemove.getParent();
			if (parent == null || parent instanceof nu.xom.Document) {
				continue; // Skip root
			}
			parent.removeChild(toRemove);
		}
	}

	public static void remove(Collection<? extends Node> nodesToRemove) {
		for (Node toRemove : nodesToRemove) {
			ParentNode parent = toRemove.getParent();
			if (parent == null || parent instanceof nu.xom.Document) {
				continue; // Skip root
			}
			if (toRemove instanceof Element) {
				Element elementToRemove = (Element) toRemove;
				int index = parent.indexOf(elementToRemove);
				while (elementToRemove.getChildCount() > 0) {
					Node child = elementToRemove.removeChild(0);
					parent.insertChild(child, index);
					index++;
				}
			}
			parent.removeChild(toRemove);
		}
	}

	public static int countElements(Node node) {
		int count = 0;
		int cc = node.getChildCount();
		for (int i = 0; i < cc; i++) {
			Node child = node.getChild(i);
			if (child instanceof Element) {
				count++;
			}
			count += countElements(child);
		}
		return count;
	}

	public static List<Text> findAllText(Node node) {
		List<Text> found = new ArrayList<>();
		int count = node.getChildCount();
		for (int i = 0; i < count; i++) {
			Node child = node.getChild(i);
			if (child instanceof Text) {
				found.add((Text) child);
			} else {
				found.addAll(findAllText(child));
			}
		}
		return found;
	}

	public static List<Element> findElements(Node node, Predicate<Element> predicate) {
		List<Element> found = new ArrayList<>();
		int count = node.getChildCount();
		for (int i = 0; i < count; i++) {
			Node child = node.getChild(i);
			if (child instanceof Element) {
				if (predicate.test((Element) child)) {
					found.add((Element) child);
				}
			}
			found.addAll(findElements(child, predicate));
		}
		return found;
	}

	public static void findMaxElements(Node node, int max, List<Element> found, Predicate<Element> predicate) {
		int count = node.getChildCount();
		for (int i = 0; i < count; i++) {
			if (found.size() >= max) {
				return;
			}
			Node child = node.getChild(i);
			if (child instanceof Element) {
				if (predicate.test((Element) child)) {
					found.add((Element) child);
				}
			}
			findMaxElements(child, max - found.size(), found, predicate);
		}
	}

	public static List<Element> findMaxElements(Node node, int max, Predicate<Element> predicate) {
		List<Element> found = new ArrayList<>();
		findMaxElements(node, max, found, predicate);
		return found;
	}

	public static Element findElement(Node node, Predicate<Element> predicate) {
		int count = node.getChildCount();
		for (int i = 0; i < count; i++) {
			Node child = node.getChild(i);
			if (child instanceof Element) {
				if (predicate.test((Element) child)) {
					return (Element) child;
				}
			}
			Element found = findElement(child, predicate);
			if (found != null) return found;
		}
		return null;
	}

	public static nu.xom.Document toXOM(Document domDocument) {
		try {
			return DOMConverter.convert(domDocument);
		} catch (XMLException e) {
			log.warn("Unable to convert DOM to XOM", e);
			return null;
		} 
	}
	
	public static org.jdom2.Document toJDOM(Document domDocument) {
		DOMBuilder jdomBuilder = new DOMBuilder();
        return jdomBuilder.build(domDocument);
	}

	public static nu.xom.Document parseXOM(File string) {
		try (FileReader reader = new FileReader(string)) {
			Builder bob = new Builder();
			return bob.build(reader);
		} catch (ValidityException e) {
			log.warn("Unable to parse", e);
		} catch (ParsingException e) {
			log.warn("Unable to parse", e);
		} catch (IOException e) {
			log.warn("Unable to parse", e);
		}
		return null;
	}

	public static nu.xom.Document parseXOM(String string) {
		if (Strings.isNotBlank(string)) {
			try (StringReader reader = new StringReader(string)) {
				Builder bob = new Builder();
				return bob.build(reader);
			} catch (ValidityException e) {
				log.warn("Unable to parse", e);
			} catch (ParsingException e) {
				log.warn("Unable to parse", e);
			} catch (IOException e) {
				log.warn("Unable to parse", e);
			}
		}
		return null;
	}

	public static nu.xom.Document parseWildHhtml(String wild) {
		if (Strings.isBlank(wild)) return null;
		org.jsoup.nodes.Document document = Jsoup.parse(wild);
		return new dk.in2isoft.commons.xml.JsoupUtils().toXOM(document);
	}
	
	public static nu.xom.Document parseWildHhtml(File wild) {
		try {
			org.jsoup.nodes.Document document = Jsoup.parse(wild, Strings.UTF8);
			return new dk.in2isoft.commons.xml.JsoupUtils().toXOM(document);
		} catch (IOException e) {
			log.error("Problem parsing wilf html", e);
		}
		return null;
	}

	public static nu.xom.Document parseAnyXOM(String wild) {
		try (StringReader reader = new StringReader(wild)){
			Parser tagsoup = new Parser();
			tagsoup.setFeature(Parser.defaultAttributesFeature, false);
			Builder bob = new Builder(tagsoup);
			return bob.build(reader);
		} catch (ParsingException e) {
			
		} catch (IOException e) {

		} catch (SAXNotRecognizedException e) {
		} catch (SAXNotSupportedException e) {
		}
		return null;
	}

	public static @Nullable Document parseDOM(String string) {
		if (Strings.isNotBlank(string)) {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			try {
				InputStream input = new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
				DocumentBuilder db = dbf.newDocumentBuilder();
				return db.parse(input);
			} catch (ParserConfigurationException e) {
				log.warn(e.getMessage(), e);
			} catch (IOException e) {
				log.warn(e.getMessage(), e);
			} catch (SAXException e) {
				log.warn(e.getMessage(), e);
			}
		}
		return null;
	}

	public static Element findCommonAncestor(Node first, Node second) {
		List<Element> firstAncestors = DOM.getAncestors(first);
		List<Element> secondAncestors = DOM.getAncestors(second);
		for (Element node : secondAncestors) {
			if (firstAncestors.contains(node)) {
				return node;
			}
		}
		return null;
	}

	public static void appendToAttribute(Element element, String name,
			String value) {
		Attribute attribute = element.getAttribute(name);
		if (attribute == null) {
			element.addAttribute(new Attribute(name, value));
		} else {
			attribute.setValue(attribute.getValue() + " " + value);
		}
	}

	public static String getBodyXML(nu.xom.Document document) {
		StringBuilder sb = new StringBuilder();
		List<Element> bodies = findElements(document, element -> element.getLocalName().toLowerCase().equals("body"));
		for (Element body : bodies) {
			int childCount = body.getChildCount();
			for (int i = 0; i < childCount; i++) {
				Node child = body.getChild(i);
				sb.append(child.toXML());
			}
		}
		return sb.toString();
	}

	public static boolean isAny(Element element, String... names) {
		for (String name : names) {
			if (element.getLocalName().toLowerCase().equals(name)) {
				return true;
			}
		}
		return false;
	}

	public static Element getPrevious(Element element) {
		ParentNode parent = element.getParent();
		int count = parent.getChildCount();
		Element found = null;
		for (int i = 0; i < count; i++) {
			Node child = parent.getChild(i);
			if (child == element) {
				return found;
			}
			if (child instanceof Element) found = (Element) child;
		}
		return null;
	}

	public static List<Element> getAllPrevious(Element element) {
		List<Element> found = new ArrayList<>(); 
		ParentNode parent = element.getParent();
		int count = parent.getChildCount();
		for (int i = 0; i < count; i++) {
			Node child = parent.getChild(i);
			if (child == element) {
				break;
			}
			if (child instanceof Element) {
				found.add((Element) child);
			}
		}
		return found;
	}

	public static boolean hasAttribute(Element element, String name, String value) {
		return value.equals(element.getAttributeValue(name));
	}

	public static List<Element> findBefore(Element main) {
		List<Element> found = getAllPrevious(main);
		List<Element> trail = getAncestors(main);
		for (Element ancestor : trail) {
			found.addAll(getAllPrevious(ancestor));
		}
		return found;
	}

	public static int getPosition(Element element) {
		List<Element> all = DOM.findElements(element.getDocument(), node -> true);
		int i = all.indexOf(element);
		return i;
	}
}
