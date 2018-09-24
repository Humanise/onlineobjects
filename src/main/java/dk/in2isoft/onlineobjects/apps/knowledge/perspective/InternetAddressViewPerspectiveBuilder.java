package dk.in2isoft.onlineobjects.apps.knowledge.perspective;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.google.common.collect.Lists;

import dk.in2isoft.commons.lang.Code;
import dk.in2isoft.commons.lang.HTMLWriter;
import dk.in2isoft.commons.lang.StringSearcher;
import dk.in2isoft.commons.lang.StringSearcher.Result;
import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.commons.xml.DOM;
import dk.in2isoft.commons.xml.DecoratedDocument;
import dk.in2isoft.commons.xml.DocumentCleaner;
import dk.in2isoft.commons.xml.DocumentToText;
import dk.in2isoft.in2igui.data.ItemData;
import dk.in2isoft.onlineobjects.apps.knowledge.KnowledgeModelService;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.UserSession;
import dk.in2isoft.onlineobjects.core.exceptions.ExplodingClusterFuckException;
import dk.in2isoft.onlineobjects.core.exceptions.IllegalRequestException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.Hypothesis;
import dk.in2isoft.onlineobjects.model.InternetAddress;
import dk.in2isoft.onlineobjects.model.LexicalCategory;
import dk.in2isoft.onlineobjects.model.Person;
import dk.in2isoft.onlineobjects.model.Property;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.Statement;
import dk.in2isoft.onlineobjects.model.Word;
import dk.in2isoft.onlineobjects.modules.information.ContentExtractor;
import dk.in2isoft.onlineobjects.modules.information.SimilarityQuery;
import dk.in2isoft.onlineobjects.modules.information.SimilarityQuery.Similarity;
import dk.in2isoft.onlineobjects.modules.information.SimpleContentExtractor;
import dk.in2isoft.onlineobjects.modules.language.TextDocumentAnalytics;
import dk.in2isoft.onlineobjects.modules.language.TextDocumentAnalyzer;
import dk.in2isoft.onlineobjects.modules.language.WordCategoryPerspectiveQuery;
import dk.in2isoft.onlineobjects.modules.language.WordListPerspective;
import dk.in2isoft.onlineobjects.services.LanguageService;
import dk.in2isoft.onlineobjects.services.SemanticService;
import dk.in2isoft.onlineobjects.ui.data.SimilarityPerspective;
import dk.in2isoft.onlineobjects.ui.data.SimpleEntityPerspective;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.ParentNode;
import nu.xom.XPathContext;
import opennlp.tools.util.Span;

public class InternetAddressViewPerspectiveBuilder {

	private static final Logger log = LogManager.getLogger(InternetAddressViewPerspectiveBuilder.class);

	private ModelService modelService;
	private LanguageService languageService;
	private SemanticService semanticService;
	private Map<String,ContentExtractor> contentExtractors;
	private KnowledgeModelService readerModelService;
	private TextDocumentAnalyzer textDocumentAnalyzer;

	public InternetAddressViewPerspective build(Long id, Settings settings, UserSession session) throws ModelException, IllegalRequestException, SecurityException, ExplodingClusterFuckException {
		StopWatch watch = new StopWatch();
		watch.start();
		watch.split();
		InternetAddress address = modelService.get(InternetAddress.class, id, session);
		if (address == null) {
			throw new IllegalRequestException("Not found");
		}
		trace("Load", watch);

		TextDocumentAnalytics analytics = textDocumentAnalyzer.analyze(address, session);
		trace("Get analytics", watch);
		Document xom = DOM.parseXOM(analytics.getXml());
		trace("Parse dom", watch);
		//HTMLDocument document = internetAddressService.getHTMLDocument(address, session);
		
		ArticleData data = buildData(address, session);
		trace("Build data", watch);

		InternetAddressViewPerspective article = new InternetAddressViewPerspective();

		article.setTitle(address.getName());
		article.setUrl(address.getAddress());
		article.setUrlText(Strings.simplifyURL(address.getAddress()));
		article.setAuthors(getAuthors(address, session));

		readerModelService.categorize(address, article, session);
		trace("Categorize", watch);

		loadStatements(address, article, session);
		trace("Load statements", watch);

		article.setHeader(buildHeader(address));
		article.setInfo(buildInfo(data, session));
		article.setId(address.getId());

		trace("Build info", watch);
		if (xom != null) {
			buildRendering(xom, data, article, settings, watch, session);
		}

		trace("Total", watch);
		return article;
	}

