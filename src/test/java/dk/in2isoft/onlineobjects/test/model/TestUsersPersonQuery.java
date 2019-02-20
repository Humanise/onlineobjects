package dk.in2isoft.onlineobjects.test.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import dk.in2isoft.onlineobjects.core.PairSearchResult;
import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.UsersPersonQuery;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.Person;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;
import dk.in2isoft.onlineobjects.test.EssentialTests;

@Category(EssentialTests.class)
public class TestUsersPersonQuery extends AbstractSpringTestCase {
    	
	@Test
	public void testThis() throws EndUserException {
		User user = getNewTestUser();
		Person person = new Person();
		person.setGivenName("test89898897");
		modelService.create(user, getAdminUser());
		Privileged priviledged = user;
		modelService.create(person, priviledged);
		modelService.createRelation(user, person, Relation.KIND_SYSTEM_USER_SELF, priviledged);
		{
			UsersPersonQuery query = new UsersPersonQuery().withUsername(user.getUsername());
			PairSearchResult<User,Person> pairs = modelService.searchPairs(query);
			assertEquals(1, pairs.getTotalCount());
		}
		{
			UsersPersonQuery query = new UsersPersonQuery().withWords("test89898897");
			PairSearchResult<User,Person> pairs = modelService.searchPairs(query);
			assertEquals(1, pairs.getTotalCount());
			assertEquals(pairs.getFirst().getKey().getId(),user.getId());
		}
		modelService.delete(person, priviledged);
		modelService.delete(user, getAdminUser());
		{
			UsersPersonQuery query = new UsersPersonQuery().withUsername("unitTestUser");
			PairSearchResult<User,Person> pairs = modelService.searchPairs(query);
			assertEquals(0, pairs.getTotalCount());
		}
		modelService.commit();
	}
}
