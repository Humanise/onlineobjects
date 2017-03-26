package dk.in2isoft.onlineobjects.ui;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.converters.DOMConverter;

import org.apache.xerces.dom.DOMImplementationImpl;

import dk.in2isoft.onlineobjects.core.Core;
import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.model.Entity;


public abstract class XSLTInterfaceAdapter extends XSLTInterface {

	@Override
	public final org.w3c.dom.Document getData(Privileged privileged) throws ModelException {
		return DOMConverter.convert(build(privileged),new DOMImplementationImpl());
	}
	
	@Override
	public final Document getDocument(Privileged privileged) throws ModelException {
		return build(privileged);
	}

	private Document build(Privileged privileged) throws ModelException {
		Element page = new Element("page",NAMESPACE_PAGE);
		Document doc = new Document(page);
		buildContent(page, privileged);
		return doc;
	}

	protected String convertToXML(Entity entity, Privileged privileged) throws ModelException {
		return Core.getInstance().getConversionService().generateXML(entity, privileged).toXML();
	}

	protected Node convertToNode(Entity entity, Privileged privileged) throws ModelException {
		return Core.getInstance().getConversionService().generateXML(entity, privileged);
	}
	
	protected abstract void buildContent(Element parent, Privileged privileged) throws ModelException;

	protected Element create(String name) {
		return new Element(name,NAMESPACE_PAGE);
	}

	protected Element createPageNode(Element parent, String name) {
		Element element = new Element(name,NAMESPACE_PAGE);
		parent.appendChild(element);
		return element;
	}

	protected Element create(String name, String text) {
		Element element = new Element(name,NAMESPACE_PAGE);
		element.appendChild(text);
		return element;
	}

}
