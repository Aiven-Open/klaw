CORAL_ROOT = ../coral
CORAL_DIST = $(CORAL_ROOT)/dist

# This is installed as part of Maven
PNPM = nodebuild/node/node_modules/.bin/pnpm

$(CORAL_DIST): ../coral/index.html $(shell find $(CORAL_ROOT)/src)
	$(PNPM) -C $(CORAL_ROOT) build --assetsDir assets/coral --mode springboot

src/main/resources/templates/coral/index.html: $(CORAL_DIST)
	mkdir -p $(shell dirname $@)
	cp -r $(CORAL_DIST)/index.html $@

src/main/resources/static/assets/coral: $(CORAL_DIST)
	cp -r $(CORAL_DIST)/assets/* $@

.PHONY: clean
clean:
	rm -rf src/main/resources/templates/coral/index.html
	rm -rf src/main/resources/static/assets/coral