package dk.in2isoft.onlineobjects.test.plain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import dk.in2isoft.commons.lang.MimeTypes;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.Image;
import dk.in2isoft.onlineobjects.model.Location;
import dk.in2isoft.onlineobjects.model.Property;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;
import dk.in2isoft.onlineobjects.test.EssentialTests;
import dk.in2isoft.onlineobjects.util.images.ImageMetaData;
import dk.in2isoft.onlineobjects.util.images.ImageProperties;
import dk.in2isoft.onlineobjects.util.images.ImageService;

@Category(EssentialTests.class)
public class TestImageService extends AbstractSpringTestCase {
	
	@Autowired
	private ImageService imageService;

	@Test
	public void testMetaData() throws EndUserException, IOException {
		File file = getTestFile("testImageWithGPS.jpg");
		ImageProperties imageDimensions = imageService.getImageProperties(file);
		assertEquals(1600,imageDimensions.getWidth());
		assertEquals(1200,imageDimensions.getHeight());
		ImageMetaData metaData = imageService.getMetaData(file);
		assertEquals("Apple",metaData.getCameraMake());
		assertEquals("iPhone",metaData.getCameraModel());
		assertEquals(Double.valueOf(57.225833333333334),metaData.getLatitude());
		assertEquals(Double.valueOf(9.515666666666666),metaData.getLongitude());
	}

	@Test
	public void testInfo() throws EndUserException, IOException {
		File file = getTestFile("testImageWithGPS.jpg");
		ImageProperties properties = imageService.getImageProperties(file);
		assertNotNull(properties);
		assertEquals(1600,properties.getWidth());
		assertEquals(1200,properties.getHeight());
		assertEquals(MimeTypes.IMAGE_JPEG,properties.getMimeType());
	}

	@Test
	public void testCreateImageFromFile() throws EndUserException, IOException {
		File file = getTestFile("testImageWithGPS.jpg");
		File copy = File.createTempFile("testImage", "jpg");
		org.apache.commons.io.FileUtils.copyFile(file, copy);
		Assert.assertTrue(copy.exists());
		Operator operator = modelService.newAdminOperator();
		
		Image image = imageService.createImageFromFile(copy, "test image", operator);
		Assert.assertNotNull(image);
		assertEquals(1600,image.getWidth());
		assertEquals(1200,image.getHeight());
		assertEquals("Apple",image.getPropertyValue(Property.KEY_PHOTO_CAMERA_MAKE));
		assertEquals("iPhone",image.getPropertyValue(Property.KEY_PHOTO_CAMERA_MODEL));

		// Check the location
		Location location = modelService.getParent(image, Location.class, operator);
		Assert.assertNotNull(location);
		assertEquals(Double.valueOf(57.225833333333334),Double.valueOf(location.getLatitude()));
		assertEquals(Double.valueOf(9.515666666666666),Double.valueOf(location.getLongitude()));
		
		// Clean up
		modelService.delete(image, operator);
		modelService.delete(location, operator);
		
		operator.commit();
	}
	
	public void setImageService(ImageService imageService) {
		this.imageService = imageService;
	}

}