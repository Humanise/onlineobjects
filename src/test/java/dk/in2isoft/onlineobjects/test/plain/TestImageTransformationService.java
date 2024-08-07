package dk.in2isoft.onlineobjects.test.plain;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.Image;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;
import dk.in2isoft.onlineobjects.test.EssentialTests;
import dk.in2isoft.onlineobjects.util.images.ImageProperties;
import dk.in2isoft.onlineobjects.util.images.ImageService;
import dk.in2isoft.onlineobjects.util.images.ImageTransformation;
import dk.in2isoft.onlineobjects.util.images.ImageTransformationService;

@Category(EssentialTests.class)
public class TestImageTransformationService extends AbstractSpringTestCase {
	
	@Autowired
	private ImageService imageService;

	@Autowired
	private ImageTransformationService imageTransformationService;

	@Test
	public void testCropImage() throws EndUserException, IOException {
		File file = getTestFile("testImageWithGPS.jpg");
		File copy = File.createTempFile("testImage", "jpg");
		org.apache.commons.io.FileUtils.copyFile(file, copy);
		Assert.assertTrue(copy.exists());
		Operator operator = modelService.newAdminOperator();
		
		Image image = imageService.createImageFromFile(copy, "test image", operator);
		Assert.assertNotNull(image);
		{
			ImageTransformation transformation = new ImageTransformation();
			transformation.setCropped(true);
			transformation.setWidth(20);
			transformation.setHeight(40);
			File thumbnail = imageTransformationService.transform(image.getId(), transformation);
			ImageProperties propeties = imageService.getImageProperties(thumbnail);
			assertEquals(20,propeties.getWidth());
			assertEquals(40,propeties.getHeight());
		}
		{
			ImageTransformation transformation = new ImageTransformation();
			transformation.setWidth(100);
			transformation.setHeight(100);
			File thumbnail = imageTransformationService.transform(image.getId(), transformation);
			ImageProperties propeties = imageService.getImageProperties(thumbnail);
			assertEquals(100,propeties.getWidth());
			assertEquals(75,propeties.getHeight());
		}
		modelService.delete(image, operator);
		operator.commit();
	}
	
	@Test
	public void testSharpen() throws IOException, EndUserException {
		File file = getTestFile("testImageWithGPS.jpg");
		ImageTransformation transform = new ImageTransformation();
		transform.setWidth(300);
		transform.setHeight(200);
		{			
			File converted = new File(getOutputDir(),"testImageWithGPS_300x200.jpg");
			if (converted.exists()) {
				converted.delete();
			}
			imageTransformationService.transform(file, transform, converted);
		}
		transform.setSharpen(1);
		{			
			File converted = new File(getOutputDir(),"testImageWithGPS_300x200_sharper.jpg");
			if (converted.exists()) {
				converted.delete();
			}
			imageTransformationService.transform(file, transform, converted);
		}
	}

	@Test
	public void testPDF() throws IOException, EndUserException {
		File pdf = getTestFile("images/humanise.pdf");
		{
			ImageProperties properties = imageService.getImageProperties(pdf);
			assertEquals("application/pdf", properties.getMimeType());
		}
		{			
			ImageTransformation transform = new ImageTransformation();
			transform.setWidth(300);
			transform.setHeight(200);
			transform.setCropped(true);
			transform.setFormat("jpg");
			File converted = new File(getOutputDir(),"humanise_300x200.jpg");
			if (converted.exists()) {
				converted.delete();
			}
			imageTransformationService.transform(pdf, transform, converted);
			ImageProperties properties = imageService.getImageProperties(converted);
			assertEquals("image/jpeg", properties.getMimeType());
			assertEquals(300, properties.getWidth());
			assertEquals(200, properties.getHeight());
		}

		{			
			ImageTransformation transform = new ImageTransformation();
			transform.setWidth(300);
			transform.setHeight(200);
			transform.setCropped(true);
			transform.setFormat("png");
			File converted = new File(getOutputDir(),"humanise_300x200.png");
			if (converted.exists()) {
				converted.delete();
			}
			imageTransformationService.transform(pdf, transform, converted);
			ImageProperties properties = imageService.getImageProperties(converted);
			assertEquals("image/png", properties.getMimeType());
			assertEquals(300, properties.getWidth());
			assertEquals(200, properties.getHeight());
		}
	}

	public void setImageService(ImageService imageService) {
		this.imageService = imageService;
	}
	
	public void setImageTransformationService(ImageTransformationService imageTransformationService) {
		this.imageTransformationService = imageTransformationService;
	}
}