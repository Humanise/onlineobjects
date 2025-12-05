package org.onlineobjects.modules.photos;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.model.Image;
import dk.in2isoft.onlineobjects.model.Property;
import dk.in2isoft.onlineobjects.util.images.ImageInfo;
import dk.in2isoft.onlineobjects.util.images.ImageService;
import dk.in2isoft.onlineobjects.util.images.ImageTransformation;

public class Photos {

	private ImageService imageService;

	public static List<Size> PHOTO_CONTAINER_SIZES = List.of(
		Size.of(375, 530), // iPhone 6/7/8
		Size.of(375*2, 530*2), // iPhone 6/7/8
		Size.of(768*2, 800*2), // iPad
		Size.of(1024*2, 800*2), // iPad Pro
		Size.of(1024*2, 1259*2), // iPad Pro full,
		Size.of(1024*2, 1292*2), // iPad Pro full + tabs,
		Size.of(1200*2, 800*2),
		Size.of(1366*2, 917*2), // iPad Pro full + tabs
		Size.of(1366*2, 950*2), // iPad Pro full
		Size.of(1600*2, 800*2),
		Size.of(2000*2, 800*2)
	);

	public String urlForSize(Long imageId, Size size) {
		ImageTransformation t = new ImageTransformation();
		t.setWidth(size.getWidth());
		t.setHeight(size.getHeight());
		t.setQuality(0.9f);
		t.setSharpen(getSharpening(size));
		return urlFor(imageId, t);
	}

	private float getSharpening(Size size) {
		float sharpen = (float)size.getWidth() / 2000f;
		sharpen = Math.max(0.2f, Math.min(sharpen, 1f));
		return sharpen;
	}

	public String urlFor(Long imageId, ImageTransformation transformation) {
		StringBuilder url = new StringBuilder();
		url.append("/service/image/id" + imageId);
		if (transformation.getWidth() > 0) {
			url.append("width").append(transformation.getWidth());
		}
		if (transformation.getHeight() > 0) {
			url.append("height").append(transformation.getHeight());
		}
		if (transformation.getSharpen() > 0) {
			url.append("sharpen").append(transformation.getSharpen());
		}
		if (transformation.getSharpen() > 0) {
			url.append("quality").append(transformation.getQuality());
		}
		return url.toString();
	}

	public Size fit(Size box, Size container, boolean upscale) {
		float boxRatio = (float)box.getWidth() / (float)box.getHeight();
		float containerRatio = (float)container.getWidth() / (float)container.getHeight();
		int width, height;
		  if (upscale==false && box.getWidth()<=container.getWidth() && box.getHeight()<=container.getHeight()) {
		    width = box.getWidth();
		    height = box.getHeight();
		  }
		  else if (boxRatio > containerRatio) {
		    width = container.getWidth();
		    height = Math.round((float)container.getWidth()/(float)box.getWidth() * (float)box.getHeight());
		  } else {
		    width = Math.round((float)container.getHeight()/(float)box.getHeight() * (float)box.getWidth());
		    height = container.getHeight();
		  }
		  return Size.of(width, height);
	}

	public String getCombinedCamera(ImageInfo info) {
		String model = info.getCameraModel();
		String make = info.getCameraMake();
		StringBuilder sb = new StringBuilder();
		if (Strings.isNotBlank(model)) {
			sb.append(info.getCameraModel());
		}
		if (Strings.isNotBlank(make)) {
			if (!sb.toString().toLowerCase().contains(make.toLowerCase())) {
				if (sb.length() > 0) {
					sb.insert(0, " ");
				}
				sb.insert(0, make);
			}
		}
		return sb.toString();
	}

	public Size getDisplaySize(Image image) {
		boolean rotated = isRotated(image);
		int height = image.getHeight();
		int width = image.getWidth();
		if (rotated) {
			width = image.getHeight();
			height = image.getWidth();
		}
		return Size.of(width, height);
	}

	private boolean isRotated(Image image) {
		Double rotation = image.getPropertyDoubleValue(Property.KEY_PHOTO_ROTATION);
		boolean rotated = rotation != null && (Math.abs(rotation) == 90);
		return rotated;
	}


	public List<ScaledImage> buildScaledSizes(Size image, Long imageId) {
		List<Size> all = new ArrayList<>();
		all.addAll(Photos.PHOTO_CONTAINER_SIZES);
		all.add(image);
		List<ScaledImage> sizes = all.stream()
			.filter(size -> size.getWidth() <= image.getWidth() && size.getHeight() <= image.getHeight())
			.map(container -> fit(image, container, false))
			.sorted((a, b) -> Integer.compare(a.getWidth(), b.getWidth())).map(s -> {
				return ScaledImage.of(s, urlForSize(imageId, s));
			}).distinct().collect(Collectors.toList());
		return sizes;
	}

	public String asSourceSet(List<ScaledImage> sizes) {
		return sizes.stream().map(s -> s.getUrl() + " " + s.getWidth() + "w").distinct().collect(joining(", "));
	}

	@Autowired
	public void setImageService(ImageService imageService) {
		this.imageService = imageService;
	}

	public ImageInfo getImageInfo(Image image, Operator operator) throws ModelException {
		return imageService.getImageInfo(image, operator);
	}
}