	private List<ItemData> getAuthors(Entity address, Privileged session) {
		Query<Person> query = Query.of(Person.class).from(address,Relation.KIND_COMMON_AUTHOR).as(session);
		List<Person> people = modelService.list(query);
		List<ItemData> authors = people.stream().map((Person p) -> {
			ItemData option = new ItemData();
			option.setId(p.getId());
			option.setText(p.getFullName());
			return option;
		}).collect(Collectors.toList());
		return authors;
	}

	private void loadStatements(InternetAddress address, InternetAddressViewPerspective article, Privileged session) throws ModelException {
		List<Statement> statements = modelService.getChildren(address, Relation.KIND_STRUCTURE_CONTAINS, Statement.class, session);
		List<StatementPerspective> quoteList = Lists.newArrayList();
		for (Statement statement : statements) {
			StatementPerspective statementPerspective = new StatementPerspective();
			statementPerspective.setText(statement.getText());
			statementPerspective.setId(statement.getId());
			statementPerspective.setAuthors(getAuthors(statement, session));
			quoteList.add(statementPerspective);
		}
		article.setQuotes(quoteList);
		
		List<Hypothesis> hypotheses = modelService.getChildren(address, Relation.KIND_STRUCTURE_CONTAINS, Hypothesis.class, session);
		List<StatementPerspective> perpectives = hypotheses.stream().map((Hypothesis hypothesis) -> {
			StatementPerspective perspective = new StatementPerspective();
			perspective.setText(hypothesis.getText());
			perspective.setId(hypothesis.getId());
			perspective.setAuthors(getAuthors(hypothesis, session));
			return perspective;
		}).collect(Collectors.toList());
		article.setHypotheses(perpectives);
	}
	
	private ArticleData buildData(InternetAddress address, UserSession session) throws ModelException {
		ArticleData data = new ArticleData();
		data.address = address;
		data.keywords = modelService.getChildren(address, Word.class, session);
		return data;
	}

	private String buildHeader(InternetAddress address) {
		HTMLWriter writer = new HTMLWriter();
		writer.startH1().text(address.getName()).endH1();
		writer.startP().startA().withClass("js_reader_action").withDataMap("action","openUrl","url",address.getAddress()).withTitle(address.getAddress()).withHref(address.getAddress()).text(Strings.simplifyURL(address.getAddress())).endA().endP();
		return writer.toString();
	}

	private String buildInfo(ArticleData data, Privileged session) throws ModelException {
		HTMLWriter writer = new HTMLWriter();
		writer.startH2().withClass("reader_meta_header").text("Tags").endH2();
		writer.startP().withClass("reader_meta_tags");

		List<String> tags = data.address.getPropertyValues(Property.KEY_COMMON_TAG);
		if (!tags.isEmpty()) {
			for (String string : tags) {
				writer.startVoidA().withClass("reader_meta_tags_item is-tag").withData(string).text(string).endA().text(" ");
			}
		}

		for (Word word : data.keywords) {
			writer.startVoidA().withData(word.getId()).withClass("reader_meta_tags_item is-word");
			writer.text(word.getText()).endA().text(" ");
		}

		writer.startA().withClass("reader_meta_tags_item is-add").text("Add word").endA();
		writer.endP();
		return writer.toString();
	}

	private void buildRendering(Document xom, ArticleData data, InternetAddressViewPerspective perspective, Settings settings, StopWatch watch, Privileged session) throws ModelException,
			ExplodingClusterFuckException {

		{
			if (xom==null) {
				log.warn("No XOM document");
				return;
			}
			ContentExtractor extractor = contentExtractors.get(settings.getExtractionAlgorithm());
			if (extractor==null) {
				log.debug("Unknown extrator: " + settings.getExtractionAlgorithm());
				extractor = new SimpleContentExtractor();
			}
			Document extracted = extractor.extract(xom);
			trace("Extracted", watch);

			DocumentToText doc2txt = new DocumentToText();
			String text = doc2txt.getText(extracted);
			perspective.setText(text);

			watch.split();
			trace("Text version", watch);

			DocumentCleaner cleaner = new DocumentCleaner();
			cleaner.setUrl(data.address.getAddress());
			cleaner.clean(extracted);

			trace("Cleaned", watch);
			
			Document annotated = annotate(perspective, data, settings, extracted, watch);

			trace("Annotated", watch);

			HTMLWriter formatted = new HTMLWriter();
			formatted.html(DOM.getBodyXML(annotated));
			trace("Get body", watch);

			
			
			renderSimilar(data, session, perspective, watch);
			
			perspective.setFormatted(formatted.toString());

		}
	}
	
