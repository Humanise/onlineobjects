package dk.in2isoft.onlineobjects.core;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

public class SystemLoader implements ServletContextListener {

	private static Logger log = Logger.getLogger(SystemLoader.class);

	public void contextDestroyed(ServletContextEvent event) {
	}

	public void contextInitialized(ServletContextEvent event) {
        log.info("OnlineObjects - ready for takeoff");		
	}
}
