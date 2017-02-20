package dk.in2isoft.onlineobjects.test.webservices;

import org.apache.log4j.Logger;
import org.geonames.Toponym;
import org.geonames.ToponymSearchCriteria;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;
import org.junit.Ignore;
import org.junit.Test;

import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;

@Ignore
public class TestGeonames extends AbstractSpringTestCase {

	private static final Logger log = Logger.getLogger(TestGeonames.class);

	@Test
	public void testRead() throws Exception {
		WebService.setUserName("demo"); // add your username here

		ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
		searchCriteria.setQ("H/F Sundbyvester, Rubingangen 49, 2300 København S, Denmark");
		ToponymSearchResult searchResult = WebService.search(searchCriteria);
		for (Toponym toponym : searchResult.getToponyms()) {
			log.info(toponym.getName() + " " + toponym.getCountryName());
		}

	}

}
