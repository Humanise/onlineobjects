package dk.in2isoft.onlineobjects.test.model;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.Word;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;
import dk.in2isoft.onlineobjects.test.EssentialTests;

@Category(EssentialTests.class)
public class TestWord extends AbstractSpringTestCase {

	@Test
	public void testCreate() throws EndUserException {
		Operator operator = modelService.newAdminOperator();
		String text = new Date().toString();
		Word word = new Word();
		word.setText(text);
		assertEquals(text, word.getText());
		modelService.create(word, operator);
		{
			Query<Word> query = Query.of(Word.class).withField("text", text);
			List<Word> list = modelService.list(query, operator);
			assertEquals(1, list.size());
			
			Word loaded = list.iterator().next();
			assertEquals(loaded.getText(), text);
		}
		
		modelService.delete(word, operator);
		operator.commit();
	}
}
