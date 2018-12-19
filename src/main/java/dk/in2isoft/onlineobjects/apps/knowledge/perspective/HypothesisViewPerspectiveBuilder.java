package dk.in2isoft.onlineobjects.apps.knowledge.perspective;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import dk.in2isoft.commons.lang.HTMLWriter;
import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.exceptions.ContentNotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Hypothesis;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.Statement;
import dk.in2isoft.onlineobjects.model.User;

public class HypothesisViewPerspectiveBuilder extends EntityViewPerspectiveBuilder {


	public HypothesisViewPerspective build(long id, Privileged privileged) throws ModelException, ContentNotFoundException, SecurityException {
		@Nullable
		Hypothesis question = modelService.get(Hypothesis.class, id, privileged);
		if (question == null) {
			throw new ContentNotFoundException(Hypothesis.class, id);
		}
		HypothesisViewPerspective perspective = new HypothesisViewPerspective();
		perspective.setId(id);
		perspective.setText(question.getText());

		User user = modelService.getRequired(User.class, privileged.getIdentity(), privileged);
		knowledgeService.categorize(question, perspective, user);

		List<Statement> supports = modelService.getParents(question, Relation.SUPPORTS, Statement.class, privileged);
		List<Statement> contradicts = modelService.getParents(question, Relation.CONTRADTICS, Statement.class, privileged);

		HTMLWriter html = new HTMLWriter();

		writeList(html, "Supporting", supports, privileged);

		writeList(html, "Contradicting", contradicts, privileged);
		
		perspective.setRendering(html.toString());
		return perspective;
	}
}
