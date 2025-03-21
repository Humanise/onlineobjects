package dk.in2isoft.onlineobjects.modules.scheduling;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.utils.Key;

public class JobStatus {
	
	private Logger log;
	private float progress;
	private SchedulingService schedulingService;
	private Key<?> key;
	private boolean interrupted;

	public float getProgress() {
		return progress;
	}
	
	public boolean isInterrupted() {
		return interrupted;
	}

	public void setProgress(int index, int total) {
		setProgress(((float) index+1)/((float) total));
	}

	public void setProgress(int index, long total) {
		setProgress(((float) index+1)/((float) total));
	}

	public void setProgress(float progress) {
		this.progress = progress;
		String text = "Progress: "+(Math.round(progress*1000)/10.0)+"%";
		schedulingService.log(text,key);
		log.warn(text+" - "+key.getName()+" : "+key.getGroup());
	}
	
	public void log(String string) {
		schedulingService.log(string, key);
		log.info(string+" - "+key.getName()+" : "+key.getGroup());
	}

	public void warn(String string) {
		schedulingService.warn(string, key);
		log.warn(string+" - "+key.getName()+" : "+key.getGroup());
	}

	public void error(String string) {
		schedulingService.error(string, key);
		log.error(string+" - "+key.getName()+" : "+key.getGroup());
	}

	public void error(String string, Exception e) {
		schedulingService.error(string, key);
		log.error(string+" - "+key.getName()+" : "+key.getGroup(), e);
	}
	
	public void setKey(Key<?> key) {
		this.key = key;
	}
	
	public void setLog(Logger log) {
		this.log = log;
	}

	public void interrupt() {
		this.interrupted = true;
	}

	public static JobStatus get(JobExecutionContext context) {
		return (JobStatus) context.get("status");
	}

	public static JobStatus getOrCreate(JobExecutionContext context, SchedulingService schedulingService, Object initiator) {
		JobStatus jobStatus = get(context);
		if (jobStatus==null) {
			jobStatus = new JobStatus();
			jobStatus.schedulingService = schedulingService;
			jobStatus.log = LogManager.getLogger(initiator.getClass());
			jobStatus.key = context.getJobDetail().getKey();
			context.put("status", jobStatus);
		}
		return jobStatus;
	}
	
	public void setSchedulingService(SchedulingService schedulingService) {
		this.schedulingService = schedulingService;
	}

}
