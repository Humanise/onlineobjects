package org.onlineobjects.modules.photos;

import dk.in2isoft.onlineobjects.model.Image;
import dk.in2isoft.onlineobjects.util.images.ImageTransformation;

public interface ImageTransformationProvider {

	ImageTransformation getTransformation(Image image);
}
