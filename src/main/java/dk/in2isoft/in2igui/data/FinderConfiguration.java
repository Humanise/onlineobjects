package dk.in2isoft.in2igui.data;

import java.util.ArrayList;
import java.util.List;

public class FinderConfiguration {

	private String url;
	private String title;
	private FinderList list;
	private Search search;
	private Creation creation;
	private Selection selection;
	
	public FinderConfiguration() {
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setListUrl(String listUrl) {
		if (list==null) {
			list = new FinderList();
		}
		list.setUrl(listUrl);
	}

	public void setSearchParameter(String searchParameter) {
		if (this.search == null) {
			search = new Search();
		}
		this.search.setParameter(searchParameter);
	}

	public Search getSearch() {
		return search;
	}
	
	public FinderList getList() {
		return list;
	}
	
	public Creation getCreation() {
		return creation;
	}
	
	public void setCreation(Creation creation) {
		this.creation = creation;
	}

	public Selection getSelection() {
		return selection;
	}

	public void setSelection(Selection selection) {
		this.selection = selection;
	}

	public class FinderList {
		private String url;

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}
	}
	
	public class Search {
		private String parameter;

		public String getParameter() {
			return parameter;
		}

		public void setParameter(String parameter) {
			this.parameter = parameter;
		}
	}

	public class Selection {
		private String value;
		private String url;
		private String parameter;
		private List<ItemData> items = new ArrayList<>();

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getParameter() {
			return parameter;
		}

		public void setParameter(String parameter) {
			this.parameter = parameter;
		}

		public List<ItemData> getItems() {
			return items;
		}

		public void setItems(List<ItemData> items) {
			this.items = items;
		}

		public void addItem(ItemData item) {
			items.add(item);
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	public class Creation {
		private String url;
		private String button;
		private Object formula;

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getButton() {
			return button;
		}

		public void setButton(String button) {
			this.button = button;
		}

		public Object getFormula() {
			return formula;
		}

		public void setFormula(Object formula) {
			this.formula = formula;
		}

	}

	public Creation addCreation() {
		creation = new Creation();
		return creation;
	}

	public Selection addSelection() {
		selection = new Selection();
		return selection;
	}
}
