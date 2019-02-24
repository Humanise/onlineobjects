package dk.in2isoft.onlineobjects.test.model;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.Person;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;
import dk.in2isoft.onlineobjects.test.EssentialTests;


@Category(EssentialTests.class)
public class TestPerson extends AbstractSpringTestCase {

	@Test
	public void testCreate() throws EndUserException {
		Operator adminOperator = modelService.newAdminOperator();

		String givenName = Strings.generateRandomString(5);
		String additionalName = Strings.generateRandomString(5);
		String familyName = Strings.generateRandomString(5);
		Person person = new Person();
		person.setGivenName(givenName);
		person.setAdditionalName(additionalName);
		person.setFamilyName(familyName);
		assertEquals(givenName+" "+additionalName+" "+familyName, person.getName());
		modelService.create(person, adminOperator);
		{
			Query<Person> query = Query.of(Person.class).withName(givenName+" "+additionalName+" "+familyName);
			List<Person> list = modelService.list(query, adminOperator);
			assertEquals(1, list.size());
		}
		
		modelService.delete(person, adminOperator);
		adminOperator.commit();
	}
	
	@Test
	public void testName() {
		{
			Person person = new Person();
			person.setFullName("Ludwig Mies van der Rohe");
			assertEquals("Ludwig", person.getGivenName());
			assertEquals("Mies van der", person.getAdditionalName());
			assertEquals("Rohe", person.getFamilyName());
			
			person.setFullName(null);
			Assert.assertNull(person.getGivenName());
			Assert.assertNull(person.getAdditionalName());
			Assert.assertNull(person.getFamilyName());
			
			person.setFullName("");
			Assert.assertNull(person.getGivenName());
			Assert.assertNull(person.getAdditionalName());
			Assert.assertNull(person.getFamilyName());
			
			person.setFullName("Jonas Munk");
			assertEquals("Jonas", person.getGivenName());
			assertEquals(null, person.getAdditionalName());
			assertEquals("Munk", person.getFamilyName());
			
			person.setFullName("Jonas");
			assertEquals("Jonas", person.getGivenName());
			assertEquals(null, person.getAdditionalName());
			assertEquals(null, person.getFamilyName());
			
			person.setFullName("  Jonas \n\tBrinkmann  \nMunk   \n");
			assertEquals("Jonas", person.getGivenName());
			assertEquals("Brinkmann", person.getAdditionalName());
			assertEquals("Munk", person.getFamilyName());
		}
	}
}
