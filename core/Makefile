CORAL_ROOT = ../coral
CORAL_DIST = $(CORAL_ROOT)/dist

# Make sure pnpm is pre installed
PNPM = pnpm

$(CORAL_DIST): ../coral/index.html $(shell find $(CORAL_ROOT)/src)
	cd $(CORAL_ROOT); $(PNPM) install --frozen-lockfile
	cd $(CORAL_ROOT); $(PNPM) build --assetsDir assets/coral --mode springboot
