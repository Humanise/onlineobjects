package dk.in2isoft.onlineobjects.test.wordnet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang.time.StopWatch;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.Duration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.common.collect.Lists;

import dk.in2isoft.onlineobjects.modules.dannet.QueryHandler;
import dk.in2isoft.onlineobjects.test.AbstractTestCase;
import dk.in2isoft.onlineobjects.test.EssentialTests;

@Category(EssentialTests.class)
public class TestWordNetRDF extends AbstractTestCase {

	private static final Logger log = LogManager.getLogger(TestWordNetRDF.class);
	private static Model model;
	private static Graph graph;
	private static QueryHandler query;
	private static StopWatch watch;

	private static final Node GLOSSARY = NodeFactory.createURI("http://www.w3.org/2006/03/wn/wn20/schema/gloss");
	private static Node wordURI = NodeFactory.createURI("http://www.w3.org/2006/03/wn/wn20/schema/Word");
	private static Node wordRelation = NodeFactory.createURI("http://www.w3.org/2006/03/wn/wn20/schema/word");
	private static Node lexicalForm = NodeFactory.createURI("http://www.w3.org/2006/03/wn/wn20/schema/lexicalForm");
	private static Node partOfSpeech = NodeFactory.createURI("http://www.wordnet.dk/owl/instance/2009/03/schema/partOfSpeech");
	private static final Node CONTAINS_SENSE = NodeFactory.createURI("http://www.w3.org/2006/03/wn/wn20/schema/containsWordSense");
	
	@BeforeClass
	public static void before() throws Exception {
		watch = new StopWatch();
		watch.start();
		model = ModelFactory.createDefaultModel();
		read("words.rdf");
		read("wordsenses.rdf");
		read("glossary.rdf");
		read("part_of_speech.rdf");
		read("synsets.rdf");

		graph = model.getGraph();
		
		query = new QueryHandler(graph);
	}

	private static void read(String fileName) throws FileNotFoundException, IOException {
		InputStream url = ClassLoader.getSystemResourceAsStream("DanNet-2.1_owl/"+fileName);
		model.read(url, "UTF-8");
		url.close();
		log.info("imported: "+fileName+" : "+new Duration(watch.getTime()));
	}
	
	@Test
	public void testGlossaries() {
		int i = 0;
		ExtendedIterator<Triple> gloss = graph.find(null, GLOSSARY, null);
		while (gloss.hasNext() && i<10) {
			Triple relation = gloss.next();
			Node synset = relation.getSubject();
			print("- synset - objects",Lists.newArrayList(query.objectsFor(synset, null)));
			print("- synset - subjects",Lists.newArrayList(query.subjectsFor(synset, null)));
			print("gloss",relation);
			i++;
		}		
	}
	
	private Node first(ExtendedIterator<Node> iterator) {
		if (iterator.hasNext()) {
			return iterator.next();
		}
		return null;
	}
	
	@Test
	public void testIt() throws Exception {
		
		ExtendedIterator<Triple> words = graph.find(null, RDF.type.asNode(), wordURI);
		{
			int num = 0;
			while (words.hasNext() && num<10) {
				Triple word = (Triple) words.next();
				Node lexicalFormNode = first(query.objectsFor(word.getSubject(), lexicalForm));
				String text = lexicalFormNode.getLiteralLexicalForm();
				if (!"heel".equals(text)) {
					continue;
				}
				print("","");
				print("word",word);
				
				print(" - lexicalForm",lexicalFormNode);
				print(" - partOfSpeech",first(query.objectsFor(word.getSubject(), partOfSpeech)));
				ExtendedIterator<Triple> senses = graph.find(null, wordRelation, word.getSubject());
				while (senses.hasNext()) {
					Triple relation = (Triple) senses.next();
					Node sense = relation.getSubject();
					//print(" - sense",sense);
					Node type = first(query.objectsFor(sense, RDF.type.asNode()));
					print(" - sense - type",type);
					print(" - sense - type - out",Lists.newArrayList(query.objectsFor(type, null)));
					print(" - sense - type - out",Lists.newArrayList(query.subjectsFor(type, null)));
					
					print(" - sense",first(query.objectsFor(sense, RDFS.label.asNode())));
					//print(" - sense - out",Lists.newArrayList(query.objectsFor(sense, null)));
					print(" - sense - out",Lists.newArrayList(graph.find(sense, null, null)));
					ExtendedIterator<Node> synSets = query.subjectsFor(CONTAINS_SENSE, sense);
					while (synSets.hasNext()) {
						Node synset = synSets.next();
						print(" - - synset - out",Lists.newArrayList(query.objectsFor(synset, null)));
						print(" - - synset - glossary",first(query.objectsFor(synset, GLOSSARY)));
						
					}
					//print(" - sense - in",Lists.newArrayList());
				}
				num++;
			}
		}

	}

	protected void print(Object object) {
		System.out.println(object);
	}

	protected void print(String string, Object object) {
		System.out.println(string+": "+object);
	}

}