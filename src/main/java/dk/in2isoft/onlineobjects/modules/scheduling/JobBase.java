package dk.in2isoft.onlineobjects.modules.scheduling;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class JobBase implements Job {

	@Autowired
	SchedulingService scheduling;

	protected JobStatus getStatus(JobExecutionContext context) {
		return JobStatus.getOrCreate(context, scheduling, this);
	}
}
