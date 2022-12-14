ROOT = ../
CORAL_DIST_PATH = dist


EMBEDDED_INDEX_HTML = $(ROOT)core/src/main/resources/templates/coral/index.html
EMBEDDED_CORAL_ASSETS = $(ROOT)core/src/main/resources/static/assets/coral
KLAW_APPLICATION_PROPERTIES_PATH = $(ROOT)core/src/main/resources/application.properties

default: $(EMBEDDED_INDEX_HTML)
	@echo "TODO: ADD USAGE INSTRUCTIONS"

$(CORAL_DIST_PATH): index.html $(wildcard src/**/*)
	pnpm build --assetsDir assets/coral --mode springboot

$(EMBEDDED_INDEX_HTML): $(CORAL_DIST_PATH)
	mkdir -p $(shell dirname $@)
	cp -r $(CORAL_DIST_PATH)/index.html $@

$(EMBEDDED_CORAL_ASSETS): $(CORAL_DIST_PATH)
	cp -r $(CORAL_DIST_PATH)/assets/* $@

.PHONY: enable-coral-in-springboot
enable-coral-in-springboot: $(EMBEDDED_INDEX_HTML) $(EMBEDDED_CORAL_ASSETS)
	sed -i "" 's/klaw\.coral\.enabled=false/klaw\.coral\.enabled=true/' $(KLAW_APPLICATION_PROPERTIES_PATH)

.PHONY: disable-coral-in-springboot
disable-coral-in-springboot:
	sed -i "" 's/klaw\.coral\.enabled=true/klaw\.coral\.enabled=false/' $(KLAW_APPLICATION_PROPERTIES_PATH)

clean:
	rm -rf $(CORAL_DIST_PATH)
	rm -rf $(EMBEDDED_INDEX_HTML)
	rm -rf $(EMBEDDED_CORAL_ASSETS)