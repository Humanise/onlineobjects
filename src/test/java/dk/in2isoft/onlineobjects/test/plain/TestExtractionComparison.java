package dk.in2isoft.onlineobjects.test.plain;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

import dk.in2isoft.commons.lang.Files;
import dk.in2isoft.commons.lang.HTMLWriter;
import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.commons.parsing.HTMLDocument;
import dk.in2isoft.commons.xml.DOM;
import dk.in2isoft.commons.xml.DocumentCleaner;
import dk.in2isoft.commons.xml.DocumentToText;
import dk.in2isoft.onlineobjects.modules.information.Boilerpipe;
import dk.in2isoft.onlineobjects.modules.information.ContentExtractor;
import dk.in2isoft.onlineobjects.modules.information.CruxExtractor;
import dk.in2isoft.onlineobjects.modules.information.ReadabilityExtractor;
import dk.in2isoft.onlineobjects.modules.information.RecognizingContentExtractor;
import dk.in2isoft.onlineobjects.modules.information.SimpleContentExtractor;
import dk.in2isoft.onlineobjects.modules.networking.HTMLService;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;
import info.debatty.java.stringsimilarity.NormalizedLevenshtein;
import nu.xom.Document;
import nu.xom.Serializer;

public class TestExtractionComparison extends AbstractSpringTestCase {
	
	@Autowired
	private HTMLService htmlService;

	private static final Logger log = LogManager.getLogger(TestExtractionComparison.class);
	
	@Test
	public void testArticleExtraction() throws Exception {
		
		List<Extractor> extractors = Lists.newArrayList();
		extractors.add(new Extractor("readability", new ReadabilityExtractor()));
		extractors.add(new Extractor("simple", new SimpleContentExtractor()));
		extractors.add(new Extractor("boilerpipe", new Boilerpipe()));
		extractors.add(new Extractor("crux", new CruxExtractor()));
		extractors.add(new Extractor("mercury", new MercuryExctractor()));
		extractors.add(new Extractor("none", new NonExctractor()));
		extractors.add(new Extractor("recognize", new RecognizingContentExtractor()));
		extractors.add(new Extractor("recognize.debug", RecognizingContentExtractor.debug()));
		
		File folder = new File(getResourcesDir(),"extraction");
		Assert.assertTrue(folder.isDirectory());
		DocumentCleaner cleaner = new DocumentCleaner();
		
		DocumentToText docToText = new DocumentToText();
		
		NormalizedLevenshtein l = new NormalizedLevenshtein();
		
		StopWatch watch = new StopWatch();
		
		Map<String,Double> comparisonCache = loadComparisonCache(folder);
		
		File[] dirs = folder.listFiles(file -> file.isDirectory());
		Arrays.sort(dirs);
		
		for (File dir : dirs) {
			//if (!dir.getName().startsWith("videnskab")) continue;
			File infoFile = findByExtension(dir, "json");
			if (infoFile==null) {
				log.warn("Info file not found: {}", infoFile);
				continue;
			}
			String baseName = infoFile.getName().split("\\.")[0];
			File original = findByExtension(dir, "original.html");
			String idealText = Files.readString(findByExtension(dir, "ideal.txt"));
			if (idealText == null || idealText.length() < 50) {
				log.error("No text for {}", dir.getName());
			}
			Info info = getInfo(infoFile);
			HTMLDocument doc = htmlService.getDocumentSilently(original, Strings.UTF8);
			Assert.assertNotNull(doc);
			Document document = doc.getXOMDocument();
			
			log.info("Checking: {}", dir.getName());
			Files.overwriteTextFile(document.toXML(), new File(dir, baseName+".xhtml"));
			{
				String str = Files.readString(original);
				Document xom = DOM.parseAnyXOM(str);
				Files.overwriteTextFile(xom.toXML(), new File(dir, baseName+".tagsoup.xhtml"));
				Document jsouped = DOM.parseWildHhtml(str);
				if (jsouped!=null) {
					Files.overwriteTextFile(jsouped.toXML(), new File(dir, baseName+".jsoup.xhtml"));
				} else {
					org.jsoup.nodes.Document jsoup = Jsoup.parse(str);
					Files.overwriteTextFile(jsoup.outerHtml(), new File(dir, baseName+".jsoup.raw.xhtml"));
				}
			}
			
			for (Extractor extractor : extractors) {

				watch.reset();
				watch.start();
				ContentExtractor contentExtractor = extractor.getExtractor();
				Document extracted;
				if (info!=null && contentExtractor instanceof MercuryExctractor) {
					File mercuryCache = new File(dir, baseName+".mercury.cache.html");
					if (mercuryCache.exists()) {
						extracted = DOM.parseXOM(Files.readString(mercuryCache));
					} else {
						extracted = ((MercuryExctractor) contentExtractor).exctract(info.url);
						if (extracted!=null) {
							Files.overwriteTextFile(extracted.toXML(), mercuryCache);
						}
					}
				} else {
					extracted = contentExtractor.extract(document);
				}
				watch.stop();
				long time = watch.getNanoTime();
				if (extracted==null) {
					log.warn("Null extraction: {}", extractor.name);
					continue;
				}
				cleaner.setAllowDataAttributes(extractor.name.equals("recognize.debug"));
				cleaner.clean(extracted);

				File out = new File(dir, baseName+"."+extractor.getName()+".html");
				
				if (extractor.name.contains("recognize")) {
					try (FileWriter w = new FileWriter(out)) {
						ByteArrayOutputStream bytes = new ByteArrayOutputStream();
						Serializer serializer = new Serializer(bytes, Strings.UTF8);
						serializer.setIndent(2);
						serializer.write(extracted);
						String string = bytes.toString();
						string = string.replaceAll("\\s+\\n", "\n");
						w.write(string);
					}
				} else {
					try (FileWriter w = new FileWriter(out)) {	
						w.append(extracted.toXML());					
					}
				}
				String text = docToText.getText(extracted);
				Files.overwriteTextFile(text, new File(dir, baseName+"."+extractor.getName()+".txt"));
				if (Strings.isBlank(text)) {
					log.warn("Blank: " + baseName + " - " + extractor.getName());
				}
				Lines lines = checkLines(idealText, text);
				extractor.lines(lines);
				double comparison = getComparison(idealText, text, comparisonCache, l);
				if (Double.isNaN(comparison)) {
					log.warn("Extracted:" + text);
				} else {
					extractor.addComparison(comparison);						
				}
				log.info(baseName + " - " + extractor.getName() + "- : " + comparison + " - " + (time / 1000000) +"ms");
				extractor.addTime(time);
				extractor.addResult(dir.getName(), info, lines);
			}
			//break;
		}

		for (Extractor extractor : extractors) {
			log.info(extractor.getStatus());
		}
		writeReport(extractors, folder);
		saveComparisonCache(comparisonCache, folder);
	}
	
