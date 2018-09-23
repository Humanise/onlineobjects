package dk.in2isoft.onlineobjects.core;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SystemLoader implements ServletContextListener {

	private static Logger log = LogManager.getLogger(SystemLoader.class);

	public void contextDestroyed(ServletContextEvent event) {
	}

	public void contextInitialized(ServletContextEvent event) {
        log.info("OnlineObjects - ready for takeoff");		
	}
}
