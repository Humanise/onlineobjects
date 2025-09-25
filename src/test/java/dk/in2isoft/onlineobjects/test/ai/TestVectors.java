package dk.in2isoft.onlineobjects.test.ai;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
	
	@Test
	public void testIt() {
		String abs = "I dislike war, because it is a waste of life and resources. War is a terrible thing that destroys lives, families, and communities. It is a tragedy that should be avoided at all costs. I believe that we should work towards peace and understanding, rather than resorting to violence and conflict. We should strive to find common ground and resolve our differences through dialogue and cooperation, rather than through war. On another note, I love apples. I also love oranges. I also love bananas. To change the topic back, I also really hate world war I since it was a really boring war fought mainly in the trenches, not anything exicting like in moves."; 
		List<String> subjects = Arrays.stream(abs.split("\\.")).collect(Collectors.toList());
		subjects.add(abs);
		
		String[] questions = {"Do you love both oranges and apples and war?", "Do you love both lemons and apples and war?"};
		for (String question : questions) {
			List<Double> questionVector = intelligence.vectorize(question);
			for (String string : subjects) {
				
				List<Double> candidateV = intelligence.vectorize(string);
				double similarity = semanticService.compareVectors(questionVector, candidateV);
				log.info(similarity + " - " + string);
			}			
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