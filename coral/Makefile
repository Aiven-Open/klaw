CORAL_DIST_PATH = coral/dist

default:
	@echo "TODO: ADD USAGE INSTRUCTIONS"

$(CORAL_DIST_PATH): coral/index.html coral/src/*
	pnpm  --prefix coral build --assetsDir assets/coral --mode springboot

src/main/resources/templates/coral/index.html: $(CORAL_DIST_PATH)
	mkdir -p $(shell dirname $@)
	cp -r $(CORAL_DIST_PATH)/index.html $@

src/main/resources/static/assets/coral: $(CORAL_DIST_PATH)
	cp -r $(CORAL_DIST_PATH)/assets/* $@

.PHONY: enable-coral-in-springboot
enable-coral-in-springboot: src/main/resources/templates/coral/index.html src/main/resources/static/assets/coral
	sed -i "" 's/klaw\.coral\.enabled=false/klaw\.coral\.enabled=true/' src/main/resources/application.properties

.PHONY: disable-coral-in-springboot
disable-coral-in-springboot:
	sed -i "" 's/klaw\.coral\.enabled=true/klaw\.coral\.enabled=false/' src/main/resources/application.properties

clean:
	rm -rf src/main/resources/templates/coral
	rm -rf src/main/resources/static/assets/coral
