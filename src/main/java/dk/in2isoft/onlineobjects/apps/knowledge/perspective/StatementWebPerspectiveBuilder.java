package dk.in2isoft.onlineobjects.apps.knowledge.perspective;

import org.eclipse.jdt.annotation.Nullable;
import org.onlineobjects.modules.suggestion.SuggestionsCategory;

import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.exceptions.ContentNotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.Statement;
import dk.in2isoft.onlineobjects.model.User;

public class StatementWebPerspectiveBuilder extends EntityViewPerspectiveBuilder {

	public StatementWebPerspective build(long id, Operator operator) throws EndUserException {
		@Nullable
		Statement statement = modelService.get(Statement.class, id, operator);
		if (statement == null) {
			throw new ContentNotFoundException(Statement.class, id);
		}
		StatementWebPerspective perspective = new StatementWebPerspective();
		perspective.setId(id);
		perspective.setText(statement.getText());
		SuggestionsCategory questionSuggestions = knowledgeService.suggestionsForStatement(statement, operator);
		perspective.setQuestionSuggestions(questionSuggestions);

		User user = modelService.getRequired(User.class, operator.getIdentity(), operator);
		knowledgeService.categorize(statement, perspective, user, operator);
		knowledgeService.addTags(statement, perspective, operator);
		
		return perspective;
	}
}
