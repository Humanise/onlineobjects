package org.onlineobjects.modules.suggestion;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;

import dk.in2isoft.onlineobjects.modules.scheduling.JobStatus;
import dk.in2isoft.onlineobjects.modules.scheduling.ServiceBackedJob;

@DisallowConcurrentExecution
public class KnowledgeSuggestionJob extends ServiceBackedJob implements InterruptableJob {

	private JobStatus status;

	public void execute(JobExecutionContext context) throws JobExecutionException {
		status = getStatus(context);
		status.setProgress(0);
		schedulingSupportFacade.getKnowledgeSuggester().beat();
		status.setProgress(1);
	}

	public void interrupt() throws UnableToInterruptJobException {
		status.interrupt();
	}

}
