package org.onlineobjects.modules.database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dk.in2isoft.onlineobjects.core.ModelService;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;

public class Migrator {

	private static Logger log = LogManager.getLogger(Migrator.class);

	private ModelService modelService;

	public void migrate() {
		SpringLiquibase liquibase = new SpringLiquibase();
	    liquibase.setChangeLog("classpath:database/changelog.xml");
	    liquibase.setDataSource(modelService.getDataSource());
	    try {
			liquibase.afterPropertiesSet();
		} catch (LiquibaseException e) {
			log.error("Unable to migrate database", e);
		}
	}

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
}
