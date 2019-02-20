package dk.in2isoft.onlineobjects.apps.knowledge.perspective;

import java.util.List;

import dk.in2isoft.commons.lang.HTMLWriter;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.exceptions.ContentNotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Question;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.Statement;
import dk.in2isoft.onlineobjects.model.User;

public class QuestionViewPerspectiveBuilder extends EntityViewPerspectiveBuilder {

	public QuestionViewPerspective build(long id, Operator operator) throws ModelException, ContentNotFoundException, SecurityException {
		Question question = modelService.getRequired(Question.class, id, operator);
		QuestionViewPerspective perspective = new QuestionViewPerspective();
		perspective.setId(id);
		perspective.setText(question.getText());
		User user = modelService.getUser(operator);

		knowledgeService.categorize(question, perspective, user, operator);

		List<Statement> answers = modelService.getParents(question, Relation.ANSWERS, Statement.class, operator);

		HTMLWriter html = new HTMLWriter();
		
		writeList(html, "Answers", answers, operator);

		perspective.setRendering(html.toString());
		return perspective;
	}
}
