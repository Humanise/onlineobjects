package dk.in2isoft.onlineobjects.modules.user;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.modules.scheduling.ServiceBackedJob;

public class UserHealthCheckJob extends ServiceBackedJob {

	public static final String USER_ID = "userId";
	private static final Logger log = LogManager.getLogger(UserHealthCheckJob.class);
	public static final String NAME = "user-health-check";
	public static final String GROUP = "core";
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap map = context.getMergedJobDataMap();
		log.info(map.toString());
		long userId = map.getLong(USER_ID);
		if (userId > 0) {
			Operator adminOperator = schedulingSupportFacade.getModelService().newAdminOperator();
			try {
				schedulingSupportFacade.getMemberService().checkUserHealth(userId, adminOperator);
				adminOperator.commit();
			} catch (EndUserException e) {
				adminOperator.rollBack();
				throw new JobExecutionException(e);
			}
		}
	}
}
