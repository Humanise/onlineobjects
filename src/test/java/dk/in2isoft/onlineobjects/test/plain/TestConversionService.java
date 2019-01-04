package dk.in2isoft.onlineobjects.test.plain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.ImagePart;
import dk.in2isoft.onlineobjects.model.conversion.EntityConverter;
import dk.in2isoft.onlineobjects.model.conversion.ImagePartConverter;
import dk.in2isoft.onlineobjects.services.ConversionService;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;

public class TestConversionService extends AbstractSpringTestCase {
	
	//private static Logger log = LogManager.getLogger(TestConversionService.class);
	
	@Autowired
	private ConversionService conversionService;
	
	@Test
	public void testIt() throws EndUserException, FileNotFoundException, IOException {
		ImagePart part = new ImagePart();
		modelService.create(part, getAdminUser());
		EntityConverter converter = conversionService.getConverter(part);
		assertEquals(ImagePartConverter.class, converter.getClass());
		assertNotNull(converter);
		
		assertNotNull(converter.generateXML(part, getAdminUser()));
		
		modelService.delete(part, getAdminUser());
	}

	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}
}