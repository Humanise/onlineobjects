package dk.in2isoft.onlineobjects.modules.language.organic;

import dk.in2isoft.onlineobjects.core.ItemQuery;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.model.Word;
import dk.in2isoft.onlineobjects.modules.organic.Cell;

public class WordCheckingCell extends Cell {

	private String checkedScent = "word.checked";

	private ModelService modelService;

	@Override
	public void beat() {
		Operator operator = modelService.newAdminOperator();
		ItemQuery<Word> query = Query.after(Word.class).withCustomProperty("scent", checkedScent).withPaging(0, 10);
		modelService.search(query,operator);
		operator.commit();
	}

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
}
