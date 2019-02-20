package dk.in2isoft.onlineobjects.test.plain;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import dk.in2isoft.onlineobjects.services.LanguageService;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;
import dk.in2isoft.onlineobjects.test.EssentialTests;

@Category(EssentialTests.class)
public class TestLanguageService extends AbstractSpringTestCase {
	
	//private static Logger log = LogManager.getLogger(TestLanguageService.class);
	
	@Autowired
	private LanguageService languageService;
	
	@Test
	public void testLanguageDetection() {
		{
			String danish = "Der var engang en mand, han boede i en spand. Spanden var af ler, nu kan jeg ikke mer.";
			Locale locale = languageService.getLocale(danish);
			Assert.assertEquals("da", locale.getLanguage());
		}
		{
			String english = "Identifier of the language that best matches a given content profile. The content profile is compared to generic language profiles based on material from various sources.";
			Locale locale = languageService.getLocale(english);
			Assert.assertEquals("en", locale.getLanguage());
		}
	}

	public void setLanguageService(LanguageService languageService) {
		this.languageService = languageService;
	}

}