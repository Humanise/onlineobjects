package dk.in2isoft.onlineobjects.apps.knowledge.index;

import java.util.List;

import org.onlineobjects.modules.index.EntityEmbedder;
import org.onlineobjects.modules.intelligence.Intelligence;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.springframework.beans.factory.annotation.Autowired;

import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.Results;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.Question;
import dk.in2isoft.onlineobjects.model.Statement;
import dk.in2isoft.onlineobjects.modules.scheduling.JobBase;
import dk.in2isoft.onlineobjects.modules.scheduling.JobStatus;

public class KnowledgeEmbeddingJob extends JobBase implements InterruptableJob {

	@Autowired
	ModelService model;

	@Autowired
	Intelligence intelligence;

	@Autowired
	EntityEmbedder embedder;

	private boolean interrupted;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobStatus status = getStatus(context);
		status.log("Starting embedding");
		try (Operator operator = model.newAdminOperator()) {
			var types = List.of(Statement.class, Question.class);
			for (Class<? extends Entity> type : types) {
				Results<? extends Entity> scroll = model.scroll(Query.after(type), operator);
				while (scroll.next() && !interrupted) {
					try {
						Entity entity = scroll.get();
						embedder.embed(entity);
						status.log("Embedding created for: " + entity.getName());
					} catch (Exception e) {
						status.error(e.getMessage(), e);
					}
				}
			}
		}
	}

	@Override
	public void interrupt() throws UnableToInterruptJobException {
		interrupted = true;
	}
}
