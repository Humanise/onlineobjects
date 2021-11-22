package dk.in2isoft.onlineobjects.test.ai;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import dk.in2isoft.commons.lang.Files;
import dk.in2isoft.onlineobjects.core.Pair;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;
import dk.in2isoft.onlineobjects.test.EssentialTests;
import opennlp.tools.doccat.DoccatFactory;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DocumentSample;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.TrainingParameters;

@Category(EssentialTests.class)
public class TestTextCategorization extends AbstractSpringTestCase {


	private static final Logger log = LogManager.getLogger(TestTextCategorization.class);

	@Test
	public void testDocumentCategorization() throws Exception {			
		ObjectStream<DocumentSample> sampleStream = new TrainingSampleStream(getTestFile("training"));

		DoccatFactory factory = new DoccatFactory();
		DoccatModel model = DocumentCategorizerME.train("en", sampleStream, new TrainingParameters(), factory);

		String[] texts = {"Why does my tooth hurt?", "Bugs are small", "How do I loose weight?", "Do dogs get to go to heaven?", "Should I get vaccinated?"};
		for (String text : texts) {
			DocumentCategorizerME myCategorizer = new DocumentCategorizerME(model);
			double[] outcomes = myCategorizer.categorize(text);
			String category = myCategorizer.getBestCategory(outcomes);
			log.info("Best category: {} = {}", text, category);
		}
	}

	private final class TrainingSampleStream implements ObjectStream<DocumentSample> {
		
		private List<Pair<String, File>> pairs = new ArrayList<>();
		private Iterator<Pair<String, File>> iter;
		
		private TrainingSampleStream(File testFile) {
			for (File sub :  testFile.listFiles()) {
				if (sub.isDirectory()) {
					for (File file : sub.listFiles()) {
						if (file.getName().endsWith(".txt")) {
							pairs.add(Pair.of(sub.getName(), file));
						}
					}
				}
			}
			iter = pairs.iterator();
		}

		@Override
		public DocumentSample read() throws IOException {
			if (iter.hasNext()) {
				Pair<String,File> pair = iter.next();
				String text = Files.readString(pair.getValue());
				return new DocumentSample(pair.getKey(), text);
			}
			return null;
		}

		@Override
		public void reset() throws IOException, UnsupportedOperationException {
			iter = pairs.iterator();
		}

		@Override
		public void close() throws IOException {
			
		}
	}

}