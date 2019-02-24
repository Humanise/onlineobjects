package dk.in2isoft.onlineobjects.test.model;

import org.apache.commons.lang.time.StopWatch;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.modules.language.WordService;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;
import dk.in2isoft.onlineobjects.test.EssentialTests;

@Category(EssentialTests.class)
public class TestModelPerformance extends AbstractSpringTestCase {

	@Autowired
	private WordService wordService;
    	
	@Test
	public void testThis() throws EndUserException {
		
		Operator admin = modelService.newAdminOperator();
		StopWatch watch = new StopWatch();
		watch.start();
		wordService.getSource("http://wordnet.princeton.edu", admin);
		watch.stop();
		admin.commit();
		System.out.println(watch.getTime());
	}
	
	public void setWordService(WordService wordService) {
		this.wordService = wordService;
	}
}
