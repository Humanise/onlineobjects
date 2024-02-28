package dk.in2isoft.onlineobjects.modules.index;

import java.util.List;

import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;

import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.Results;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.modules.scheduling.JobBase;
import dk.in2isoft.onlineobjects.modules.scheduling.JobStatus;

public class ConfigurableIndexJob<E extends Entity> extends JobBase implements InterruptableJob {
	
	private boolean interrupted;
	
	private ConfigurableIndexer<E> configurableIndexer;
	
	private ModelService modelService;
	
	private SecurityService securityService;

	public final void execute(JobExecutionContext context) throws JobExecutionException {
		JobStatus status = getStatus(context);
		Operator operator = modelService.newOperator(securityService.getAdminPrivileged());
		try {
			run(status, modelService, operator);
		} catch (Exception e) {
			status.error(e.getMessage(), e);
		} finally {
			operator.commit();
		}
	}

	private void run(JobStatus status, ModelService modelService, Operator operator) {
		try {
			status.log("Clearing index");
			configurableIndexer.clear();
		} catch (EndUserException e) {
			status.error("Error while clearing index", e);
		}
		Query<E> query = Query.of(configurableIndexer.getType());
		Long count = modelService.count(query, operator);
		status.log("Starting re-index of "+count+" items");
		int num = 0;
		int percent = -1;
		Results<E> results = modelService.scroll(query, operator);
		List<E> batch = Lists.newArrayList();
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
			E word = results.get();
			batch.add(word);
			if (batch.size() > 200) {
				configurableIndexer.index(batch);
				batch.clear();
			}
			num++;
		}
		// Index remaining
		if (!batch.isEmpty()) {
			configurableIndexer.index(batch);
		}
		results.close();
		status.log("Finished indexing");
	}

	public void interrupt() throws UnableToInterruptJobException {
		interrupted = true;
	}
	
	public void setConfigurableIndexer(ConfigurableIndexer<E> configurableIndexer) {
		this.configurableIndexer = configurableIndexer;
	}

	@Autowired
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
	
	@Autowired
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
}
