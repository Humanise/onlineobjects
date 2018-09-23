package dk.in2isoft.onlineobjects.test.hui;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;
import dk.in2isoft.onlineobjects.ui.HUIService;

public class TestRendering extends AbstractSpringTestCase {
	
	//private static Logger log = LogManager.getLogger(TestRendering.class);
	
	@Autowired
	private HUIService huiService;

	@Test
	public void testSimple() throws Exception {
		String xml = "<button text='Button text'/>";
		String result = huiService.renderFragment(xml);
		Assert.assertTrue(result.contains("class=\"hui_button\""));
	}

	public void setHUIService(HUIService huiService) {
		this.huiService = huiService;
	}
}