package dk.in2isoft.onlineobjects.modules.index;

import java.util.List;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.TextField;

import com.google.common.collect.Sets;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.model.Image;
import dk.in2isoft.onlineobjects.model.Privilege;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.model.Word;

public class PhotoIndexDocumentBuilder implements IndexDocumentBuilder<Image> {
	
	private ModelService modelService;
	private SecurityService securityService;
	
	public Document build(Image image, Operator operator) throws ModelException {
		Operator admin = operator.as(securityService.getAdminPrivileged());
		image = modelService.get(Image.class, image.getId(), operator.as(admin));
		if (image == null) {
			return null;
		}
		StringBuilder text = new StringBuilder();
		text.append(image.getName());
		String glossary = image.getPropertyValue(Image.PROPERTY_DESCRIPTION);
		if (Strings.isNotBlank(glossary)) {
			text.append(" ").append(glossary);
		}
		
		Document doc = new Document();
		doc.add(new TextField("text", text.toString(), Field.Store.YES));
		doc.add(new LongField("fileSize", image.getFileSize(), Field.Store.YES));
		doc.add(new IntField("width", image.getWidth(), Field.Store.YES));
		doc.add(new IntField("height", image.getHeight(), Field.Store.YES));

		Set<Long> viewers = Sets.newHashSet();
		List<Privilege> priviledges = modelService.getPrivileges(image, admin);
		for (Privilege privilege : priviledges) {
			if (privilege.isView()) {
				viewers.add(privilege.getSubject());
			}
		}
		for (Long id : viewers) {
			doc.add(new LongField("viewerId",id,Field.Store.YES));
		}
		// TODO: Is it ok to load this using admin?
		List<Word> words = modelService.getChildren(image, null, Word.class, admin);
		for (Word word : words) {
			doc.add(new TextField("word", word.getText(), Field.Store.YES));
			doc.add(new LongField("wordId",word.getId(),Field.Store.YES));
		}
		User owner = modelService.getOwner(image, admin);
		if (owner!=null) {
			doc.add(new LongField("ownerId",owner.getId(),Field.Store.YES));
		}
		boolean publico = securityService.isPublicView(image, admin);
		doc.add(new TextField("public",publico ? "true" : "false",Field.Store.YES));
		return doc;
	}
	
	// Wiring...
	
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
	
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
}
