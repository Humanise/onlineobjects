include Makefile.config

.DEFAULT_GOAL := test

ROOT_DIR:=$(shell dirname $(realpath $(lastword $(MAKEFILE_LIST))))

test:
	mvn test -q -P EssentialTests

test-all:
	mvn test -q

extract:
	mvn test -q -Dtest=TestExtractionComparison

run:
	mvn jetty:run -DskipTests=true

tomcat:
	mvn clean install -DskipTests=true
	/Users/jbm/Code/apache-tomcat-8.0.20/bin/startup.sh

install:
	mvn clean install -DskipTests=true

deploy:
	cd ${ROOT_DIR}/src/main/webapp/hui/tools/
	grunt --gruntfile ${ROOT_DIR}/src/main/webapp/hui/Gruntfile.js build
	cd ${ROOT_DIR}
	mvn clean install -DskipTests=true
	ant deploy
	rsync -r -a -v -e "ssh -l root" --delete ${ROOT_DIR}/tmp/OnlineObjects/onlineobjects ${SYNC_DIST}
	
	${SYNC_FINALLY}