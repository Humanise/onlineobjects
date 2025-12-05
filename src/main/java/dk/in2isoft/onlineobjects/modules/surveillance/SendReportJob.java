package dk.in2isoft.onlineobjects.modules.surveillance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.modules.scheduling.JobStatus;
import dk.in2isoft.onlineobjects.modules.scheduling.ServiceBackedJob;

@DisallowConcurrentExecution
public class SendReportJob extends ServiceBackedJob {

	private static Logger log = LogManager.getLogger(SendReportJob.class);

	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobStatus status = getStatus(context);
		try {
			status.log("Sending report");
			schedulingSupportFacade.getSurveillanceService().sendReport();
			status.log("The report was sent");
		} catch (EndUserException e) {
			status.error(e.getMessage());
			log.error(e);
			throw new JobExecutionException(e);
		}
	}

}
