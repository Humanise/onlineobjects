package dk.in2isoft.onlineobjects.model.conversion;

import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.Image;
import nu.xom.Element;
import nu.xom.Node;

public class ImageConverter extends EntityConverter {

	@Override
	protected Node generateSubXML(Entity entity, Operator privileged) {
		Image image = (Image) entity;
		image.getIcon();
		Element root = new Element("Image",Image.NAMESPACE);
		Element contentType = new Element("contentType",Image.NAMESPACE);
		contentType.appendChild(image.getContentType());
		root.appendChild(contentType);
		Element fileSize = new Element("fileSize",Image.NAMESPACE);
		fileSize.appendChild(String.valueOf(image.getFileSize()));
		root.appendChild(fileSize);
		Element width = new Element("width",Image.NAMESPACE);
		width.appendChild(String.valueOf(image.getWidth()));
		root.appendChild(width);
		Element height = new Element("height",Image.NAMESPACE);
		height.appendChild(String.valueOf(image.getHeight()));
		root.appendChild(height);
		
		return root;
	}
	
	@Override
	public Class<? extends Entity> getType() {
		return Image.class;
	}

}
