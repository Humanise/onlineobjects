package dk.in2isoft.commons.lang;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.io.FileWriteMode;

public class Files {

	private static final Logger log = LogManager.getLogger(Files.class);

	static public boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

	public static String formatFileSize(Number bytes) {
		return FileUtils.byteCountToDisplaySize(bytes.intValue());
	}

	public static boolean overwriteTextFile(String text, File file) {
		if (file.exists()) {
			file.delete();
		}
		try {
			com.google.common.io.Files.asCharSink(file, StandardCharsets.UTF_8, FileWriteMode.APPEND).write(text);
			return true;
		} catch (IOException e) {
			log.error("Unable to overwrite text file", e);
		}
		return false;
	}


	public static String readString(File file) {
		return readString(file, Strings.UTF8);
	}

	public static String readString(File file, String encoding) {
		if (encoding==null) {
			encoding = Strings.UTF8;
		}
		try (FileInputStream inputStream = new FileInputStream(file)){
			return IOUtils.toString(inputStream, encoding);
		} catch (IOException e) {
			// Ignore
		}
		return null;
	}

	public static String cleanFileName(String fileName) {
		if (fileName==null) {
			return null;
		}
		int i = fileName.indexOf(".");
		if (i!=-1) {
			fileName = fileName.substring(0,i);
		}
		i = fileName.lastIndexOf("\\");
		if (i!=-1) {
			fileName = fileName.substring(i+1);
		}
		fileName = StringUtils.capitalize(fileName);
		fileName = StringUtils.replace(fileName, "_", " ");
		fileName = StringUtils.replace(fileName, "-", " ");
		fileName = fileName.replaceAll("[ ]+", " ");
		return fileName;
	}

	public static boolean copy(File temp, File original) {
		if (original.exists()) {
			log.error("Target file already exists: "+original);
			return false;
		}
		try (
			InputStream tempStream = new FileInputStream(temp);
			OutputStream outStream = new FileOutputStream(original)
		) {
			IOUtils.copy(tempStream, outStream);
			return true;
		}
		catch (FileNotFoundException e) {
			log.error("Unable to copy file",e);
		} catch (IOException e) {
			log.error("Unable to copy file",e);
		}
		return false;
	}

	public static boolean checkSensitivity(File file) {
		if (!file.exists()) {
			return true;
		}
		File[] files = file.getParentFile().listFiles();
		String absolutePath = file.getAbsolutePath();
		if (files != null) {
			for (File prospect : files) {
				if (prospect.getAbsolutePath().equals(absolutePath)) {
					return true;
				}
			}
		}
		return false;
	}
}
