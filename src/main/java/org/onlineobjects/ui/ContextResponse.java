package org.onlineobjects.ui;

import java.util.ArrayList;
import java.util.List;

public class ContextResponse {

	private Info info = new Info();
	private List<Action> actions = new ArrayList<>();

	public void setTitle(String title) {
		info.title = title;
	}

	public void setText(String text) {
		info.text = text;
	}

	class Info {
		String title;
		String text;
	}

	public static class Action {
		String text;
		String url;

		public Action text(String text) {
			this.text = text;
			return this;
		}

		public Action url(String url) {
			this.url = url;
			return this;
		}
	}

	public Action addAction() {
		Action a = new Action();
		this.actions.add(a);
		return a;
	}
}
