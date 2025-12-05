package dk.in2isoft.onlineobjects.apps.knowledge.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.InitializingBean;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import dk.in2isoft.commons.jsf.AbstractView;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.Pair;
import dk.in2isoft.onlineobjects.core.exceptions.ExplodingClusterFuckException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.model.InternetAddress;
import dk.in2isoft.onlineobjects.model.Kind;
import dk.in2isoft.onlineobjects.model.LexicalCategory;
import dk.in2isoft.onlineobjects.model.Property;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.modules.language.TextDocumentAnalytics;
import dk.in2isoft.onlineobjects.modules.language.TextDocumentAnalyzer;
import dk.in2isoft.onlineobjects.modules.language.WordCategoryPerspectiveQuery;
import dk.in2isoft.onlineobjects.modules.language.WordListPerspective;
import dk.in2isoft.onlineobjects.ui.Request;

public class KnowledgeAnalyzeView extends AbstractView implements InitializingBean {

	private ModelService modelService;
	private TextDocumentAnalyzer textDocumentAnalyzer;

	private InternetAddress internetAddress;

	private List<Pair<String,Object>> properties;
	private TextDocumentAnalytics analytics;
	private Map<String, Collection<String>> wordsByPartOfSpeech;
	private List<Pair<InternetAddress, Double>> similar;

	private StringBuilder message;
	private List<String> names;

	public KnowledgeAnalyzeView() {
		properties = Lists.newArrayList();
		message = new StringBuilder();
	}

	@Override
	protected void before(Request request) throws Exception {
		Long id = request.getId();
		internetAddress = modelService.getRequired(InternetAddress.class, id, request);

		analytics = textDocumentAnalyzer.analyze(internetAddress, request);

		properties.add(Pair.of("Entity name", internetAddress.getName()));
		properties.add(Pair.of("Entity address", internetAddress.getAddress()));
		for (Property property : internetAddress.getProperties()) {
			properties.add(Pair.of(property.getKey(), property.getValue()));
		}
		properties.add(Pair.of("Document title", analytics.getDocumentTitle()));
		properties.add(Pair.of("Locale - guessed", analytics.getGuessedLocale()));
		properties.add(Pair.of("Locale - used", analytics.getUsedLocale()));
		Multimap<String, String> textByPOS = HashMultimap.create();
		analytics.getSignificantWords().forEach((part) ->
			textByPOS.put(part.getPartOfSpeech(), part.getText().toLowerCase())
		);
		wordsByPartOfSpeech = textByPOS.asMap();

		compare(request);
		findPeople(request);
	}

	private void findPeople(Operator operator) throws ModelException, ExplodingClusterFuckException {
		List<List<String>> nameCandidates = analytics.getNameCandidates();
		Set<String> uniques = Sets.newHashSet();
		for (List<String> list : nameCandidates) {
			uniques.addAll(list);
		}
		List<WordListPerspective> perspectives = findNames(uniques, operator);
		List<String> names = perspectives.stream().map((a) -> a.getText()).collect(Collectors.toList());
		this.names = new ArrayList<String>();
		for (List<String> words : nameCandidates) {
			StringBuilder sb = new StringBuilder();
			for (String word : words) {
				if (names.contains(word)) {
					if (sb.length()>0) sb.append(" ");
					sb.append(word);
				}
			}
			if (sb.length()>0) {
				this.names.add(sb.toString());
			}
		}

	}

	public List<String> getNames() {
		return names;
	}

	private void compare(Operator operator) throws ModelException {

		List<Pair<InternetAddress,Double>> similarities = new ArrayList<>();

		List<Relation> relationsFrom = modelService.getRelationsFrom(internetAddress, InternetAddress.class, Kind.similarity.toString(), operator);
		List<Relation> relationsTo = modelService.getRelationsTo(internetAddress, InternetAddress.class, Kind.similarity.toString(), operator);

		for (Relation relation : relationsFrom) {
			if (relation.getTo() instanceof InternetAddress && relation.getStrength()!=null) {
				similarities.add(Pair.of((InternetAddress)relation.getTo(), relation.getStrength()));
			}
		}
		for (Relation relation : relationsTo) {
			if (relation.getFrom() instanceof InternetAddress && relation.getStrength()!=null) {
				similarities.add(Pair.of((InternetAddress)relation.getFrom(), relation.getStrength()));
			}
		}
		this.similar = similarities.stream().filter((x) -> x.getValue()>-1).sorted((a,b) -> b.getValue().compareTo(a.getValue())).collect(Collectors.toList());

	}

	private List<WordListPerspective> findNames(Collection<String> words, Operator operator) throws ModelException, ExplodingClusterFuckException {
		WordCategoryPerspectiveQuery query = new WordCategoryPerspectiveQuery().withWords(words);
		query.withCategories(LexicalCategory.CODE_PROPRIUM_FIRST, LexicalCategory.CODE_PROPRIUM_MIDDLE, LexicalCategory.CODE_PROPRIUM_LAST);
		return modelService.search(query, operator).getList();
	}


	public TextDocumentAnalytics getAnalytics() {
		return analytics;
	}

	public Map<String, Collection<String>> getWordsByPartOfSpeech() {
		return wordsByPartOfSpeech;
	}

	public List<Pair<String, Object>> getProperties() {
		return properties;
	}

	public List<Pair<InternetAddress, Double>> getSimilar() {
		return similar;
	}

	public String getMessage() {
		return message.toString();
	}

	// Wiring...

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	public void setTextDocumentAnalyzer(TextDocumentAnalyzer textDocumentAnalyzer) {
		this.textDocumentAnalyzer = textDocumentAnalyzer;
	}
}