	private void trace(String msg, StopWatch watch) {
		long prev = watch.getSplitTime();
		watch.split();
		log.trace(msg + ": " + watch.getSplitTime() + " (" + (watch.getSplitTime() - prev) + ")");
		
	}

	private void renderSimilar(ArticleData data, Privileged session, InternetAddressViewPerspective perspetive, StopWatch watch) throws ModelException {
		List<Similarity> list = modelService.list(new SimilarityQuery().withId(data.address.getId()));
		trace("Similarity query", watch);

		List<Long> ids = list.stream().map(e -> e.getId()).collect(Collectors.toList());
		List<InternetAddress> addresses = modelService.list(Query.after(InternetAddress.class).as(session).withIds(ids));
		trace("List similar", watch);
		
		Function<Long,InternetAddress> find = id -> {
			for (InternetAddress internetAddress : addresses) {
				if (internetAddress.getId()==id) {
					return internetAddress;
				}
			}
			return null;
		};
		
		List<SimilarityPerspective> similarities = new ArrayList<>();
		for (Similarity similarity : list) {
			SimilarityPerspective similarityPerspective = new SimilarityPerspective();
			similarityPerspective.setSimilarity(similarity.getSimilarity().floatValue());
			InternetAddress address = find.apply(similarity.getId());
			similarityPerspective.setEntity(SimpleEntityPerspective.create(address));
			similarities.add(similarityPerspective);
		}
		perspetive.setSimilar(similarities);
	}

	private Document annotate(InternetAddressViewPerspective article, ArticleData data, Settings settings, Document xomDocument, StopWatch watch) throws ModelException, ExplodingClusterFuckException {
		
		List<StatementPerspective> statements = article.getQuotes();

		DecoratedDocument decorated = new DecoratedDocument(xomDocument);
		String text = decorated.getText();
		String textLowercased = text.toLowerCase();
		trace("Decorate setup", watch);

		Locale locale = languageService.getLocale(text);
		trace("Locale", watch);
		if (settings.isHighlight() && locale!=null) {
			if (locale.getLanguage().equals("da") || locale.getLanguage().equals("en")) {
				annotatePeople(watch, decorated, text, locale);
				trace("Annotate people", watch);
			}
		}
		StringSearcher searcher = new StringSearcher();
		for (StatementPerspective statement : statements) {
			List<Result> found = searcher.search(statement.getText(), text);
			statement.setFirstPosition(text.length());
			for (Result result : found) {
				Map<String, Object> info = new HashMap<>();
				info.put("id", statement.getId());
				info.put("type", Statement.class.getSimpleName());
				info.put("description", "Statement: " + StringUtils.abbreviate(statement.getText(), 30));
				
				Map<String, Object> attributes = new HashMap<>();
				attributes.put("data-id", statement.getId());
				attributes.put("class", settings.getCssNamespace() + "statement js_reader_item");
				attributes.put("data-info", Strings.toJSON(info));
				decorated.decorate(result.getFrom(), result.getTo(), "mark", attributes);
				
				statement.setFirstPosition(Math.min(result.getFrom(), statement.getFirstPosition()));
			}
			if (!found.isEmpty()) {
				statement.setFound(true);
			}
		}

		trace("Decorate statements", watch);
		for (StatementPerspective hypothesis : article.getHypotheses()) {
			List<Result> found = searcher.search(hypothesis.getText(), text);
			hypothesis.setFirstPosition(text.length());
			for (Result result : found) {
				Map<String, Object> info = new HashMap<>();
				info.put("id", hypothesis.getId());
				info.put("type", Hypothesis.class.getSimpleName());
				info.put("description", "Hypothesis: " + StringUtils.abbreviate(hypothesis.getText(), 30));
				
				Map<String, Object> attributes = new HashMap<>();
				attributes.put("data-id", hypothesis.getId());
				attributes.put("class", settings.getCssNamespace() + "hypothesis js_reader_item");
				attributes.put("data-info", Strings.toJSON(info));
				decorated.decorate(result.getFrom(), result.getTo(), "mark", attributes);
				
				hypothesis.setFirstPosition(Math.min(result.getFrom(), hypothesis.getFirstPosition()));
			}
			if (!found.isEmpty()) {
				hypothesis.setFound(true);
			}
		}
		trace("Decorate hypothesis", watch);

		for (Word keyword : data.keywords) {
			List<Result> found = searcher.search(keyword.getText().toLowerCase(), textLowercased);
			for (Result result : found) {
				Map<String, Object> info = new HashMap<>();
				info.put("id", keyword.getId());
				info.put("type", Word.class.getSimpleName());
				info.put("description", "Word: " + StringUtils.abbreviate(keyword.getText(), 30));
				
				Map<String, Object> attributes = new HashMap<>();
				attributes.put("data-id", keyword.getId());
				attributes.put("class", settings.getCssNamespace() + "word js_reader_item");
				attributes.put("data-info", Strings.toJSON(info));
				decorated.decorate(result.getFrom(), result.getTo(), "span", attributes);
			}
		}

		trace("Decorate keywords", watch);
		
		Collections.sort(statements,(a,b) -> {
			return a.getFirstPosition() - b.getFirstPosition();
		});
		decorated.build();
		Document document = decorated.getDocument();
		annotateLinks(document, settings);
		trace("Decorate links", watch);
		return document;
	}

