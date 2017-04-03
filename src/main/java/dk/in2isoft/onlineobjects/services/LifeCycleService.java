package dk.in2isoft.onlineobjects.services;

import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;

public class LifeCycleService implements ApplicationListener<ApplicationContextEvent> {

	private static Logger log = Logger.getLogger(LifeCycleService.class);
	private ConsistencyService consistencyService;

	private Date startTime;
	
	public LifeCycleService() {
		startTime = new Date();
	}
	
	@Override
	public void onApplicationEvent(ApplicationContextEvent event) {
		if (event instanceof ContextRefreshedEvent) {
			log.info("The context has started!");
			try {
				consistencyService.check();
			} catch (EndUserException e) {
				log.error("Failed checking consistency", e);
			}
		}
	}
	
	public Date getStartTime() {
		return startTime;
	}
	
	public void setConsistencyService(ConsistencyService consistencyService) {
		this.consistencyService = consistencyService;
	}
}
