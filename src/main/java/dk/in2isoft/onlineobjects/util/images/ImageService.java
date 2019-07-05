package dk.in2isoft.onlineobjects.util.images;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.lang.Rational;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.iptc.IptcDirectory;
import com.google.common.collect.Sets;

import dk.in2isoft.commons.geo.GeoDistance;
import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.commons.util.AbstractCommandLineInterface;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Image;
import dk.in2isoft.onlineobjects.model.Location;
import dk.in2isoft.onlineobjects.model.Property;
import dk.in2isoft.onlineobjects.services.ConfigurationService;
import dk.in2isoft.onlineobjects.services.FileService;
import dk.in2isoft.onlineobjects.services.StorageService;
import dk.in2isoft.onlineobjects.util.images.ImageInfo.ImageLocation;

public class ImageService extends AbstractCommandLineInterface {
	
	private static final int NONE = 0;
	private static final int HORIZONTAL = 1;
	private static final int VERTICAL = 2;
	private static final int[][] EXIF_ORIENTATION = new int[][] {
	    new int[] {  0, NONE},
	    new int[] {  0, HORIZONTAL},
	    new int[] {180, NONE},
	    new int[] {  0, VERTICAL},
	    new int[] { 90, HORIZONTAL},
	    new int[] { 90, NONE},
	    new int[] {-90, HORIZONTAL},
	    new int[] {-90, NONE},
	};

	private static Logger log = LogManager.getLogger(ImageService.class);
	private Set<String> mimes = Sets.newHashSet("image/jpeg", "image/png", "image/gif", "application/pdf");
	private Set<String> extensions = Sets.newHashSet("jpg", "png", "gif", "pdf");
	
	private StorageService storageService;
	private ConfigurationService configurationService;
	private FileService fileService;
	private ModelService modelService;

	public ImageService() {
	}
	
	private String magickTypeToMimeType(String magick) {
		if ("PNG".equals( magick)) {
			return "image/png";
		}
		else if ("JPEG".equals( magick)) {
			return "image/jpeg";
		}
		else if ("PDF".equals( magick)) {
			return "application/pdf";
		}
		return null;
	}
	
	public ImageProperties getImageProperties(File file) throws EndUserException {
		log.debug(file.getAbsolutePath());
		log.debug("Exists: " + file.exists());
		String cmd = configurationService.getImageMagickPath() + "/identify -quiet -format \"%m-%wx%h\" " + file.getAbsolutePath();
		String result = execute(cmd).trim();
		Pattern pattern = Pattern.compile(".*\"([a-zA-Z]+)-([0-9]+)x([0-9]+)\"");
		Matcher matcher = pattern.matcher(result);
		if (matcher.matches()) {
			ImageProperties properties = new ImageProperties();
			String format = matcher.group(1);
			int width = Integer.parseInt(matcher.group(2));
			int height = Integer.parseInt(matcher.group(3));
			properties.setMimeType(magickTypeToMimeType(format));
			properties.setWidth(width);
			properties.setHeight(height);
			return properties;
		} else {
			throw new EndUserException("Could not parse output: " + result);
		}
	}
	
	public boolean isSupportedMimeType(String mime) {
		return mime!=null && mimes.contains(mime);
	}

	public boolean isSupportedExtension(String extension) {
		return extension!=null && extensions.contains(extension);
	}
	
	public String getColors(Image image) throws EndUserException {
		File file = getImageFile(image);
		ImageProperties props = getImageProperties(file);
		int width = props.getWidth();
		int height = props.getHeight();
		String top = getAverageColor(file, width, Math.round(height/3f), 0, 0);
		String middle = getAverageColor(file, width, Math.round(height/3f), 0, Math.round(height/3f));
		String bottom = getAverageColor(file, width, Math.round(height/3f), 0, Math.round(height/3f*2)); 
		return "rgb(" + top + "),rgb(" + middle + "),rgb(" + bottom + ")";
	}

	private String getAverageColor(File file, int width, int height, int x, int y) throws EndUserException {
		String cmd = "/convert " + file.getAbsolutePath() + " -crop "+width+"x"+height+"+"+x+"+"+y+" -resize 1x1 -format %[fx:int(255*r+.5)],%[fx:int(255*g+.5)],%[fx:int(255*b+.5)] info:-";
		return execute(configurationService.getImageMagickPath() + "/" + cmd);
	}
	
