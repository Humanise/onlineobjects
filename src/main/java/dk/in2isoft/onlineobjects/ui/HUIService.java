package dk.in2isoft.onlineobjects.ui;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.StackObjectPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import dk.in2isoft.commons.lang.Files;
import dk.in2isoft.onlineobjects.services.ConfigurationService;

public class HUIService implements ApplicationListener<ContextRefreshedEvent> {

	private static Logger log = LogManager.getLogger(HUIService.class);

	private ObjectPool<Transformer> pool;

	private Templates templates;
	
	private ConfigurationService configurationService;

	private List<String> baseCSS;

	private List<String> baseJS;

	public HUIService() {
		super();
		pool = new StackObjectPool<Transformer>(new PoolableObjectFactory<Transformer>() {

			public void activateObject(Transformer arg0) throws Exception {
			}

			public void destroyObject(Transformer arg0) throws Exception {
			}

			public Transformer makeObject() throws Exception {
				return createTransformer(false);
			}

			public void passivateObject(Transformer arg0) throws Exception {
			}

			public boolean validateObject(Transformer arg0) {
				return true;
			}
			
		});
	}
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		baseCSS = readJSONStrings("info/core_css.json");
		baseJS = readJSONStrings("info/core_js.json");
	}

	private void render(StreamSource source, OutputStream output, String context,boolean devMode) throws IOException {
		Transformer transformer = null;;
		try {
			if (devMode) {
				transformer = createTransformer(true);
				 // create a SchemaFactory capable of understanding WXS schemas
			} else {
				transformer = (Transformer) pool.borrowObject();
			}
			transformer.setParameter("context", context);
			transformer.setParameter("dev", devMode);
			transformer.setParameter("version", "x");
			transformer.setParameter("profile", "false");
			transformer.setParameter("language", "da");
			transformer.setParameter("pathVersion", "");
			transformer.setParameter("unique", "uid"+System.currentTimeMillis());
			transformer.transform(source, new StreamResult(output));
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace(new PrintStream(output));
		} catch (TransformerConfigurationException e) {
			e.printStackTrace(new PrintStream(output));
		} catch (TransformerException e) {
			log.error(e.getMessage(), e);
			try {
				new OutputStreamWriter(output).write("<?xml version=\"1.0\"?><error><message>" + e.getMessage()
						+ "</message></error>");
			} catch (IOException ex) {
				log.error(ex.getMessage(), ex);
			}
		} catch (NoSuchElementException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (!devMode && transformer!=null) {
				try {
					pool.returnObject(transformer);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void validate(StreamSource source) throws IOException {
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		String path = "xslt/schema.xsd";
		String schemaPath = getFile(path).getAbsolutePath();
		// load a WXS schema, represented by a Schema instance
		Source schemaFile = new StreamSource(new File(schemaPath));
		Schema schema;
		try {
			schema = factory.newSchema(schemaFile);
		} catch (SAXException e1) {
			// TODO Auto-generated catch block
			log.error(e1);
			return;
		}

		// create a Validator instance, which can be used to validate an instance document
		Validator validator = schema.newValidator();
		validator.setErrorHandler(new ErrorHandler() {
			
			@Override
			public void warning(SAXParseException exception) throws SAXException {
				log.warn(exception.getMessage());
			}
			
			@Override
			public void fatalError(SAXParseException exception) throws SAXException {
				log.fatal(exception.getMessage());
			}
			
			@Override
			public void error(SAXParseException exception) throws SAXException {
				log.error("[" + exception.getLineNumber() + ":" + exception.getColumnNumber() + "] " + exception.getMessage());
			}
		});

		// validate the DOM tree
		try {
		    validator.validate(source);
		} catch (SAXException e) {
		    // instance document is invalid!
		}
	}

	private File getFile(String path) {
		return configurationService.getFile("hui/" + path);
	}
	
	private void setHeaders(HttpServletRequest request, HttpServletResponse response) {
		//String accept = request.getHeader("Accept");
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
	}

	public void render(String xmlData, HttpServletRequest request,HttpServletResponse response) throws IOException {
		setHeaders(request, response);
		OutputStream stream = response.getOutputStream();
		try {
			boolean devMode = configurationService.isDevelopmentMode();
			if (!xmlData.startsWith("<?xml")) {
				xmlData="<?xml version=\"1.0\"?>"+xmlData;
			}
			StringReader xmlReader = new StringReader(xmlData);
			if ("false".equals(request.getParameter("dev"))) {
				devMode=false;
			} else if ("true".equals(request.getParameter("dev"))) {
				devMode=true;
			}
			if (devMode) {
			    validate(new StreamSource( new StringReader(xmlData)));
			}
			render(new StreamSource(xmlReader), stream, request.getContextPath(),devMode);
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace(new PrintStream(stream));
		}
	}
	
	public String renderFragment(String ui) throws IOException {
		String rendered = render("<?xml version=\"1.0\"?><subgui xmlns=\"uri:hui\">"+ ui + "</subgui>");
		rendered = rendered.replaceAll("<![^>]+>", ""); // TODO instruct render to not output doctype
		return rendered;
	}
	
	public String render(String xmlData) throws IOException {
		if (!xmlData.startsWith("<?xml")) {
			xmlData="<?xml version=\"1.0\"?>"+xmlData;
		}
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		StringReader xmlReader = new StringReader(xmlData);
		render(new StreamSource(xmlReader), stream, "",false);
		return new String(stream.toByteArray(),"UTF-8");
	}

	public String render(File file, HttpServletRequest request) throws IOException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			boolean devMode = configurationService.isDevelopmentMode();
			render(new StreamSource(file), stream, request.getContextPath(),devMode);
			return new String(stream.toByteArray(),"UTF-8");
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace(new PrintStream(stream));
		}
		return "";
	}

	public void render(File file, HttpServletRequest request,HttpServletResponse response) throws IOException {
		setHeaders(request, response);
		OutputStream stream = response.getOutputStream();
		try {
			boolean devMode = configurationService.isDevelopmentMode();
			if ("true".equals(request.getParameter("nodev"))) {
				devMode=false;
			}
			render(new StreamSource(file), stream, request.getContextPath(),devMode);
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace(new PrintStream(stream));
		}
	}

	public List<String> getBaseCSS() {
		return baseCSS;
	}

	private List<String> readJSONStrings(String path) {
		File file = getFile(path);
		if (!file.exists()) {
			throw new IllegalStateException("File not found: " + file.getAbsolutePath());
		}
		String string = Files.readString(file);
		List<String> map = new Gson().fromJson(string, new TypeToken<ArrayList<String>>() {}.getType());
		List<String> files = map.stream().map(s -> "/hui/" + s).collect(Collectors.toList());
		return files;
	}
	
	public List<String> getBaseJS() {
		return baseJS;
	}
	
	private Transformer createTransformer(boolean newTemplates) throws TransformerFactoryConfigurationError, TransformerConfigurationException {
		if (templates == null || newTemplates) {
			StringBuilder xslString = new StringBuilder();
			xslString.append("<?xml version='1.0' encoding='UTF-8'?>").append(
					"<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='1.0'>").append(
					"<xsl:output method='xml' indent='no' encoding='UTF-8'/>").append(
					"<xsl:param name='dev'/><xsl:param name='version'/><xsl:param name='context'/><xsl:param name='profile'/><xsl:param name='language'/><xsl:param name='pathVersion'/><xsl:param name='unique'/>").append(
					"<xsl:include href='").append(configurationService.getFile("hui").getAbsolutePath()).append("/xslt/gui.xsl'/>").append(
					"<xsl:template match='/'><xsl:apply-templates/></xsl:template>").append("</xsl:stylesheet>");
			StringReader xslReader = new StringReader(xslString.toString());
			TransformerFactory factory = TransformerFactory.newInstance();
			templates = factory.newTemplates(new StreamSource(xslReader));
		}
		return templates.newTransformer();
	}
	
	// Wiring...
	
	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}
}
