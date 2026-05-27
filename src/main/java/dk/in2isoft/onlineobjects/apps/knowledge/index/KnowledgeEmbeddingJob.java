package dk.in2isoft.onlineobjects.apps.knowledge.index;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.onlineobjects.modules.index.EntityEmbedder;
import org.onlineobjects.modules.intelligence.EmbeddingInfo;
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
		Duration delay = intelligence.getSearchEmbeddingModel().orElseThrow().getRequestDelay();

		try (Operator operator = model.newAdminOperator()) {
			var types = List.of(Statement.class, Question.class);
			int total = 0;
			int current = 0;
			for (Class<? extends Entity> type : types) {
				total += model.count(Query.after(type), operator);
			}
			for (Class<? extends Entity> type : types) {
				Results<? extends Entity> scroll = model.scroll(Query.after(type), operator);
				while (scroll.next() && !interrupted) {
					try {
						current++;
						Entity entity = scroll.get();
						Optional<EmbeddingInfo> info = embedder.embed(entity, delay);
						if (info.isEmpty()) {
							status.warn("Missing embedding, trying again");
							try {
								// Wait for 60 secs
								Thread.sleep(1000 * 60);
							} catch (InterruptedException ignore) {}
							Optional<EmbeddingInfo> again = embedder.embed(entity);
							if (again.isEmpty()) {
								status.error("Unable to create embedding af retry");
							}
						} else {
							status.log("Embedding created for: " + entity.getName());
						}
						status.setProgress(current, total);
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