	public ImageMetaData getMetaData(Image image) {
		return getMetaData(getImageFile(image));
	}
	
	public ImageMetaData getMetaData(File file) {
		ImageMetaData imageMetaData = new ImageMetaData();
		try {
			Metadata metadata;
			// TODO: No need to try this if not a JPEG
			metadata = JpegMetadataReader.readMetadata(file);
			Directory exifDirectory = metadata.getDirectory(ExifIFD0Directory.class);
			Directory gpsDirectory = metadata.getDirectory(GpsDirectory.class);
			Directory iptcDirectory = metadata.getDirectory(IptcDirectory.class);
			
			if (exifDirectory!=null) {
				if (exifDirectory.containsTag(ExifIFD0Directory.TAG_DATETIME)) {
					imageMetaData.setDateTime(exifDirectory.getDate(ExifIFD0Directory.TAG_DATETIME));
				}
				if (exifDirectory.containsTag(ExifIFD0Directory.TAG_MAKE)) {
					imageMetaData.setCameraMake(exifDirectory.getString(ExifIFD0Directory.TAG_MAKE));
				}
				if (exifDirectory.containsTag(ExifIFD0Directory.TAG_MODEL)) {
					imageMetaData.setCameraModel(exifDirectory.getString(ExifIFD0Directory.TAG_MODEL));
				}
				if (exifDirectory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
					int orientation = exifDirectory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
					imageMetaData.setOrientation(orientation);
					if (orientation>0 && orientation<=EXIF_ORIENTATION.length) {
						orientation--;
						int degrees = EXIF_ORIENTATION[orientation][0];
						imageMetaData.setRotation(degrees);
				        switch (EXIF_ORIENTATION[orientation][1]) {
				            case HORIZONTAL:
				                imageMetaData.setFlippedHorizontally(true);
				                break;
				            case VERTICAL:
				                imageMetaData.setFlippedVertically(true);
				        }
					}
				}
			}
			if (iptcDirectory!=null) {
				if (iptcDirectory.containsTag(IptcDirectory.TAG_OBJECT_NAME)) {
					imageMetaData.setObjectName(iptcDirectory.getString(IptcDirectory.TAG_OBJECT_NAME));
				}
				if (iptcDirectory.containsTag(IptcDirectory.TAG_CAPTION)) {
					imageMetaData.setCaption(iptcDirectory.getString(IptcDirectory.TAG_CAPTION));
				}
				if (iptcDirectory.containsTag(IptcDirectory.TAG_KEYWORDS)) {
					imageMetaData.setKeywords(iptcDirectory.getStringArray(IptcDirectory.TAG_KEYWORDS));
				}
			}
			if (gpsDirectory!=null) {
				if (gpsDirectory.containsTag(GpsDirectory.TAG_GPS_LATITUDE) && gpsDirectory.containsTag(GpsDirectory.TAG_GPS_LATITUDE_REF)) {
					String ref = gpsDirectory.getString(GpsDirectory.TAG_GPS_LATITUDE_REF);
					Rational[] dist = gpsDirectory.getRationalArray(GpsDirectory.TAG_GPS_LATITUDE);
					double decimal = getDecimal(dist,ref);
					imageMetaData.setLatitude(decimal);
				}
				if (gpsDirectory.containsTag(GpsDirectory.TAG_GPS_LONGITUDE) && gpsDirectory.containsTag(GpsDirectory.TAG_GPS_LONGITUDE_REF)) {
					String ref = gpsDirectory.getString(GpsDirectory.TAG_GPS_LONGITUDE_REF);
					Rational[] dist = gpsDirectory.getRationalArray(GpsDirectory.TAG_GPS_LONGITUDE);
					double decimal = getDecimal(dist,ref);
					imageMetaData.setLongitude(decimal);
				}
				Collection<Tag> tags = gpsDirectory.getTags();
				for (Tag tag : tags) {
					if (gpsDirectory.containsTag(tag.getTagType())) {
						Object object = gpsDirectory.getObject(tag.getTagType());
						if (object instanceof Rational[]) {
							Rational[] pos = (Rational[]) object;
							GeoDistance convert = new GeoDistance(pos[0].longValue(), pos[1].longValue(), pos[2].longValue());
							log.info(tag.getTagName()+" != "+convert.getDecimal());
						} else {
							log.info(tag.getTagName()+" = "+object);
						}
					}
				}
			}
		} catch (JpegProcessingException e) {
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} catch (MetadataException e) {
			log.error(e.getMessage(), e);
		}
		return imageMetaData;
	}
	
