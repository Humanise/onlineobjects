package dk.in2isoft.onlineobjects.test.ai;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.onlineobjects.modules.intelligence.Intelligence;
import org.springframework.beans.factory.annotation.Autowired;

import dk.in2isoft.commons.lang.Matrix;
import dk.in2isoft.commons.lang.MatrixEntry;
import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.commons.parsing.HTMLDocument;
import dk.in2isoft.onlineobjects.modules.networking.HTMLService;
import dk.in2isoft.onlineobjects.services.SemanticService;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;
import dk.in2isoft.onlineobjects.test.EssentialTests;

@Category(EssentialTests.class)
public class TestVectors extends AbstractSpringTestCase {

	@Autowired
	SemanticService semanticService;

	@Autowired
	Intelligence intelligence;
	
	@Autowired
	HTMLService htmlService;

	private static final Logger log = LogManager.getLogger(TestVectors.class);

	@Test
	public void testVectors() {
		String subject = "water is wet";
		
		String[] cancidates = {
				"h2o is liquid",
				"a lake can have fish",
				"the ocean is wast",
				"the moon is round",
				"1 2 3 4",
				"a dog can be old",
				"donald trump is the president"
		};
		List<Double> sub = intelligence.vectorize(subject);
		for (String string : cancidates) {
			
			List<Double> candidateV = intelligence.vectorize(string);
			double similarity = semanticService.compareVectors(sub, candidateV);
			System.out.println(string + " - " + similarity);
		}
	}
	
	private class Item {
		HTMLDocument document;
		public String text;
		public List<Double> vector;
		public String title;
	}

	@Test
	public void testWikipedia() throws IOException {

		File folder = getTestFile("wikipedia");
		File[] files = folder.listFiles();
		List<Item> items = new ArrayList<>();
		for (File file : files) {
			var item = new Item();
			item.document = htmlService.getDocumentSilently(file, Strings.UTF8);
			item.text = item.document.getExtractedText();
			item.title = item.document.getTitle();
			item.vector = intelligence.vectorize(item.text);
			items.add(item);
		}
		
		Matrix<String, String, Double> matrix = new Matrix<>();
		
		for (Item item : items) {
			for (Item other : items) {
				double comparison = semanticService.compareVectors(item.vector, other.vector);
				matrix.put(item.title, other.title, comparison);
			}
		}
		logMatrix(matrix);
	}
	
	private void logMatrix(Matrix<String, String, Double> matrix) {
		log.info("-------- Entries by value --------");
		List<MatrixEntry<String,String,Double>> entries = matrix.getEntries();
		Collections.sort(entries, new Comparator<MatrixEntry<String,String,Double>>() {

			public int compare(MatrixEntry<String, String, Double> o1, MatrixEntry<String, String, Double> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
		});
		Map<String,String> exists = new HashMap<String, String>();
		for (MatrixEntry<String, String, Double> entry : entries) {
			if (entry.getX().equals(entry.getY())) {
				continue;
			}
			if (exists.containsKey(entry.getX()) && exists.get(entry.getX()).equals(entry.getY())) {
				continue;
			}
			log.info("----------------");
			log.info("value: "+entry.getValue());
			log.info("X: "+entry.getX());
			log.info("Y: "+entry.getY());
			exists.put(entry.getX(), entry.getY());
			exists.put(entry.getY(), entry.getX());
		}
	}
}