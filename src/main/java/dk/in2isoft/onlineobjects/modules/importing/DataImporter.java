package dk.in2isoft.onlineobjects.modules.importing;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.core.FileUploadException;
import org.apache.commons.fileupload2.core.ProgressListener;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletDiskFileUpload;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Maps;

import dk.in2isoft.onlineobjects.apps.ApplicationController;
import dk.in2isoft.onlineobjects.apps.ApplicationSession;
import dk.in2isoft.onlineobjects.core.exceptions.BadRequestException;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.StupidProgrammerException;
import dk.in2isoft.onlineobjects.services.FileService;
import dk.in2isoft.onlineobjects.ui.AsynchronousProcessDescriptor;
import dk.in2isoft.onlineobjects.ui.Request;
import jakarta.servlet.http.HttpServletResponse;

public class DataImporter {

	private static Logger log = LogManager.getLogger(DataImporter.class);
	private ImportListener<?> listener;
	private FileService fileService;
	private String successResponse = "SUCCESS";

	public DataImporter(FileService fileService) {
		super();
		this.fileService = fileService;
	}

	public void importMultipart(ApplicationController controller,Request request) throws IOException, EndUserException {
		if (listener==null) {
			throw new StupidProgrammerException("No one ever listens!");
		}
		log.info("Starting upload");
		ApplicationSession session = request.getSession().getApplicationSession(controller);
		final AsynchronousProcessDescriptor process = session.createAsynchronousProcessDescriptor(listener.getProcessName());
		if (!JakartaServletFileUpload.isMultipartContent(request.getRequest())) {
			process.setError(true);
			throw new BadRequestException("The request is not multi-part!");
		}
		DiskFileItemFactory.builder().get();

		DiskFileItemFactory factory = DiskFileItemFactory.builder().get();
		//factory.setSizeThreshold(0);
		//factory.setRepository(fileService.getUploadDir());

		JakartaServletDiskFileUpload upload = new JakartaServletDiskFileUpload(factory);
		ProgressListener progressListener = new ProgressListener() {
			@Override
			public void update(long pBytesRead, long pContentLength, int pItems) {
				if (pContentLength == -1) {
					process.setValue(0);
				} else {
					process.setValue((float) pBytesRead / (float) pContentLength);
				}

			}
		};
		upload.setProgressListener(progressListener);

		// Parse the request
		try {
			List<DiskFileItem> items = upload.parseRequest(request.getRequest());
			Map<String,String> parameters = Maps.newHashMap();
			for (DiskFileItem item : items) {
				if (item.isFormField()) {
					parameters.put(item.getFieldName(), item.getString());
				}
			}
			for (DiskFileItem item : items) {
				if (!item.isFormField()) {
					try {
						item.getInputStream();
						if (item instanceof DiskFileItem) {
							File file = item.getPath().toFile();
							listener.processFile(file, fileService.getMimeType(file), item.getName(), parameters, request);
						}
					} catch (Exception e) {
						process.setError(true);
						throw new EndUserException(e);
					}
				}
			}
		} catch (FileUploadException e) {
			process.setError(true);
			throw new EndUserException(e);
		}
		process.setCompleted(true);
		HttpServletResponse response = request.getResponse();
		response.setStatus(HttpServletResponse.SC_OK);
		Object obj = listener.getResponse();
		if (obj!=null) {
			request.sendObject(obj);
		} else {
			response.getWriter().write(successResponse);
		}
	}

	public void setSuccessResponse(String successResponse) {
		this.successResponse = successResponse;
	}

	public void setListener(ImportListener<?> listener) {
		this.listener = listener;
	}

	public ImportListener<?> getListener() {
		return listener;
	}

	public void setFileService(FileService fileService) {
		this.fileService = fileService;
	}

	public FileService getFileService() {
		return fileService;
	}
}
