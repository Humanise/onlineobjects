package dk.in2isoft.onlineobjects.apps.knowledge.index;

import java.util.List;

import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;

import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.core.SimpleOperator;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.modules.scheduling.JobStatus;
import dk.in2isoft.onlineobjects.modules.scheduling.ServiceBackedJob;

public class KnowledgeIndexJob extends ServiceBackedJob implements InterruptableJob {
	
	private boolean interrupted;

	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		KnowledgeIndexer indexer = schedulingSupportFacade.getKnowledgeIndexer();
		
		JobStatus status = getStatus(context);

		ModelService modelService = schedulingSupportFacade.getModelService();
		SecurityService securityService = schedulingSupportFacade.getSecurityService();
		
		SimpleOperator operator = new SimpleOperator(securityService.getAdminPrivileged(), modelService);
		List<User> users = modelService.list(Query.of(User.class), operator);
		status.log("Starting re-index of knowledge");
		int num=0;	
		for (User user : users) {
			if (!interrupted && !securityService.isCoreUser(user)) {
				try {
					status.log("Re-indexing: " + user.getUsername());
					indexer.reIndex(operator.as(user));
					status.log("Done re-indexing: " + user.getUsername());
				} catch (EndUserException e) {
					status.error("Problem while re-indexing", e);
				} finally {
					operator.commit();
				}
			}
			status.setProgress(num, users.size());
			num++;
		}
		status.log("Done re-indexing");
		operator.commit();
	}

	public void interrupt() throws UnableToInterruptJobException {
		interrupted = true;
	}

}
