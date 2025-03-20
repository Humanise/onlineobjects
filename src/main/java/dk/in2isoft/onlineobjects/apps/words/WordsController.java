package dk.in2isoft.onlineobjects.apps.words;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import com.google.common.collect.Maps;

import dk.in2isoft.commons.lang.HTMLWriter;
import dk.in2isoft.in2igui.data.Diagram;
import dk.in2isoft.onlineobjects.apps.words.importing.WordsImporter;
import dk.in2isoft.onlineobjects.core.Path;
import dk.in2isoft.onlineobjects.core.View;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.BadRequestException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.Language;
import dk.in2isoft.onlineobjects.model.LexicalCategory;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.modules.importing.DataImporter;
import dk.in2isoft.onlineobjects.modules.importing.ImportSession;
import dk.in2isoft.onlineobjects.modules.language.WordListPerspective;
import dk.in2isoft.onlineobjects.modules.language.WordListPerspectiveQuery;
import dk.in2isoft.onlineobjects.ui.Request;
import dk.in2isoft.onlineobjects.util.Messages;

public class WordsController extends WordsControllerBase {

	@Path(expression = "/(en|da)?")
	@View(jsf = "front.xhtml")
	public void front(Request request) {}

	@Path(expression = "/<language>")
	@View(jsf = "front.xhtml")
	public void frontWithLanguage(Request request) {}

	@Path(expression = "/<language>/statistics")
	@View(jsf = "statistics.xhtml")
	public void statistics(Request request) {}

	@Path(expression = "/<language>/search(/[0-9]+)?")
	@View(jsf = "search.xhtml")
	public void search(Request request) {}

	@Path(expression = "/<language>/about")
	@View(jsf = "about.xhtml")
	public void about(Request request) {}
	
	@Path(expression = "/<language>/word/<any>")
	@View(jsf = "word.xhtml")
	public void word(Request request) {}
	
	@Path(exactly="importUpload")
	public void upload(Request request) throws IOException, EndUserException {
		ImportSession session = importService.createImportSession(request.getSession());
		DataImporter importer = importService.createImporter();
		WordsImporter listener = new WordsImporter();
		session.setTransport(listener);
		importer.setListener(listener);
		importer.setSuccessResponse(session.getId());
		importer.importMultipart(this, request);
	}
	
	@Override
	public boolean isAllowed(Request request) {
		String[] localPath = request.getLocalPath();
		if (localPath.length>1 && "index".equals(localPath[1])) {
			return !securityService.isPublicUser(request.getSession());
		}
		return super.isAllowed(request);
	}
	
	@Override
	public boolean logAccessExceptions() {
		return false;
	}
	
	@Path(exactly="diagram.json")
	public void getDiagram(Request request) throws ModelException, IOException {
		String text = request.getString("word");
		// TODO: Generalize this to all robots
		String agent = request.getRequest().getHeader("User-Agent");
		if (agent != null && agent.contains("Googlebot")) {
			request.sendObject(new Diagram());
			return;
		}
		
		Diagram diagram = wordsModelService.getDiagram(text, request);
		
		request.sendObject(diagram);
	}

	@Path
	public void createWord(Request request) throws IOException, EndUserException {
		wordsModelService.createWord(request.getString("language"), request.getString("category"), request.getString("text"), request);
	}

	@Path
	public void relateWords(Request request) throws IOException, EndUserException {
		wordsModelService.relateWords(request.getLong("parentId"), request.getString("kind"), request.getLong("childId"), request);
	}

	@Path
	public void changeLanguage(Request request) throws IOException, EndUserException {
		wordsModelService.changeLanguage(request.getLong("wordId"), request.getString("language"), request);
	}

	@Path
	public void changeCategory(Request request) throws IOException, EndUserException {
		wordsModelService.changeCategory(request.getLong("wordId"), request.getString("category"), request);
	}

	@Path
	public void deleteWord(Request request) throws IOException, EndUserException {
		wordsModelService.deleteWord(request.getLong("wordId"), request);
	}

	@Path
	public void deleteRelation(Request request) throws IOException, EndUserException {
		wordsModelService.deleteRelation(request.getLong("relationId"), request);
	}
	
	@Path
	public Map<String, Object> getRelationInfo(Request request) throws EndUserException, IOException {
		Long relationid = request.getLong("relationId");
		Long wordId = request.getLong("wordId");
		Locale locale = new Locale(request.getString("language"));
		
		Relation relation = modelService.getRelation(relationid, request).orElseThrow(() -> new BadRequestException("Word not found"));
		Entity to = relation.getTo();
		Entity from = relation.getFrom();
		Entity word = null;
		if (to.getId()==wordId) {
			word = to;
		} else if (from.getId()==wordId) {
			word = from;
		} else {
			throw new BadRequestException("Word not found");
		}
		
		WordListPerspectiveQuery query = new WordListPerspectiveQuery().withWord(word);
		WordListPerspective perspective = modelService.search(query, request).getFirst();
		Map<String,Object> map = Maps.newHashMap();
		map.put("word", perspective);
		

		Messages msg = new Messages(this);
		Messages langMsg = new Messages(Language.class);
		Messages lexMsg = new Messages(LexicalCategory.class);
		
		HTMLWriter writer = new HTMLWriter();
		writer.startDiv();
		writer.startH1().text(perspective.getText()).endH1();
		if (perspective.getGlossary()!=null) {
			writer.startP().withClass("glossary").text(perspective.getGlossary()).endP();			
		}
		writer.startP().withClass("kind").startStrong().text(msg.get(relation.getKind(), locale)).endStrong();
		if (perspective.getLanguage()!=null) {
			writer.text(" - ").text(langMsg.get("code",perspective.getLanguage(), locale));			
		}
		if (perspective.getLexicalCategory()!=null) {
			writer.text(" - ").text(lexMsg.get("code",perspective.getLexicalCategory(), locale));			
		}
		
		writer.endP();
		writer.endDiv();
		
		map.put("rendering", writer.toString());
		map.put("id", relation.getId());
		
		return map;
	}
}
