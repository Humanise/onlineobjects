package dk.in2isoft.onlineobjects.test.model;

import static org.junit.Assert.assertEquals;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.Person;
import dk.in2isoft.onlineobjects.model.Word;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;
import dk.in2isoft.onlineobjects.test.EssentialTests;

@Category(EssentialTests.class)
public class TestQuery extends AbstractSpringTestCase {
	
	@Test
	public void testBasic() throws EndUserException {
		
		Query<Person> query = Query.of(Person.class);
		String hql = queryToHql(query);
		assertEquals("select distinct obj from dk.in2isoft.onlineobjects.model.Person as obj where obj.id>0 order by obj.name asc", hql);
	}
	
	@Test
	public void testFieldIn() throws EndUserException {
		
		Query<Word> query = Query.of(Word.class).withFieldIn(Word.TEXT_FIELD, new String[] {"eat","my","pants"});
		String hql = queryToHql(query);
		assertEquals("select distinct obj from dk.in2isoft.onlineobjects.model.Word as obj where obj.id>0 and text in (:text) order by obj.name asc", hql);
	}

	private String queryToHql(Query<?> query) {
		Session session = modelService.getSessionfactory().getCurrentSession();
		Transaction transaction = session.beginTransaction();
		org.hibernate.query.Query<?> hibernateQuery = query.createItemQuery(session);
		String string = hibernateQuery.getQueryString();
		transaction.commit();
		return string;
	}
}