	private double getComparison(String idealText, String actual, Map<String, Double> comparisonCache,
			NormalizedLevenshtein l) {
		String idealNormalized = normalize(idealText);
		String actualNormalized = normalize(actual);
		String key = idealNormalized.hashCode()+"-"+actualNormalized.hashCode();
		if (comparisonCache.containsKey(key)) {
			return comparisonCache.get(key);
		}
		double similarity = l.similarity(idealNormalized, actualNormalized);
		comparisonCache.put(key, similarity);
		return similarity; 
	}
	
	private void saveComparisonCache(Map<String, Double> cache, File folder) {
		try (FileWriter out = new FileWriter(new File(folder, "comparison.cache"))) {
			for (Entry<String, Double> entry : cache.entrySet()) {
				out.append(entry.getKey()).append("=").append(entry.getValue().toString()).append("\n");
			}
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	private Map<String, Double> loadComparisonCache(File folder) {
		Map<String, Double> cache = new HashMap<>();
		String cacheText = Files.readString(new File(folder, "comparison.cache"));
		if (cacheText !=null) {
			for (String line : cacheText.split("\\n")) {
				String[] parts = line.split("=");
				if (parts.length==2) {
					cache.put(parts[0], Double.valueOf(parts[1]));
				}
			}
		}
		return cache;
	}

	private void writeReport(List<Extractor> extractors, File dir) {
		TemplateEngine engine = new TemplateEngine();
		String template = new File(dir,"template.html").getAbsolutePath();
		File output = new File(dir,"report.html");
		FileTemplateResolver resolver = new FileTemplateResolver(); 
		resolver.setSuffix(".html");
		resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
		resolver.setTemplateMode(TemplateMode.HTML);
		engine.setTemplateResolver(resolver);
		Map<String,Object> variables = new HashMap<>();
        variables.put("extractors", extractors);
		IContext context = new Context(Locale.ENGLISH, variables);
		try (FileWriter writer = new FileWriter(output)) {
			engine.process(template, context, writer);
			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private Lines checkLines(String idealText, String text) {
		Lines lines = new Lines();
		List<String> idealLines = Arrays.asList(idealText.split("\\n{1,}"));
		List<String> actualLines = Arrays.asList(text.split("\\n{1,}"));
		for (String line : idealLines) {
			if (!actualLines.contains(line)) {
				lines.missing.add(line);
			}
		}
		for (String line : actualLines) {
			if (!idealLines.contains(line)) {
				lines.extra.add(line);
			}
		}
		return lines;
	}

	private File findByExtension(File dir, String ext) {
		File[] infos = dir.listFiles(file -> file.getName().endsWith("." + ext));
		return infos.length > 0 ? infos[0] : null;
	}
	
	private String normalize(String text) {
		return text.replaceAll("[\\s]+", " ").trim();
	}

	private Info getInfo(File file) throws IOException, FileNotFoundException {
		if (!file.exists()) return null;
		try (FileReader reader = new FileReader(file)) {
			String txt = IOUtils.toString(reader);
			return new Gson().fromJson(txt, Info.class);
		}
	}

	private class Extractor {
		private ContentExtractor extractor;
		private String name;
		private int count;
		private double total;
		private double min = Double.MAX_VALUE;
		private double max = Double.MIN_VALUE;
		private long time;
		
		private int missing;
		private int extra;
		private List<ReportTest> tests = new ArrayList<>();

		public Extractor(String name, ContentExtractor extractor) {
			super();
			this.name = name;
			this.extractor = extractor;
		}
		
		public String getStatus() {
			DecimalFormat df = new DecimalFormat("#,###,##0.00");
			return name+" | min : "+df.format(min*100)+"%, max : "+df.format(max*100)+"%, average : "+df.format(total/count*100)+"%, missing: " + this.missing + ", extra: " + this.extra + ", time: "+df.format(((double)this.time)/1000000.0)+"ms";
		}

		public String getName() {
			return name;
		}
		
		public void addTime(long time) {
			this.time += time;
		}
		
		public ContentExtractor getExtractor() {
			return extractor;
		}

		public void addComparison(double comparison) {
			min = Math.min(min, comparison);
			max = Math.max(max, comparison);
			count++;
			total+=comparison;
		}
		
		public void addResult(String name, Info info, Lines lines) {
			ReportTest test = new ReportTest();
			test.name = name;
			test.url = info.url;
			test.lines = lines;
			tests.add(test);
		}
		
		public List<ReportTest> getTests() {
			return tests;
		}
		
		public void lines(Lines lines) {
			this.missing += lines.missing.size();
			this.extra += lines.extra.size();
		}
		
		public int getMissing() {
			return missing;
		}
		
		public int getExtra() {
			return extra;
		}
		
		public double getMin() {
			return min;
		}
		
		public double getMax() {
			return max;
		}
		
		public double getAverage() {
			return total/count;
		}
	}
	
	private class ReportTest {
		String name;
		String url;
		Lines lines;
		
		public String getName() {
			return name;
		}
		
		public Lines getLines() {
			return lines;
		}
	}
	
	private class Info {
		String url;
	}
	
	private class MercuryExctractor implements ContentExtractor {
		
		public Document exctract(String url) {
			try {
				HttpClient client = HttpClients.custom().disableRedirectHandling().build();
				HttpContext context = new BasicHttpContext();

				// connect and receive 
				HttpGet get = new HttpGet("https://mercury.postlight.com/parser?url=https://politiken.dk/debat/art5592320/Krisen-har-%C3%B8get-afstanden-mellem-eliten-og-alle-de-andre"+url);
				get.addHeader("x-api-key", getProperty("mercury"));
				HttpResponse response = client.execute(get, context);
				if (response.getStatusLine().getStatusCode() != 200) {
					return null;
				}
				HttpEntity entity = response.getEntity();
				String encoding = StandardCharsets.UTF_8.name();
				String json = IOUtils.toString(entity.getContent(), encoding);
				
				MercuryResponse mr = new Gson().fromJson(json, MercuryResponse.class);
				if (mr != null && Strings.isNotBlank(mr.excerpt)) {
					HTMLWriter html = new HTMLWriter();
					html.startH1().text(mr.title).endH1();
					html.startP().text(mr.excerpt).endP();
					return DOM.parseAnyXOM(html.toString() + mr.content);
				}
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
			return null;

		}
		
		@Override
		public Document extract(Document document) {
			return (Document) document.copy();
		}
		
	}
	
	private class NonExctractor implements ContentExtractor {
		
		@Override
		public Document extract(Document document) {
			return (Document) document.copy();
		}
		
	}
	
	private class Lines {
		List<String> missing = new ArrayList<>();
		List<String> extra = new ArrayList<>();
		public List<String> getMissing() {
			return missing;
		}
		public List<String> getExtra() {
			return extra;
		}
	}
	
	private class MercuryResponse {
		String content;
		String title;
		String excerpt;
	}
	
	public void setHtmlService(HTMLService htmlService) {
		this.htmlService = htmlService;
	}
	
}