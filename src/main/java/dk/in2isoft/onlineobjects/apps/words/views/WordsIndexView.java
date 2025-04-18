package dk.in2isoft.onlineobjects.apps.words.views;

import java.util.List;

import org.apache.commons.lang.math.NumberUtils;

import com.google.common.collect.Lists;

import dk.in2isoft.commons.jsf.AbstractView;
import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.SearchResult;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.modules.language.WordListPerspective;
import dk.in2isoft.onlineobjects.modules.language.WordListPerspectiveQuery;
import dk.in2isoft.onlineobjects.ui.Request;
import dk.in2isoft.onlineobjects.ui.jsf.model.Option;

public class WordsIndexView extends AbstractView {

	private static final int PAGING = 10;
	private ModelService modelService;
	
	private List<WordListPerspective> list;
	private static List<Option> alphabeth;
	static {
		alphabeth = Lists.newArrayList();
		for (String character : Strings.ALPHABETH) {
			alphabeth.add(new Option(character, character));
		}
		alphabeth.add(new Option("_","&"));
	}
	private int count;
	private int page;
	private String character;
	
	private int pageSize = 20;
	private List<Option> pages;

	@Override
	protected void before(Request request) throws Exception {

		String[] localPath = request.getLocalPath();
		if (localPath.length>2) {
			character = request.getLocalPath()[2];
		}
		page = 0;
		if (localPath.length>3) {
			page = Math.max(0, NumberUtils.toInt(localPath[3])-1);
		}
		WordListPerspectiveQuery query = new WordListPerspectiveQuery().withPaging(page, 20);
		if ("_".equals(character)) {
			query.startingWithSymbol();
		} else {
			query.startingWith(character);
		}
		query.orderByText();
		SearchResult<WordListPerspective> result = modelService.search(query, request);
		this.list = result.getList();
		this.count = result.getTotalCount();
		String lang = request.getLanguage();
		pages = Lists.newArrayList();
		int pageCount = (int) Math.ceil(count / pageSize) + 1;
		if (pageCount>1) {
		
			int min = Math.max(1,page-PAGING);
			int max = Math.min(pageCount, page+PAGING);
			if (min>1) {
				pages.add(buildOption(1, lang));
			}
			if (min>2) {
				pages.add(null);
			}
			for (int i = min; i <= max; i++) {
				pages.add(buildOption(i, lang));
			}
			if (max<pageCount-1) {
				pages.add(null);
			}
			if (max<pageCount) {
				pages.add(buildOption(pageCount, lang));
			}
		}}
	
	public List<WordListPerspective> getList() throws ModelException {
		return this.list;
	}
	
	public List<Option> getPages() {
		return pages;
	}
	
	private Option buildOption(int num, String language) {
		Option option = new Option();
		option.setValue("/"+language+"/index/"+character+"/"+num);
		option.setLabel(num+"");
		option.setSelected(page==num-1);
		return option;
	}
	
	public int getCount() {
		return count;
	}
	
	public String getCharacter() {
		return character;
	}
	
	public List<Option> getAlphabeth() {
		return alphabeth;
	}
	
	// Wiring...
	
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

}
