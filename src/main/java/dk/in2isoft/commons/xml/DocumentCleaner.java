package dk.in2isoft.commons.xml;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.ParentNode;
import nu.xom.XPathContext;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class DocumentCleaner {

	Multimap<String, String> validAttributes = HashMultimap.create();
	Set<String> validTags = Sets.newHashSet(); 

	public DocumentCleaner() {
		validAttributes.put("a", "href");
		validAttributes.put("a", "title");
		validAttributes.put("img", "src");
		validAttributes.put("img", "title");
		validAttributes.put("img", "width");
		validAttributes.put("img", "height");
		
		validTags.addAll(Sets.newHashSet("html","head","body","title"));
		validTags.addAll(Sets.newHashSet("h1","h2","h3","h4","h5","h6","p"));
		validTags.addAll(Sets.newHashSet("strong","em","a","img"));
		validTags.addAll(Sets.newHashSet("table","tbody","tr","th","td","thead","tfoot","colgroup","col","caption"));
		validTags.addAll(Sets.newHashSet("dl","dt","dd"));
		validTags.addAll(Sets.newHashSet("ul","ol","li"));
		validTags.addAll(Sets.newHashSet("blockquote","figure","pre","code"));
	}
	
	public void clean(nu.xom.Document document) {
		XPathContext context = new XPathContext("html", document.getRootElement().getNamespaceURI());
		Nodes nodes = document.query("//html:*",context);
		int length = nodes.size();
		Set<Element> nodesToRemove = Sets.newHashSet();
		for (int i = 0; i < length; i++) {
			nu.xom.Node node = nodes.get(i);
			if (node instanceof Element) {
				Element element = (Element) node;
				String nodeName = element.getLocalName().toLowerCase();
				if (!validTags.contains(nodeName)) {
					nodesToRemove.add(element);
				}
				
				element.getAttributeCount();
				for (int j = element.getAttributeCount() - 1; j >= 0; j--) {
					Attribute attribute = element.getAttribute(j);
					if (!validAttributes.containsEntry(nodeName, attribute.getLocalName())) {
						element.removeAttribute(attribute);
					}
				}
				
				for (Element toRemove : nodesToRemove) {
					ParentNode parent = toRemove.getParent();
					if (parent!=null) {
						int index = parent.indexOf(toRemove);
						while (toRemove.getChildCount()>0) {
							nu.xom.Node child = toRemove.removeChild(0);
							parent.insertChild(child, index);
							index ++;
						}
						parent.removeChild(toRemove);
					}
				}
			}
		}
		
	}
	
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
				if (!validAttributes.containsEntry(nodeName, item.getNodeName())) {
					atts.put(item.getNodeName(),item.getNamespaceURI());
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
}