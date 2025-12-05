package org.onlineobjects.modules.suggestion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.util.Strings;
import org.eclipse.jdt.annotation.Nullable;

import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.Pair;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.InternetAddress;
import dk.in2isoft.onlineobjects.model.Question;
import dk.in2isoft.onlineobjects.model.Statement;
import dk.in2isoft.onlineobjects.modules.knowledge.InternetAddressApiPerspective;
import dk.in2isoft.onlineobjects.modules.knowledge.KnowledgeService;
import dk.in2isoft.onlineobjects.services.SemanticService;
import opennlp.tools.doccat.DocumentSample;
import opennlp.tools.util.ObjectStream;

public class QuestionTrainingStream implements ObjectStream<DocumentSample> {

	private List<Pair<String, String[]>> stuff = new ArrayList<>();
	private Iterator<Pair<String, String[]>> iter;

	public QuestionTrainingStream(Operator operator, ModelService model, KnowledgeService knowledge, SemanticService semantics) throws EndUserException {
		List<Question> questions = model.list(Query.after(Question.class), operator);
		for (Question question : questions) {
			List<Statement> statements = model.getParents(question, Statement.class, operator);
			String key = String.valueOf(question.getId());
			for (Statement statement : statements) {
				@Nullable
				InternetAddress internetAddress = model.getParent(statement, InternetAddress.class, operator);
				if (internetAddress != null) {
					InternetAddressApiPerspective perspective = knowledge.getAddressPerspective(internetAddress.getId(), operator);
					if (perspective != null) {
						stuff.add(Pair.of(key, extract(perspective.getText(), semantics)));
					}
				}
				if (Strings.isNotBlank(statement.getText())) {
					stuff.add(Pair.of(key, extract(statement.getText(), semantics)));
				}
			}
		}
		iter = stuff.iterator();
	}

	private String[] extract(String text, SemanticService semantics) {
		return semantics.getTokensAsString(text, Locale.ENGLISH);
	}

	@Override
	public DocumentSample read() throws IOException {
		if (iter.hasNext()) {
			Pair<String,String[]> pair = iter.next();
			return new DocumentSample(pair.getKey(), pair.getValue());
		}
		return null;
	}

	@Override
	public void reset() throws IOException, UnsupportedOperationException {
		iter = stuff.iterator();
	}

	@Override
	public void close() throws IOException {
	}

}
