package dk.in2isoft.onlineobjects.modules.importing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dk.in2isoft.onlineobjects.modules.importing.ImportSession.Status;

public class HttpImportTransport<T> implements ImportTransport {

	private static final Logger log = LogManager.getLogger(HttpImportTransport.class);

	private String uri;
	private Status status = Status.waiting;
	private ImportListener<T> listener;

	private T result;

	public HttpImportTransport(String uri, ImportListener<T> listener) {
		this.uri = uri;
		this.listener = listener;
	}

	@Override
	public T getResult() {
		return result;
	}

	@Override
	public void start() {
		/*
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(uri);
		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			log.info("Url import started: "+uri);
			File tempFile = File.createTempFile("raw", null);
			tempFile.deleteOnExit();
			status = Status.transferring;
			client.executeMethod(method);
			String mimeType = null;
			Header header = method.getResponseHeader("Content-Type");
			if (header!=null) {
				log.info("Content-Type: "+header.getValue());
				mimeType = HeaderUtil.getContentTypesMimeType(header.getValue());
				log.info("Mime: "+mimeType);
			} else {
				log.warn("No header received from: "+uri);
			}
			inputStream = method.getResponseBodyAsStream();
			outputStream = new FileOutputStream(tempFile);
			IOUtils.copy(inputStream, outputStream);
			log.info("Url import finished");
			status = Status.processing;
			listener.processFile(tempFile, mimeType, null, null, null);
			result = listener.getResponse();
			status = Status.success;
			log.info("Processing the file finished");
		} catch (HttpException e) {
			this.status = Status.failure;
			log.error("Unable to get: "+uri,e);
		} catch (IOException e) {
			this.status = Status.failure;
			log.error("Unable to get: "+uri,e);
		} catch (EndUserException e) {
			this.status = Status.failure;
			log.error("Error processing: "+uri,e);
		} finally {
			log.info("Url import closing: "+status);
			IOUtils.closeQuietly(inputStream);
			IOUtils.closeQuietly(outputStream);
		}*/
	}

	@Override
	public Status getStatus() {
		return status;
	}

}
