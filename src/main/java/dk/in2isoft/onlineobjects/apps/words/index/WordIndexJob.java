package dk.in2isoft.onlineobjects.apps.words.index;

import java.util.List;

import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;

import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.modules.index.WordIndexer;
import dk.in2isoft.onlineobjects.modules.language.WordListPerspective;
import dk.in2isoft.onlineobjects.modules.language.WordListPerspectiveQuery;
import dk.in2isoft.onlineobjects.modules.scheduling.JobStatus;
import dk.in2isoft.onlineobjects.modules.scheduling.ServiceBackedJob;

public class WordIndexJob extends ServiceBackedJob implements InterruptableJob {
	
	private boolean interrupted;

	public void execute(JobExecutionContext context) throws JobExecutionException {
		newSchool(context);
	}

	private void newSchool(JobExecutionContext context) {
		JobStatus status = getStatus(context);
		WordIndexer wordIndexer = schedulingSupportFacade.getWordIndexer();
		ModelService modelService = schedulingSupportFacade.getModelService();
		/*
		try {
			status.log("Clearing index");
			wordIndexer.clear();
		} catch (EndUserException e) {
			status.error("Error while clearing index", e);
		}*/
		
		//Query.after(Word.class).withCustomProperty("common.source", Comparison.LIKE, "http://www.wordnet.dk/%");
		Operator operator = modelService.newAdminOperator();
		WordListPerspectiveQuery query = new WordListPerspectiveQuery().orderByUpdated();
		//query.withWord("a-321");
		int total = modelService.count(query, operator);
		int pageSize = 500;
		int pages = (int) Math.ceil((double)total/(double)pageSize);

		try {
			for (int i = 0; i < pages; i++) {
				query.withPaging(i, pageSize);
				List<WordListPerspective> list = modelService.search(query, operator).getList();
				wordIndexer.indexWordPerspectives(list);
				status.setProgress(i, pages);
				if (interrupted) {
					status.log("Interrupted");
					break;
				}
			}
			operator.commit();
		} catch (ModelException e) {
			status.error("Error while fetching words", e);
			operator.rollBack();
		}
	}
/*
	private void oldSchool(JobExecutionContext context) {
		JobStatus status = getStatus(context);
		WordIndexer wordIndexer = schedulingSupportFacade.getWordIndexer();
		ModelService modelService = schedulingSupportFacade.getModelService();
		
		try {
			status.log("Clearing index");
			wordIndexer.clear();
		} catch (EndUserException e) {
			status.error("Error while clearing index", e);
		}
		Query<Word> query = Query.of(Word.class);
		Long count = modelService.count(query);
		status.log("Starting re-index of "+count+" words");
		int num = 0;
		int percent = -1;
		Results<Word> results = modelService.scroll(query);
		List<Word> batch = Lists.newArrayList();
		while (results.next()) {
			if (interrupted) {
				status.log("Interrupting indexing");
				break;
			}
			
			int newPercent = Math.round(((float)num)/(float)count*100);
			if (newPercent>percent) {
				percent = newPercent;
				status.setProgress(num, count.intValue());
			}
			Word word = results.get();
			batch.add(word);
			if (batch.size()>200) {
				wordIndexer.indexWords(batch);
				batch.clear();
				modelService.clearAndFlush();
			}
			num++;
		}
		results.close();
		status.log("Finished indexing words");
	}*/

	public void interrupt() throws UnableToInterruptJobException {
		interrupted = true;
	}

}
