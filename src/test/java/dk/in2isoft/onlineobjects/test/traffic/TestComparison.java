package dk.in2isoft.onlineobjects.test.traffic;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Maps;

import dk.in2isoft.commons.lang.Matrix;
import dk.in2isoft.commons.lang.MatrixEntry;
import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.commons.parsing.HTMLDocument;
import dk.in2isoft.onlineobjects.modules.networking.HTMLService;
import dk.in2isoft.onlineobjects.services.SemanticService;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;
import dk.in2isoft.onlineobjects.util.semantics.English;
import dk.in2isoft.onlineobjects.util.semantics.Language;

public class TestComparison extends AbstractSpringTestCase {
	
	private static Logger log = LogManager.getLogger(TestComparison.class);
	
	@Autowired
	private SemanticService semanticService;
	
	@Autowired
	private HTMLService htmlService;
	
	@Test
	public void testWikipedia() throws Exception {
		
		File folder = getTestFile("wikipedia");
		File[] files = folder.listFiles();
		compareUrls(files, new English());
		
	}

	private void compareUrls(File[] urls, Language language) {
		StopWatch watch = new StopWatch();
		log.info("Reading files...");
		watch.start();
		Map<String,String> docs = Maps.newHashMap();
		for (File url : urls) {
			HTMLDocument document = htmlService.getDocumentSilently(url, Strings.UTF8);
			if (document!=null) {
				String text = document.getExtractedText();
				if (text == null) {
					throw new IllegalStateException("No text for "+url);
				}
				docs.put(document.getTitle().replace(" - Wikipedia", ""), text);
			}
		}
		watch.split();
		log.info("Files read: " + watch.getSplitTime());
		
		Matrix<String, String, Double> matrix = new Matrix<String, String, Double>();
		for (Entry<String, String> doc1 : docs.entrySet()) {
			for (Entry<String, String> doc2 : docs.entrySet()) {
				if (matrix.getValue(doc1.getKey(), doc2.getKey()) != null) {
					continue;
				}
				if (matrix.getValue(doc2.getKey(), doc1.getKey()) != null) {
					continue;
				}
				watch.reset();
				watch.start();
				double comparison = semanticService.compare(doc1.getValue(), doc2.getValue(),language);
				matrix.put(doc1.getKey(), doc2.getKey(), comparison);
				watch.stop();
				log.info("Comparing: {} to {} in {}", doc1.getKey(), doc2.getKey(), watch.getTime());
			}
		}
		
		
		
		log.info("\n"+matrix.toString());

		StringBuilder entiretext = new StringBuilder();
		for (String string : docs.values()) {
			entiretext.append(" ").append(string);
		}
		
		final Map<String, Integer> freq = semanticService.getWordFrequency(entiretext.toString().toLowerCase(),language);
		Map<String, Integer> sorted = new java.util.TreeMap<String,Integer>(new Comparator<String>() {
			public int compare(String o1, String o2) {
				return freq.get(o1).compareTo(freq.get(o2));
			}
		});
		sorted.putAll(freq);
		
		log.info("Frequency: "+freq);
		
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

	public void setSemanticService(SemanticService semanticService) {
		this.semanticService = semanticService;
	}

	public SemanticService getSemanticService() {
		return semanticService;
	}
}