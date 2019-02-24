package dk.in2isoft.onlineobjects.modules.information;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sun.syndication.feed.rss.Item;

import dk.in2isoft.commons.http.URLUtil;
import dk.in2isoft.commons.lang.Counter;
import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.commons.parsing.HTMLDocument;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.NetworkException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.InternetAddress;
import dk.in2isoft.onlineobjects.model.Kind;
import dk.in2isoft.onlineobjects.model.Property;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.model.Word;
import dk.in2isoft.onlineobjects.modules.information.MissingSimilarityQuery.SimilarityResult;
import dk.in2isoft.onlineobjects.modules.language.TextDocumentAnalytics;
import dk.in2isoft.onlineobjects.modules.language.TextDocumentAnalyzer;
import dk.in2isoft.onlineobjects.modules.language.WordListPerspective;
import dk.in2isoft.onlineobjects.modules.language.WordListPerspectiveQuery;
import dk.in2isoft.onlineobjects.modules.networking.HTMLService;
import dk.in2isoft.onlineobjects.modules.scheduling.JobStatus;
import dk.in2isoft.onlineobjects.modules.surveillance.SurveillanceService;
import dk.in2isoft.onlineobjects.services.FeedService;
import dk.in2isoft.onlineobjects.services.LanguageService;
import dk.in2isoft.onlineobjects.services.SemanticService;

public class InformationService {

	//private static final Logger log = LogManager.getLogger(InformationService.class);

	private FeedService feedService;
	private SemanticService semanticService;
	private LanguageService languageService;
	private ModelService modelService;
	private SecurityService securityService;
	private SurveillanceService surveillanceService;
	private HTMLService htmlService;
	private TextDocumentAnalyzer textDocumentAnalyzer;

	private Set<String> supportedLanguages = Sets.newHashSet("da", "en");

	public void importInformation(String feed, JobStatus status) {
		Operator admin = modelService.newAdminOperator();
		try {
			surveillanceService.logInfo("Checking feed", feed);
			List<Item> items = feedService.getFeedItems(feed);
			if (items == null) {
				surveillanceService.logInfo("No items in feed", feed);
				status.error("Unable to parse feed: " + feed);
				return;
			}
			for (Item item : items) {
				String link = item.getLink();
				if (Strings.isBlank(link)) {
					status.warn("Feed item has no link: " + item.getTitle());
					continue;
				}
				String abreviated = StringUtils.abbreviateMiddle(link, "...", 100);

				Long num = modelService
						.count(Query.after(InternetAddress.class).withField(InternetAddress.FIELD_ADDRESS, link), admin);

				if (num > 0) {
					status.log("Internet address already exists: " + abreviated);
					continue;
				}
				status.log("Fetching: " + abreviated);
				HTMLDocument doc = htmlService.getDocumentSilently(link);
				if (doc == null) {
					status.log("Unable to get HTML document: " + abreviated);
					continue;
				}
				String contents = doc.getExtractedText();

				InternetAddress internetAddress = new InternetAddress();
				internetAddress.setAddress(link);
				internetAddress.setName(doc.getTitle());
				internetAddress.addProperty(Property.KEY_INTERNETADDRESS_CONTENT,
						StringUtils.abbreviate(contents, 3990));
				modelService.create(internetAddress, admin);

				if (Strings.isBlank(contents)) {
					status.warn("No content: " + link);
				} else {
					Locale locale = languageService.getLocale(contents);
					int wordsCreated = 0;
					if (locale == null) {
						status.warn("Unable to detect language: "
								+ StringUtils.abbreviateMiddle(doc.getTitle(), "...", 50));
					} else {
						if ("no".equals(locale.getLanguage())) {
							locale = new Locale("da");
							status.warn("Using danish instead of norwegian");
						}
						if (!supportedLanguages.contains(locale.getLanguage())) {
							status.warn("Unsupported language: " + locale);
						} else {
							String[] allWords = semanticService.getNaturalWords(contents, locale);
							List<String> uniqueWords = Lists
									.newArrayList(semanticService.getUniqueWordsLowercased(allWords));

							WordListPerspectiveQuery perspectiveQuery = new WordListPerspectiveQuery()
									.withWords(uniqueWords).orderByText();
							List<WordListPerspective> list = modelService.list(perspectiveQuery, admin);
							Set<String> found = Sets.newHashSet();
							for (WordListPerspective perspective : list) {
								found.add(perspective.getText().toLowerCase());
							}
							Counter<String> languages = languageService.countLanguages(list);
							String topLanguage = languages.getTop();
							// Locale locale =
							// languageService.getLocaleForCode(topLanguage);
							for (String wordStr : uniqueWords) {
								if (!found.contains(wordStr)) {
									Word word = new Word();
									word.setText(wordStr);
									word.addProperty(Property.KEY_DATA_VALIDATED, "false");
									word.addProperty(Property.KEY_WORD_SUGGESTION_LANGUAGE, topLanguage);

									modelService.create(word, admin);
									securityService.makePublicVisible(word, admin);
									status.log("New word found: " + word);

									modelService.createRelation(internetAddress, word, Relation.KIND_COMMON_SOURCE,
											admin);
									wordsCreated++;
								}
							}
						}
					}
					surveillanceService.logInfo("Imported webpage", "Title: "
							+ StringUtils.abbreviateMiddle(doc.getTitle(), "...", 50) + " - words: " + wordsCreated);
				}
				// break;
			}
			admin.commit();
		} catch (NetworkException e) {
			status.error("Unable to fetch feed: " + feed, e);
			admin.rollBack();
		} catch (ModelException e) {
			status.error("Failed to persist something", e);
			admin.rollBack();
		} catch (SecurityException e) {
			status.error("Permissions problem", e);
			admin.rollBack();
		}
	}

