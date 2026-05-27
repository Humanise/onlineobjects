package org.onlineobjects.modules.suggestion;

import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.Question;
import dk.in2isoft.onlineobjects.model.Statement;

public class Suggestions {

	private KnowledgeSuggester knowledgeSuggester;

	public SuggestionsCategory suggestionsForStatement(Statement statement, Operator operator) throws EndUserException {
		SuggestionsCategory category = knowledgeSuggester.suggestQuestionViaEmbedding(statement, operator);
		category.setDescription("Suggested questions...");
		return category;
	}

	public SuggestionsCategory suggestQuestion(String text, Operator operator) throws EndUserException {
		Statement dummy = new Statement();
		dummy.setText(text);
		return knowledgeSuggester.suggestQuestion(dummy, operator);
	}

	public void setKnowledgeSuggester(KnowledgeSuggester knowledgeSuggester) {
		this.knowledgeSuggester = knowledgeSuggester;
	}

	public SuggestionsCategory suggestStatement(Question question, Operator operator) throws EndUserException {
		SuggestionsCategory category = knowledgeSuggester.suggestStatementViaEmbedding(question, operator);
		category.setDescription("Suggested answers...");
		return category;
	}


}