	private double getDecimal(Rational[] triple, String ref) {
		GeoDistance point = new GeoDistance(triple[0].doubleValue(),triple[1].doubleValue(),triple[2].doubleValue());
		double decimal = point.getDecimal();
		if (ref.equals("S") || ref.equals("W")) {
			decimal*=-1;
		}
		
		return decimal;
	}
	
	public void synchronizeContentType(Image image, Operator priviledged) throws EndUserException {
		File file = getImageFile(image);
		String mimeType = fileService.getMimeType(file);
		if (!StringUtils.equals(mimeType, image.getContentType())) {
			image.setContentType(mimeType);
			modelService.update(image, priviledged);
		}
	}
	
	public void synchronizeMetaData(Image image, Operator priviledged) throws EndUserException {
		File file = getImageFile(image);
		ImageMetaData metaData = getMetaData(file);
		boolean modified = false;
		Date taken = image.getPropertyDateValue(Property.KEY_PHOTO_TAKEN);
		if (taken==null) {
			image.overrideFirstProperty(Property.KEY_PHOTO_TAKEN, metaData.getDateTime());
			modified = true;
		}
		String make = image.getPropertyValue(Property.KEY_PHOTO_CAMERA_MAKE);
		if (make==null) {
			image.overrideFirstProperty(Property.KEY_PHOTO_CAMERA_MAKE, metaData.getCameraMake());
			modified = true;
		}
		String model = image.getPropertyValue(Property.KEY_PHOTO_CAMERA_MODEL);
		if (model==null) {
			image.overrideFirstProperty(Property.KEY_PHOTO_CAMERA_MODEL, metaData.getCameraModel());
			modified = true;
		}
		if (!ArrayUtils.isEmpty(metaData.getKeywords())) {
			image.overrideProperties(Property.KEY_COMMON_TAG, Arrays.asList(metaData.getKeywords()));
			modified = true;
		}
		if (metaData.getObjectName()!=null) {
			image.setName(metaData.getObjectName());
			modified = true;
		}
		if (metaData.getCaption()!=null) {
			image.overrideFirstProperty(Image.PROPERTY_DESCRIPTION, metaData.getCaption());
			modified = true;
		}
		if (metaData.getRotation()!=null) {
			image.overrideFirstProperty(Property.KEY_PHOTO_ROTATION, metaData.getRotation().doubleValue());
			modified = true;
		}
		if (Boolean.TRUE.equals(metaData.getFlippedHorizontally())) {
			image.overrideFirstProperty(Property.KEY_PHOTO_FLIP_HORIZONTALLY, "true");
			modified = true;
		}
		if (Boolean.TRUE.equals(metaData.getFlippedVertically())) {
			image.overrideFirstProperty(Property.KEY_PHOTO_FLIP_VERTICALLY, "true");
			modified = true;
		}
		String colors = image.getPropertyValue(Property.KEY_PHOTO_COLORS);
		if (Strings.isBlank(colors)) {
			String newColors = getColors(image);
			image.overrideFirstProperty(Property.KEY_PHOTO_COLORS, newColors);
			modified = true;
		}
		if (modified) {
			modelService.update(image, priviledged);
		}
		if (metaData.getLatitude()!=null && metaData.getLongitude()!=null) {
			Location location = modelService.getParent(image, Location.class, priviledged);
			if (location==null) {
				location = new Location();
				location.setLatitude(metaData.getLatitude());
				location.setLongitude(metaData.getLongitude());
				modelService.create(location, priviledged);
				modelService.createRelation(location, image, null, priviledged);
			}
		}
	}

	public ImageInfo getImageInfo(Image image, Operator privileged) throws ModelException {
		ImageInfo info = new ImageInfo();
		info.setId(image.getId());
		info.setName(image.getName());
		info.setTaken(image.getPropertyDateValue(Property.KEY_PHOTO_TAKEN));
		info.setDescription(image.getPropertyValue(Image.PROPERTY_DESCRIPTION));
		info.setCameraMake(image.getPropertyValue(Property.KEY_PHOTO_CAMERA_MAKE));
		info.setCameraModel(image.getPropertyValue(Property.KEY_PHOTO_CAMERA_MODEL));
		info.setTags(image.getPropertyValues(Property.KEY_COMMON_TAG));
		Location location = modelService.getParent(image, Location.class, privileged);
		if (location!=null) {
			info.setLocation(new ImageLocation(location.getLatitude(), location.getLongitude()));
		}
		info.setRotation(image.getPropertyDoubleValue(Property.KEY_PHOTO_ROTATION));
		return info;
	}
	