	public InternetAddress addInternetAddress(String url, Operator privileged) throws ModelException, SecurityException {
		if (!URLUtil.isValidHttpUrl(url)) {
			throw new IllegalArgumentException("URL not valid: " + url);
		}
		InternetAddress internetAddress = new InternetAddress();
		internetAddress.setAddress(url);
		HTMLDocument doc = htmlService.getDocumentSilently(url);
		if (doc != null) {
			internetAddress.setName(doc.getTitle());
		} else {
			internetAddress.setName(Strings.simplifyURL(url));
		}
		modelService.create(internetAddress, privileged);
		return internetAddress;
	}

	public void calculateNextMissingSimilary(JobStatus status) {
		Operator admin = modelService.newAdminOperator();
		try {
			@NonNull
			MissingSimilarityQuery simQuery = new MissingSimilarityQuery();
			List<SimilarityResult> results = modelService.list(simQuery, admin);
			int index = 0;
			int total = results.size();
			for (SimilarityResult pair : results) {
				if (status.isInterrupted()) {
					break;
				}
				User privileged = modelService.get(User.class, pair.getUserId(), admin);
				Operator userOperator = admin.as(privileged);
				status.log("Found missing similarity (user=" + privileged.getUsername() + ")");
				status.setProgress(index, total);
				status.log("Loading entities");
				InternetAddress a = modelService.get(InternetAddress.class, pair.getFirstId(), userOperator);
				InternetAddress b = modelService.get(InternetAddress.class, pair.getSecondId(), userOperator);

				status.log("Building analytics: "+a.getAddress());
				TextDocumentAnalytics analyticsA = textDocumentAnalyzer.analyze(a, userOperator);
				status.log("Building analytics: "+b.getAddress());
				TextDocumentAnalytics analyticsB = textDocumentAnalyzer.analyze(b, userOperator);
				double similarity;
				if (analyticsA!=null && analyticsB!=null) {
					List<String> tokensA = getTokens(analyticsA);
					List<String> tokensB = getTokens(analyticsB);

					status.log("Comparing: " + tokensA.size() + "/" + tokensB.size());

					similarity = semanticService.compare(tokensA, tokensB);					
				} else {
					status.log("One of the two where null");
					similarity = 0;
				}

				status.log("Saving similarity");
				createSimilarity(a, b, similarity, userOperator);

				status.log(a.getName() + " vs " + b.getName() + " = " + similarity);
				index++;
				admin.commit();
			}
		} catch (EndUserException e) {
			status.error("Error while calculating similarity", e);
			admin.rollBack();
		}
	}

	private void createSimilarity(InternetAddress a, InternetAddress b, double similarity, Operator privileged)
			throws ModelException, SecurityException {
		Optional<Relation> a2b = modelService.getRelation(a, b, Kind.similarity.toString(), privileged);
		Optional<Relation> b2a = modelService.getRelation(b, a, Kind.similarity.toString(), privileged);

		// TODO Make sure there is exactly 1 relation
		if (!a2b.isPresent() && !b2a.isPresent()) {
			Relation relation = new Relation(a, b);
			relation.setStrength(similarity);
			relation.setKind(Kind.similarity);
			modelService.create(relation, privileged);
		}

	}

	private List<String> getTokens(TextDocumentAnalytics candidate) {
		if (candidate==null || candidate.getSignificantWords() == null) {
			return new ArrayList<>();
		}
		return candidate.getSignificantWords().stream()
				.map((part) -> part.getPartOfSpeech() + "_" + part.getText().toLowerCase())
				.collect(Collectors.toList());
	}

	// Wiring...

	public void setFeedService(FeedService feedService) {
		this.feedService = feedService;
	}

	public void setSemanticService(SemanticService semanticService) {
		this.semanticService = semanticService;
	}

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	public void setLanguageService(LanguageService languageService) {
		this.languageService = languageService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public void setSurveillanceService(SurveillanceService surveillanceService) {
		this.surveillanceService = surveillanceService;
	}

	public void setHtmlService(HTMLService htmlService) {
		this.htmlService = htmlService;
	}

	public void setTextDocumentAnalyzer(TextDocumentAnalyzer textDocumentAnalyzer) {
		this.textDocumentAnalyzer = textDocumentAnalyzer;
	}
}
