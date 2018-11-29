package dk.in2isoft.commons.xml;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import dk.in2isoft.commons.lang.Strings;
import nu.xom.Attribute;
import nu.xom.Comment;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.ParentNode;
import nu.xom.Text;

public class DocumentCleaner {

	private Multimap<String, String> validAttributes = HashMultimap.create();
	private Set<String> validTags = new HashSet<>();
	private Set<String> structureTags = new HashSet<>();
	private Set<String> validLeaves = new HashSet<>();
	
	private Set<String> bannedTags = new HashSet<>();
	private Set<String> semanticAttributes = new HashSet<>();

	private URI uri;
	private boolean allowDataAttributes;
	private boolean allowClasses;
	private boolean allowSemanticAttributes;
	private boolean allowMetaTags;
	private boolean allowStructureTags;
	
	private static final Logger log = LoggerFactory.getLogger(DocumentCleaner.class);

	public DocumentCleaner() {
		validAttributes.put("a", "href");
		validAttributes.put("a", "title");
		validAttributes.put("abbr", "title");
		validAttributes.put("time", "datetime");
		validAttributes.put("img", "src");
		validAttributes.put("img", "title");
		validAttributes.put("img", "width");
		validAttributes.put("img", "height");
		
		validAttributes.put("meta", "name");
		validAttributes.put("meta", "content");
		validAttributes.put("meta", "property");

		validTags.addAll(Sets.newHashSet("html","head","body","title"));
		validTags.addAll(Sets.newHashSet("h1","h2","h3","h4","h5","h6","p"));
		validTags.addAll(Sets.newHashSet("strong","em","a","img","br","hr"));
		validTags.addAll(Sets.newHashSet("sub","sup","abbr","dfn"));
		validTags.addAll(Sets.newHashSet("del","ins","cite","q"));
		validTags.addAll(Sets.newHashSet("code","tt","kbd","samp","var","time"));
		validTags.addAll(Sets.newHashSet("table","tbody","tr","th","td","thead","tfoot","colgroup","col","caption"));
		validTags.addAll(Sets.newHashSet("dl","dt","dd"));
		validTags.addAll(Sets.newHashSet("ul","ol","menu","li")); // TODO: menu is deprecated, translate it into ul
		validTags.addAll(Sets.newHashSet("blockquote","pre"));
		validTags.addAll(Sets.newHashSet("div"));

		// TODO: This should be an option 
		structureTags.addAll(Sets.newHashSet("section","article","aside","main","footer","nav","figure","figcaption"));
		
		
		bannedTags.add("script");
		bannedTags.add("style");
		bannedTags.add("noscript");

		validLeaves.add("hr");
		validLeaves.add("br");
		validLeaves.add("img");
		validLeaves.add("col");
		validLeaves.add("body");
		validLeaves.add("html");
		validLeaves.add("head");
		validLeaves.add("meta");
		
		semanticAttributes.add("about");
		semanticAttributes.add("typeof");
		semanticAttributes.add("role");
		semanticAttributes.add("itemprop");
		semanticAttributes.add("itemtype");
	}
	
	public void setAllowDataAttributes(boolean allowDataAttributes) {
		this.allowDataAttributes = allowDataAttributes;
	}
	
	public void setAllowClasses(boolean allowClasses) {
		this.allowClasses = allowClasses;
	}
	
	public void setAllowSemanticAttributes(boolean allowSemanticAttributes) {
		this.allowSemanticAttributes = allowSemanticAttributes;
	}
	
	public void setAllowMetaTags(boolean allowMetaTags) {
		this.allowMetaTags = allowMetaTags;
	}
	
	public void setUrl(String url) {
		if (Strings.isBlank(url)) {
			uri = null;
		} else {
			try {
				this.uri = new URI(url);
			} catch (URISyntaxException e) {
				log.warn("Illegal URI",e);
			}
		}
	}
	
	public void clean(nu.xom.Document document) {
		if (document.getRootElement()==null) {
			return;
		}
		DOM.travelElements(document, element -> {
			element.setLocalName(element.getLocalName().toLowerCase());
			if (!allowStructureTags && structureTags.contains(element.getLocalName())) {
				element.setLocalName("div");
			}
		});			
		// TODO lowercase all elements
		Set<nu.xom.Node> nodesToRemove = Sets.newHashSet();
		DOM.travel(document, node -> {			
			if (node instanceof Comment) {
				nodesToRemove.add(node);
			}
			else if (node instanceof Element) {
				Element element = (Element) node;
				String nodeName = element.getLocalName().toLowerCase();
				
				element.getAttributeCount();
				for (int j = element.getAttributeCount() - 1; j >= 0; j--) {
					Attribute attribute = element.getAttribute(j);
					if (!isValid(nodeName, attribute)) {
						element.removeAttribute(attribute);
					}
				}
				
				if (!isValidTag(nodeName)) {
					nodesToRemove.add(element);
					return;
				}
				if (nodeName.equals("img")) {
					String src = element.getAttributeValue("src");
					if (Strings.isBlank(src)) {
						nodesToRemove.add(element);
					}
				}
				if (nodeName.equals("a")) {
					if (element.getAttributeCount() == 0) {
						nodesToRemove.add(element);
					} else if (uri!=null) {
						Attribute attribute = element.getAttribute("href");
						if (attribute!=null) {
							String value = attribute.getValue();
							try {
								attribute.setValue(uri.resolve(value).toString());
							} catch (IllegalArgumentException e) {
								log.warn("Error resolving link URL: "+e.getMessage());
							}
						}
					}
				}
			}
		});
		removeNodes(nodesToRemove);
		if (uri!=null) {
			DOM.travelElements(document, element -> {
				if (!element.getLocalName().equalsIgnoreCase("img")) {
					return;
				}
				Attribute attribute = element.getAttribute("src");
				if (attribute!=null) {
					String value = attribute.getValue();
					try {
						attribute.setValue(uri.resolve(value).toString());
					} catch (IllegalArgumentException e) {
						log.warn("Error resolving image URL",e);
					}
				}
				
			});
		}
		removeLeaves(document);
		nodesToRemove.clear();
		DOM.travel(document, node -> {			
			if (node instanceof Element) {
				Element element = (Element) node;
				if (isUnnecessary(element)) {
					nodesToRemove.add(element);
				}
			}
		});
		removeNodes(nodesToRemove);
	}

