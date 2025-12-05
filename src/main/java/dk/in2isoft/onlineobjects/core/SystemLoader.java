package dk.in2isoft.onlineobjects.core;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.jcs.JCS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SystemLoader implements ServletContextListener {

	private static Logger log = LogManager.getLogger(SystemLoader.class);

	public void contextDestroyed(ServletContextEvent event) {
        log.info("Shutting down");
		JCS.shutdown();
	}

	public void contextInitialized(ServletContextEvent event) {
        log.info("OnlineObjects - ready for takeoff");
	}
}
