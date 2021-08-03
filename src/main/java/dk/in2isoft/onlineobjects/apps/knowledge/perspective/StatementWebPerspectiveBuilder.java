package dk.in2isoft.onlineobjects.apps.knowledge.perspective;

import org.eclipse.jdt.annotation.Nullable;

import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.exceptions.ContentNotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Statement;
import dk.in2isoft.onlineobjects.model.User;

public class StatementWebPerspectiveBuilder extends EntityViewPerspectiveBuilder {


	public StatementWebPerspective build(long id, Operator operator) throws ModelException, ContentNotFoundException, SecurityException {
		@Nullable
		Statement statement = modelService.get(Statement.class, id, operator);
		if (statement == null) {
			throw new ContentNotFoundException(Statement.class, id);
		}
		StatementWebPerspective perspective = new StatementWebPerspective();
		perspective.setId(id);
		perspective.setText(statement.getText());

		User user = modelService.getRequired(User.class, operator.getIdentity(), operator);
		knowledgeService.categorize(statement, perspective, user, operator);
		
		return perspective;
	}
}
