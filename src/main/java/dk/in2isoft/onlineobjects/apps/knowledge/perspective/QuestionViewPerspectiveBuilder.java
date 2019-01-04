package dk.in2isoft.onlineobjects.apps.knowledge.perspective;

import java.util.List;

import dk.in2isoft.commons.lang.HTMLWriter;
import dk.in2isoft.onlineobjects.core.exceptions.ContentNotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Question;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.Statement;
import dk.in2isoft.onlineobjects.model.User;

public class QuestionViewPerspectiveBuilder extends EntityViewPerspectiveBuilder {

	public QuestionViewPerspective build(long id, User session) throws ModelException, ContentNotFoundException, SecurityException {
		Question question = modelService.getRequired(Question.class, id, session);
		QuestionViewPerspective perspective = new QuestionViewPerspective();
		perspective.setId(id);
		perspective.setText(question.getText());

		knowledgeService.categorize(question, perspective, session);

		List<Statement> answers = modelService.getParents(question, Relation.ANSWERS, Statement.class, session);

		HTMLWriter html = new HTMLWriter();
		
		writeList(html, "Answers", answers, session);

		perspective.setRendering(html.toString());
		return perspective;
	}
}
