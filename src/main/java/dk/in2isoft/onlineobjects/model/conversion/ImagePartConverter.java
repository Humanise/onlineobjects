package dk.in2isoft.onlineobjects.model.conversion;

import java.util.List;

import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.Image;
import dk.in2isoft.onlineobjects.model.ImagePart;
import dk.in2isoft.onlineobjects.services.ConversionService;
import nu.xom.Element;
import nu.xom.Node;

public class ImagePartConverter extends EntityConverter {
	
	private ModelService modelService;
	private ConversionService conversionService;

	@Override
	protected Node generateSubXML(Entity entity, Privileged privileged) throws ModelException {
		ImagePart part = (ImagePart) entity;
		Element root = new Element("ImagePart",ImagePart.NAMESPACE);
		List<Image> children = modelService.getChildren(part, Image.class, privileged);
		for (Image image : children) {
			Node node = conversionService.generateXML(image, privileged);
			root.appendChild(node);
		}
		return root;
	}
	
	@Override
	public Class<? extends Entity> getType() {
		return ImagePart.class;
	}

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
	
	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}
}
