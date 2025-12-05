package dk.in2isoft.onlineobjects.publishing;

import java.util.List;

import javax.xml.stream.XMLStreamException;

import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.HeaderPart;
import dk.in2isoft.onlineobjects.model.HtmlPart;
import dk.in2isoft.onlineobjects.model.Image;
import dk.in2isoft.onlineobjects.model.ImageGallery;
import dk.in2isoft.onlineobjects.services.ConfigurationService;
import dk.in2isoft.onlineobjects.services.ConversionService;
import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Node;

public class ImageGalleryBuilder extends DocumentBuilder implements FeedBuilder {

	private static String NAMESPACE = "http://uri.onlineobjects.com/publishing/Document/ImageGallery/";

	private ModelService modelService;
	private ConversionService conversionService;
	private ConfigurationService configurationService;

	// private static Logger log = LogManager.getLogger(ImageGalleryBuilder.class);

	public ImageGalleryBuilder() {
		super();
	}

	@Override
	public Class<? extends Entity> getEntityType() {
		return ImageGallery.class;
	}

	@Override
	public Node build(Document document, Operator operator) throws EndUserException {
		ImageGallery gallery = (ImageGallery) document;

		String style = gallery.getPropertyValue(ImageGallery.PROPERTY_FRAMESTYLE);
		if (style == null)
			style = "simple";
		Element root = new Element("ImageGallery", NAMESPACE);
		Element settings = new Element("settings", NAMESPACE);
		settings.addAttribute(new Attribute("tiledColumns", String.valueOf(gallery.getTiledColumns())));
		settings.addAttribute(new Attribute("tiledWidth", String.valueOf(gallery.getTiledWidth())));
		settings.addAttribute(new Attribute("tiledHeight", String.valueOf(gallery.getTiledHeight())));
		settings.addAttribute(new Attribute("style", style));
		root.appendChild(settings);

		HeaderPart header = modelService.getChild(gallery, HeaderPart.class, operator);
		if (header != null) {
			root.appendChild(conversionService.generateXML(header, operator));
		}

		HtmlPart html = modelService.getChild(gallery, HtmlPart.class, operator);
		if (html != null) {
			root.appendChild(conversionService.generateXML(html, operator));
		}

		Element tiled = new Element("tiled", NAMESPACE);
		root.appendChild(tiled);
		Element row = new Element("row", NAMESPACE);
		int columns = gallery.getTiledColumns();
		List<Image> images = modelService.getChildrenOrdered(gallery, Image.class, operator);
		for (int i = 0; i < images.size(); i++) {
			if (i % columns == 0) {
				row = new Element("row", NAMESPACE);
				tiled.appendChild(row);
			}
			Entity image = images.get(i);
			row.appendChild(conversionService.generateXML(image, operator));
		}
		return root;
	}

	@Override
	public Entity create(Operator priviledged) throws EndUserException {

		// Create an image gallery
		ImageGallery gallery = new ImageGallery();
		gallery.setName("Mine billeder");
		modelService.create(gallery, priviledged);

		// Create gallery title
		HeaderPart header = new HeaderPart();
		header.setText("Mine billeder");
		modelService.create(header, priviledged);
		modelService.createRelation(gallery, header, priviledged);

		// Create gallery title
		HtmlPart text = new HtmlPart();
		text.setHtml("Dette er nogle billeder jeg har taget");
		modelService.create(text, priviledged);
		modelService.createRelation(gallery, text, priviledged);

		return gallery;
	}

	@Override
	public void buildFeed(Document document, FeedWriter writer, Operator privileged) throws EndUserException {
		ImageGallery gallery = (ImageGallery) document;
		List<Image> images = modelService.getChildren(gallery, Image.class, privileged);
		try {
			writer.startFeed();
			writer.startChannel(gallery.getName(),configurationService.getBaseUrl());
			for (Image image : images) {
				writer.writeItem(image.getName(), image.getPropertyValue(Image.PROPERTY_DESCRIPTION), image.getUpdated());
			}
			writer.endChannel();
			writer.endFeed();
		} catch (XMLStreamException e) {
			throw new EndUserException(e);
		}
	}

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}
}
