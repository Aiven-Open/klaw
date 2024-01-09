ifeq ($(OS),Windows_NT)
    unix_shell := $(shell sh -c uname -s 2>NUL || echo 'false')
    # If unix shell is 'false' that means uname doesn't exist and so is not a unix shell
    ifeq ($(unix_shell), 'false')
      all: WIN
    else
      all: UNIX
    endif
else
    all: UNIX
endif

CORAL_ROOT = ../coral
CORAL_DIST = $(CORAL_ROOT)/dist
CORAL_ROOT_WIN = ..\coral
CORAL_DIST_WIN = $(CORAL_ROOT_WIN)\dist

CORAL_ROOT_WIN = ..\coral
CORAL_DIST_WIN = $(CORAL_ROOT)\dist

# Make sure pnpm is pre installed
PNPM = pnpm
VERSION = $(shell $(PNPM) -v)
    # is Windows_NT on XP, 2000, 7, Vista, 10...
WIN: ..\coral\index.html $(shell cd $(CORAL_ROOT_WIN)/src && dir /s /b /o:gn)
	@echo "Windows Coral Build"
	cd $(CORAL_ROOT_WIN) && $(PNPM) install --frozen-lockfile
	cd $(CORAL_ROOT_WIN) && $(PNPM) build --assetsDir assets\coral --mode springboot

UNIX: ../coral/index.html $(shell find $(CORAL_ROOT)/src)
	@echo "Linux Coral Build"
	cd $(CORAL_ROOT); $(PNPM) install --frozen-lockfile
	cd $(CORAL_ROOT); $(PNPM) build --assetsDir assets/coral --mode springboot