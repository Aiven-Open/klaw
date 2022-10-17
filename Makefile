
all: klaw api

klaw:
	mvn clean verify

api:
	cd cluster-api && mvn clean install
	cd cluster-api && mvn clean package

edit-config:
	${EDITOR} target/classes/application.properties

edit-api-config:
	${EDITOR} cluster-api/target/classes/application.properties

run:
	java -jar target/klaw-1.0.0.jar

run-api:
	java -jar cluster-api/target/klaw-clusterapi-1.0.0.jar --spring.config.location=cluster-api/target/classes/application.properties
