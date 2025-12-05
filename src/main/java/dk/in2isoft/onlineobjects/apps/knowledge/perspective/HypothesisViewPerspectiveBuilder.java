package dk.in2isoft.onlineobjects.apps.knowledge.perspective;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import dk.in2isoft.commons.lang.HTMLWriter;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.NotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Hypothesis;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.Statement;
import dk.in2isoft.onlineobjects.model.User;

public class HypothesisViewPerspectiveBuilder extends EntityViewPerspectiveBuilder {


	public HypothesisViewPerspective build(long id, Operator operator) throws ModelException, NotFoundException, SecurityException {
		@Nullable
		Hypothesis question = modelService.get(Hypothesis.class, id, operator);
		if (question == null) {
			throw new NotFoundException(Hypothesis.class, id);
		}
		HypothesisViewPerspective perspective = new HypothesisViewPerspective();
		perspective.setId(id);
		perspective.setText(question.getText());

		User user = modelService.getRequired(User.class, operator.getIdentity(), operator);
		knowledgeService.categorize(question, perspective, user, operator);

		List<Statement> supports = modelService.getParents(question, Relation.SUPPORTS, Statement.class, operator);
		List<Statement> contradicts = modelService.getParents(question, Relation.CONTRADTICS, Statement.class, operator);

		HTMLWriter html = new HTMLWriter();

		writeList(html, "Supporting", supports, operator);

		writeList(html, "Contradicting", contradicts, operator);

		perspective.setRendering(html.toString());
		return perspective;
	}
}
