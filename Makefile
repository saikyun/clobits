UNAME := $(shell uname)

ifeq ($(UNAME_S), Linux)
	OSFLAG = +linux
endif

ifeq ($(UNAME), Darwin)
	OSFLAG = +macos
endif

clean:
	-rm sdl_example
	-rm -r src/bindings
	-rm -r target
	-rm -r libs/*

info:
	native-image --expert-options-all

sdl-bindings:
	lein exec -ep "(require '[clobits.examples.sdl.create-sdl-lib]) (clobits.examples.sdl.create-sdl-lib/-main)"
	-rm -r target

ncurses-bindings:
	lein exec -ep "(require '[clobits.examples.ncurses.create-ncurses-lib]) (clobits.examples.ncurses.create-ncurses-lib/-main)"
	-rm -r target

bindings: sdl-bindings ncurses-bindings

sdl-poly: bindings
	lein with-profiles $(OSFLAG),+sdl-poly do clean, run

ncurses-poly: bindings
	lein with-profiles $(OSFLAG),+ncurses-poly do clean, run

sdl-ni: bindings
	NATIVE_IMAGE=true NI_EXAMPLE=sdl ./compile && LD_LIBRARY_PATH=./libs ./sdl_example

ncurses-ni: bindings
	NATIVE_IMAGE=true NI_EXAMPLE=ncurses ./compile && LD_LIBRARY_PATH=./libs ./ncurses_example
