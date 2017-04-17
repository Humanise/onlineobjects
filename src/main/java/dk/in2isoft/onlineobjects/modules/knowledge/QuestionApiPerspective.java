package dk.in2isoft.onlineobjects.modules.knowledge;

import java.util.List;

public class QuestionApiPerspective {

	private String text;
	private long id;

	private List<StatementApiPerspective> answers;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public List<StatementApiPerspective> getAnswers() {
		return answers;
	}
	
	public void setAnswers(List<StatementApiPerspective> answers) {
		this.answers = answers;
	}
}