	private void annotateLinks(Document document, Settings settings) {
		XPathContext context = new XPathContext();
		String namespaceURI = document.getRootElement().getNamespaceURI();
		context.addNamespace("html", namespaceURI);
		Nodes links = document.query("//html:a", context);
		for (int i = 0; i < links.size(); i++) {
			Element node = (Element) links.get(i);
			String href = node.getAttributeValue("href");
			if (href!=null && href.startsWith("http")) {
				Map<String, Object> info = new HashMap<>();
				info.put("type", "Link");
				info.put("url", href);
				info.put("description", "Link: " + Strings.simplifyURL(href));
				node.addAttribute(new Attribute("class", "js_reader_item"));
				node.addAttribute(new Attribute("data-info", Strings.toJSON(info)));
			}
		}
		Nodes images = document.query("//html:img", context);
		List<Element> imgs = Lists.newArrayList();
		for (int i = 0; i < images.size(); i++) {
			Element node = (Element) images.get(i);
			imgs.add(node);
		}
		for (Element node : imgs) {
			String width = node.getAttributeValue("width");
			String height = node.getAttributeValue("height");
			if (Strings.isInteger(width) && Strings.isInteger(height)) {
				float ratio = Float.parseFloat(height) / Float.parseFloat(width);
				Element wrapper = new Element("span", namespaceURI);
				wrapper.addAttribute(new Attribute("style", "max-width: " + width + "px;"));
				wrapper.addAttribute(new Attribute("class", settings.getCssNamespace() + "picture"));
				ParentNode parent = node.getParent();
				parent.insertChild(wrapper, parent.indexOf(node));
				
				Element body = new Element("span", namespaceURI);
				body.addAttribute(new Attribute("class", settings.getCssNamespace() + "picture_body"));
				body.addAttribute(new Attribute("style", "padding-bottom: "+ (ratio * 100) + "%;"));
				
				body.appendChild(parent.removeChild(node));
				wrapper.appendChild(body);

				node.addAttribute(new Attribute("class", settings.getCssNamespace() + "picture_image"));
			} else {
				node.addAttribute(new Attribute("class", settings.getCssNamespace() + "image"));
			}
		}
	}

