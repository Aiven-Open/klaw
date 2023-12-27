version = 2.7.0
NODE = node

# Sets a custom hook path in the local git config.
# Currently there's only a pre-commit hook related
# to changes in `/coral/*`  and `openapi.yaml`
# so it's not needed for pure backend changes.
config_hook_path:
	$(shell git config --local core.hooksPath .githooks/)
	echo "✅ Custom git hook path set!"

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

generate-toc:
	@if [ ! "$(NODE)" ]; then \
		echo "⚠️ Node.js is not installed. This command needs Node.js to run."; \
		exit 1; \
	fi
	cd coral && $(NODE) scripts/markdown-toc.cjs