version = 2.0.0

build_all: klaw_core cluster_api

klaw_core:
	cd core && mvn clean verify

cluster_api:
	cd cluster-api && mvn clean verify

edit-core-config:
	${EDITOR} core/target/classes/application.properties

edit-cluster-api-config:
	${EDITOR} cluster-api/target/classes/application.properties

run-core:
	java -jar core/target/klaw-$(version).jar

run-cluster-api:
	java -jar cluster-api/target/cluster-api-$(version).jar --spring.config.location=cluster-api/target/classes/application.properties