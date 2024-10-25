package org.onlineobjects.modules.suggestion;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import dk.in2isoft.onlineobjects.services.LanguageService;
import dk.in2isoft.onlineobjects.services.SemanticService;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;
import opennlp.tools.doccat.DoccatFactory;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DocumentSample;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.TrainingParameters;

public class TestSuggestion extends AbstractSpringTestCase {
	
	@Autowired
	LanguageService language;

	@Autowired
	SemanticService semanticService;

	public class TestStream implements ObjectStream<DocumentSample> {
		
		private Iterator<String> files;

		public TestStream(String[] f) {
			files = Arrays.stream(f).iterator();
		}
		
		@Override
		public DocumentSample read() throws IOException {
			if (files.hasNext()) {
				String file = files.next();
				String text = getTestFileAsString(file);			
				return new DocumentSample(file, tokenize(text));				
			}
			return null;
		}

	}

	@Test
	public void testIt() throws IOException {
		DoccatFactory factory = new DoccatFactory();
		String[] x = {
				"texts/wikipedia.whale.en.txt",
				"texts/obama_drone.en.txt"
			};
		ObjectStream<DocumentSample> stream = new TestStream(x);
		DoccatModel model = DocumentCategorizerME.train("en", stream, new TrainingParameters(), factory);

		DocumentCategorizerME categorizer = new DocumentCategorizerME(model);
		
		double[] categorize = categorizer.categorize(tokenize(getTestFileAsString("texts/wikipedia.blue-whale.en.txt")));
		for (int i = 0; i < categorize.length; i++) {
			String category = categorizer.getCategory(i);
			System.out.println(category + ":" + categorize[i]);
		}
	}

	private String[] tokenize(String string) {
		return semanticService.getWords(string);
	}
	
	
}
