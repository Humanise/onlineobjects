package dk.in2isoft.onlineobjects.services;

import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.LogEntry;
import dk.in2isoft.onlineobjects.model.LogLevel;
import dk.in2isoft.onlineobjects.model.LogType;
import dk.in2isoft.onlineobjects.modules.surveillance.SurveillanceService;

public class LifeCycleService implements ApplicationListener<ApplicationContextEvent> {

	private static Logger log = LogManager.getLogger(LifeCycleService.class);
	private ConsistencyService consistencyService;
	private ModelService modelService;
	private SurveillanceService surveillanceService;

	private Date startTime;
	
	public LifeCycleService() {
		startTime = new Date();
	}
	
	@Override
	public void onApplicationEvent(ApplicationContextEvent event) {
		if (event instanceof ContextRefreshedEvent) {
			surveillanceService.audit().info("Application launched");
			log.info("The context has started!");
			try {
				consistencyService.check();
			} catch (EndUserException e) {
				log.error("Failed checking consistency", e);
			}
			LogEntry entry = new LogEntry();
			entry.setLevel(LogLevel.info);
			entry.setTime(new Date());
			entry.setType(LogType.startUp);
			modelService.create(entry);
			modelService.commit();
		} else {
			surveillanceService.audit().info("Event: {}", event.getClass().getSimpleName());
		}
	}
	
	public Date getStartTime() {
		return startTime;
	}
	
	public void setConsistencyService(ConsistencyService consistencyService) {
		this.consistencyService = consistencyService;
	}
	
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
	
	public void setSurveillanceService(SurveillanceService surveillanceService) {
		this.surveillanceService = surveillanceService;
	}
}
