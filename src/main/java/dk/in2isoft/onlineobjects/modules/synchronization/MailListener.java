package dk.in2isoft.onlineobjects.modules.synchronization;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.Image;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.modules.inbox.InboxService;
import dk.in2isoft.onlineobjects.modules.scheduling.JobStatus;
import dk.in2isoft.onlineobjects.modules.user.MemberService;
import dk.in2isoft.onlineobjects.services.FileService;
import dk.in2isoft.onlineobjects.util.images.ImageService;

public class MailListener {

	private static final Logger log = LogManager.getLogger(MailListener.class);

	private ModelService modelService;
	private ImageService imageService;
	private FileService fileService;
	private MemberService memberService;

	private InboxService inboxService;
	
	public void mailArrived(Message message, JobStatus status) {
		Address[] from;
		boolean recognized = false;
		String subject = null;
		try {
			from = message.getFrom();
			subject = message.getSubject();
			status.log(message.getSubject() + " / " + message.getSentDate() + " / " + from);
			String usersEmail = null;
			for (Address address : from) {
				if (address instanceof InternetAddress) {
					InternetAddress iad = (InternetAddress) address;
					status.log("From: " + iad.getAddress() + " / " + iad.getPersonal());
					usersEmail = iad.getAddress();
				}
			}

			Object content = message.getContent();
			
			// If the email is multi part...
			if (content instanceof Multipart) {
				Multipart mp = (Multipart) content;
				int count = mp.getCount();
				log.info("Parts: " + count);
				for (int j = 0; j < count; j++) {
					BodyPart part = mp.getBodyPart(j);
					String contentType = part.getContentType();
					
					log.debug("-- part-type: " + contentType);
					log.debug("-- part-disp: " + part.getDisposition());

					if (contentType.toLowerCase().startsWith("image/jpg") || contentType.toLowerCase().startsWith("image/jpeg")) {
						recognized = true;
						handleImage(usersEmail, "image/jpg",part.getFileName(), message.getSubject(), part.getInputStream(),status);
					} else if (contentType.toLowerCase().startsWith("image/png")) {
						recognized = true;
						handleImage(usersEmail, "image/png",part.getFileName(), message.getSubject(), part.getInputStream(),status);
					} else if (contentType.startsWith("multipart")) {
						MimeMultipart x = new MimeMultipart(part.getDataHandler().getDataSource());
						log.debug("SUB: " + x.getCount());

						for (int k = 0; k < x.getCount(); k++) {
							BodyPart subPart = x.getBodyPart(k);
							String subContentType = subPart.getContentType();
							log.debug(" ------- " + subContentType);
							if (subContentType.toLowerCase().startsWith("image/jpg") || subContentType.toLowerCase().startsWith("image/jpeg")) {
								recognized = true;
								handleImage(usersEmail, "image/jpg", subPart.getFileName(), message.getSubject(), subPart.getInputStream(),status);
							} else if (contentType.toLowerCase().startsWith("image/png")) {
								recognized = true;
								handleImage(usersEmail, "image/png",subPart.getFileName(), message.getSubject(), subPart.getInputStream(),status);
							}
						}
					}
				}
			} else {
				log.debug("Content: " + content);
			}
		} catch (MessagingException e) {
			status.error("Error while checking email",e);
		} catch (IOException e) {
			status.error("Error while checking email",e);
		} finally {
			if (!recognized) {
				status.warn("The email was not recognized: "+ subject);
			}
		}

	}

	private void handleImage(String usersEmail, String mimeType, String fileName, String subject, InputStream inputStream, JobStatus status) {
		status.log("Image from: " + usersEmail + " of type: " + mimeType + "...");
		FileOutputStream output = null;
		Operator adminOperator = modelService.newAdminOperator();
		try {
			User user = memberService.getUserByPrimaryEmail(usersEmail, adminOperator);
			if (user!=null) {
				File tempFile = File.createTempFile(usersEmail, null);
				tempFile.deleteOnExit();
				output = new FileOutputStream(tempFile);
				status.log("Transfering image from e-mail");
				IOUtils.copy(inputStream, output);
				status.log("Creating image from file");
				String title;
				if (Strings.isNotBlank(subject)) {
					title = subject;
				} else {
					title = fileService.cleanFileName(fileName);
				}
				Image image = imageService.createImageFromFile(tempFile, title, adminOperator.as(user));
				inboxService.add(user, image, adminOperator.as(user));
				status.log("Created image "+image.getName()+" for the user "+user.getUsername()+", Image-ID: "+image.getId());
			} else {
				status.warn("Ignoring email since no user found: "+usersEmail);
			}
			adminOperator.commit();
		} catch (IOException e) {
			status.error("Unable to get stream", e);
			adminOperator.rollBack();
		} catch (EndUserException e) {
			status.error("Unable to create image", e);
			adminOperator.rollBack();
		} finally {
			IOUtils.closeQuietly(output);
			IOUtils.closeQuietly(inputStream);
		}
	}
	
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
	
	public void setImageService(ImageService imageService) {
		this.imageService = imageService;
	}
	
	public void setMemberService(MemberService memberService) {
		this.memberService = memberService;
	}
	
	public void setFileService(FileService fileService) {
		this.fileService = fileService;
	}
	
	public void setInboxService(InboxService inboxService) {
		this.inboxService = inboxService;
	}
}