	public void updateImageInfo(ImageInfo info, Operator priviledged) throws ModelException, SecurityException {

		Image image = modelService.get(Image.class, info.getId(),priviledged);
		image.setName(info.getName());
		image.overrideFirstProperty(Image.PROPERTY_DESCRIPTION, info.getDescription());
		image.overrideFirstProperty(Property.KEY_PHOTO_TAKEN, info.getTaken());
		image.overrideProperties(Property.KEY_COMMON_TAG, info.getTags());
		modelService.update(image, priviledged);
		Location location = modelService.getParent(image, Location.class, priviledged);
		if (info.getLocation()==null) {
			if (location!=null) {
				modelService.delete(location, priviledged);
			}
			return;
		}
		if (info.getLocation()==null && location!=null) {
			modelService.delete(location, priviledged);
		} else if (info.getLocation()!=null && location==null) {
			location = new Location();
			location.setLatitude(info.getLocation().getLatitude());
			location.setLongitude(info.getLocation().getLongitude());
			modelService.create(location, priviledged);
			modelService.createRelation(location, image, priviledged);
		} else {
			location.setLatitude(info.getLocation().getLatitude());
			location.setLongitude(info.getLocation().getLongitude());			
			modelService.update(location, priviledged);
		}
	}
	
	public Image createImageFromFile(File file, String name, Operator privileged) throws ModelException {
		try {
			ImageProperties properties = getImageProperties(file);
			Image image = new Image();
			modelService.create(image, privileged);
			image.setName(name);
			changeImageFile(image, file, properties.getMimeType());
			synchronizeMetaData(image, privileged);
			modelService.update(image, privileged);
			return image;
		} catch (EndUserException e) {
			log.error("Unable to create image from file",e);
		}
		return null;
	}
	
	public void updateImageLocation(Image image, ImageLocation imageLocation, Operator priviledged) throws ModelException, SecurityException {
		Location existing = modelService.getParent(image, Location.class, priviledged);
		if (imageLocation==null && existing==null) {
			return;
		}
		else if (imageLocation==null && existing!=null) {
			// Delete
			modelService.delete(existing, priviledged);
		} else if (imageLocation!=null && existing==null) {
			// Update
			existing = new Location();
			existing.setLatitude(imageLocation.getLatitude());
			existing.setLongitude(imageLocation.getLongitude());
			modelService.create(existing, priviledged);
			modelService.createRelation(existing, image, priviledged);
		} else {
			// Add
			existing.setLatitude(imageLocation.getLatitude());
			existing.setLongitude(imageLocation.getLongitude());			
			modelService.update(existing, priviledged);
		}		
	}

	public void setStorageService(StorageService storageService) {
		this.storageService = storageService;
	}

	public StorageService getStorageService() {
		return storageService;
	}

	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	public ConfigurationService getConfigurationService() {
		return configurationService;
	}

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	public ModelService getModelService() {
		return modelService;
	}

	public void setFileService(FileService fileService) {
		this.fileService = fileService;
	}

	public FileService getFileService() {
		return fileService;
	}

	public File getImageFile(Image image) {
		File folder = storageService.getItemFolder(image);
		return new File(folder,"original");
	}

	public boolean hasImageFile(Image image) {
		if (image==null) {
			return false;
		}
		File folder = storageService.getItemFolder(image);
		return new File(folder,"original").exists();
	}

	public void changeImageFile(Image image, File file,String contentType) throws EndUserException {
		ImageProperties props = getImageProperties(file);
		image.setWidth(props.getWidth());
		image.setHeight(props.getHeight());
		image.setContentType(contentType);
		image.setFileSize(file.length());
		File folder = storageService.getItemFolder(image);
		file.renameTo(new File(folder,"original"));
	}

	public void deleteImage(Image image, Operator privileged) throws ModelException, SecurityException {
		Location location = modelService.getParent(image, Location.class, privileged);
		if (location!=null) {
			modelService.delete(location, privileged);
		}
		modelService.delete(image, privileged);
	}
}
