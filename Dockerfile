FROM tomcat:11.0-jre21

LABEL maintainer="jonas@humanise.dk"

ADD target/OnlineObjects.war /usr/local/tomcat/webapps/

EXPOSE 8080

CMD ["catalina.sh", "run"]