	private boolean isValidTag(String nodeName) {
		if (allowMetaTags && nodeName.equals("meta")) {
			return true;
		}
		if (allowStructureTags && structureTags.contains(nodeName)) {
			return true;
		}
		return validTags.contains(nodeName);
	}

	private void removeNodes(Set<nu.xom.Node> nodesToRemove) {
		for (nu.xom.Node toRemove : nodesToRemove) {
			ParentNode parent = toRemove.getParent();
			if (parent!=null) {
				if (toRemove instanceof Element) {
					Element elementToRemove = (Element) toRemove;
					String tagName = elementToRemove.getLocalName().toLowerCase();
					if (!bannedTags.contains(tagName)) {
						int index = parent.indexOf(elementToRemove);
						while (elementToRemove.getChildCount()>0) {
							nu.xom.Node child = elementToRemove.removeChild(0);
							if (parent instanceof Element || !(child instanceof Text)) {
								if (parent instanceof nu.xom.Document) {
									log.debug("The parent is a document");
								} else {
									parent.insertChild(child, index);
								}
							}
							index ++;
						}
					}
				}
				if (parent instanceof nu.xom.Document) {
					//log.warn("Cannot remove the root node");
				} else {
					parent.removeChild(toRemove);
				}
			}
		}
	}

	private boolean isUnnecessary(Element element) {
		Set<String> blocks = Sets.newHashSet("ul","p","h1","h2","h3","h4","h5","h6");
		if (element.getLocalName().toLowerCase().equals("div")) {
			int childElementCount = element.getChildElements().size();
			for (int i = 0; i < element.getChildCount(); i++) {
				Node child = element.getChild(i);
				if (child instanceof Text) {
					Text text = (Text) child;
					if (Strings.isNotBlank(text.getValue())) {
						return false;
					}
				} else if (child instanceof Element) {
					Element ce = (Element) child;
					if (childElementCount == 1 && blocks.contains(ce.getLocalName().toLowerCase())) {
						return true;
					}
					if (!ce.getLocalName().toLowerCase().equals("div")) {
						return false;
					}
				}
			}
			return true;
		}
		return false;
	}

	private boolean isValid(String nodeName, Attribute attribute) {
		String attrName = attribute.getLocalName().toLowerCase();
		if (allowClasses && attrName.equals("class")) {
			return true;
		}
		if (allowSemanticAttributes && semanticAttributes.contains(attrName)) {
			return true;
		}
		if (allowDataAttributes && attrName.startsWith("data")) {
			return true;
		}
		return validAttributes.containsEntry(nodeName, attrName);
	}
	
	private void removeLeaves(nu.xom.Document document) {
		Set<Element> toRemove = new HashSet<>();
		DOM.travelElements(document, node -> {
			int childCount = node.getChildCount();
			boolean leaf = true; 
			for (int j = 0; j < childCount; j++) {
				nu.xom.Node child = node.getChild(j);
				if (child instanceof Element) {
					leaf = false;
					break;
				}
				if (child instanceof Text) {
					String text = ((Text) child).getValue();
					if (Strings.isNotBlank(text)) {
						leaf = false;
						break;
					}
				}
			}
			if (!leaf) return;
			String name = node.getLocalName().toLowerCase();
			if (!validLeaves.contains(name)) {
				toRemove.add(node);
			}
		});
		boolean modified = false;
		for (Element element : toRemove) {
			ParentNode parent = element.getParent();
			if (parent instanceof Element) {
				parent.removeChild(element);
				modified = true;
			}
		}
		if (modified) {
			removeLeaves(document);
		}
	}
	/*
	@Deprecated
	public void clean(Document document) {

		NodeList nodes = document.getElementsByTagName("*");
		int length = nodes.getLength();
		Set<Node> nodesToRemove = Sets.newHashSet();
		for (int i = 0; i < length; i++) {
			Node node = nodes.item(i);
			String nodeName = node.getNodeName().toLowerCase();
			if (!validTags.contains(nodeName)) {
				nodesToRemove.add(node);
			}
			
			NamedNodeMap attributes = node.getAttributes();
			Map<String,String> atts = Maps.newHashMap();
			for (int j = 0; j < attributes.getLength(); j++) {
				Node item = attributes.item(j);
				String localName = item.getLocalName();
				if (localName==null) {
					localName = item.getNodeName();
				}
				if (!validAttributes.containsEntry(nodeName, localName)) {
					atts.put(localName,item.getNamespaceURI());
				}
			}
			for (Entry<String, String> entry : atts.entrySet()) {
				String ns = entry.getValue();
				String name = entry.getKey();
				if (ns!=null) {
					attributes.removeNamedItemNS(ns,name);
				} else {
					attributes.removeNamedItem(name);
				}
			}
		}
		
		for (Node node : nodesToRemove) {
			Node parent = node.getParentNode();
			if (parent!=null) {
				while (node.getFirstChild()!=null) {
					Node child = node.getFirstChild();
					node.removeChild(child);
					parent.insertBefore(child, node);
				}
				parent.removeChild(node);
			}
		}
	}
	*/

	public void setAllowStructureTags(boolean allow) {
		this.allowStructureTags = allow;
	}
}
