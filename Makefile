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


poly: bindings
	lein with-profiles +runner,$(OSFLAG) do clean, run

ni: bindings
	NATIVE_IMAGE=true ./compile && LD_LIBRARY_PATH=./libs ./ncurses_example

ni2:
	NATIVE_IMAGE=true ./compile && LD_LIBRARY_PATH=./libs ./ncurses_example