	private void annotatePeople(StopWatch watch, DecoratedDocument decorated, String text, Locale locale) throws ExplodingClusterFuckException, ModelException {
		List<Span> nounSpans = Lists.newArrayList();

		List<String> nouns = Lists.newArrayList();

		String[] lines = text.split("\\n");
		int pos = 0;
		for (String line : lines) {
			Span[] sentences = semanticService.getSentencePositions(line, locale);
			watch.split();
			for (Span sentence : sentences) {
				// decorated.decorate(sentence.getStart(), sentence.getEnd(),
				// "mark", getClassMap("sentence") );

				String sentenceText = sentence.getCoveredText(line).toString();
				Span[] sentenceTokenPositions = semanticService.getTokenSpans(sentenceText, locale);
				String[] sentenceTokens = semanticService.spansToStrings(sentenceTokenPositions, sentenceText);
				String[] partOfSpeach = semanticService.getPartOfSpeach(sentenceTokens, locale);
				for (int i = 0; i < sentenceTokenPositions.length; i++) {
					String token = sentenceTokens[i];
					if (!Character.isUpperCase(token.charAt(0))) {
						continue;
					}
					if (!partOfSpeach[i].startsWith("N")) {
						continue;
					}
					boolean prev = i > 0 && partOfSpeach[i - 1].startsWith("N") && Character.isUpperCase(sentenceTokens[i - 1].charAt(0));
					boolean next = i < sentenceTokenPositions.length - 1 && partOfSpeach[i + 1].startsWith("N") && Character.isUpperCase(sentenceTokens[i + 1].charAt(0));
					if (!(prev || next)) {
						continue;
					}
					nouns.add(token);
					Span spn = new Span(sentenceTokenPositions[i].getStart() + sentence.getStart() + pos, sentenceTokenPositions[i].getEnd() + sentence.getStart() + pos, token);
					nounSpans.add(spn);
				}
			}
			pos += 1;
			pos += line.length();
		}

		trace("Part of speech", watch);

		List<WordListPerspective> names = findNames(nouns);

		trace("Find names", watch);

		for (Span span : nounSpans) {
			String cls = "noun";
			if (isPerson(span.getType(), names)) {
				cls = "person";
			}
			decorated.decorate(span.getStart(), span.getEnd(), "mark", getClassMap(cls));
		}
	}

	private List<WordListPerspective> findNames(List<String> words) throws ModelException, ExplodingClusterFuckException {
		if (Code.isEmpty(words)) {

		}
		WordCategoryPerspectiveQuery query = new WordCategoryPerspectiveQuery().withWords(words);
		query.withCategories(LexicalCategory.CODE_PROPRIUM_FIRST, LexicalCategory.CODE_PROPRIUM_MIDDLE, LexicalCategory.CODE_PROPRIUM_LAST);
		return modelService.search(query).getList();
	}

	private boolean isPerson(String token, List<WordListPerspective> words) {
		for (WordListPerspective word : words) {
			if (word.getText().equalsIgnoreCase(token)) {
				return true;
			}
		}
		return false;
	}

	private Map<String, Object> getClassMap(Object cls) {
		Map<String, Object> attributes = new HashMap<>();
		attributes.put("class", cls);
		return attributes;
	}
	
	private class ArticleData {
		InternetAddress address;
		List<Word> keywords;
	}
	
	public static class Settings {
		private boolean highlight;
		private String extractionAlgorithm;
		private String cssNamespace;
		
		public Settings() {
			cssNamespace = "";
		}

		public boolean isHighlight() {
			return highlight;
		}

		public void setHighlight(boolean highlight) {
			this.highlight = highlight;
		}

		public String getExtractionAlgorithm() {
			return extractionAlgorithm;
		}

		public void setExtractionAlgorithm(String extractionAlgorithm) {
			this.extractionAlgorithm = extractionAlgorithm;
		}

		public String getCssNamespace() {
			return cssNamespace;
		}

		public void setCssNamespace(String cssNamespace) {
			this.cssNamespace = cssNamespace;
		}
	}

	// Wiring...

	public void setLanguageService(LanguageService languageService) {
		this.languageService = languageService;
	}

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
	
	public void setReaderModelService(KnowledgeModelService readerModelService) {
		this.readerModelService = readerModelService;
	}

	public void setSemanticService(SemanticService semanticService) {
		this.semanticService = semanticService;
	}
		
	public void setContentExtractors(Map<String,ContentExtractor> contentExtractors) {
		this.contentExtractors = contentExtractors;
	}
	
	public Map<String,ContentExtractor> getContentExtractors() {
		return contentExtractors;
	}
	
	public void setTextDocumentAnalyzer(TextDocumentAnalyzer analyzer) {
		this.textDocumentAnalyzer = analyzer;
	}
}
