UNAME := $(shell uname)

ifeq ($(UNAME), Linux)
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
	-rm -r classes/bindings
	-rm -r java-src/bindings

get-ni:
	$(GRAALVM_HOME)/bin/gu install native-image || true

info:
	native-image --expert-options-all

sdl-bindings:
	lein exec -ep "(require '[clobits.examples.sdl.create-sdl-lib]) (clobits.examples.sdl.create-sdl-lib/-main) (shutdown-agents)"
	lein with-profiles +compile-sdl compile

ncurses-bindings:
	lein exec -ep "(require '[clobits.examples.ncurses.create-ncurses-lib]) (clobits.examples.ncurses.create-ncurses-lib/-main) (shutdown-agents)"

bindings:
	lein exec -ep "(require '[clobits.examples.sdl.create-sdl-lib]) (clobits.examples.sdl.create-sdl-lib/-main) (require '[clobits.examples.ncurses.create-ncurses-lib]) (clobits.examples.ncurses.create-ncurses-lib/-main) (shutdown-agents)"
	lein with-profiles +compile-sdl compile

sdl-poly: bindings
	lein with-profiles $(OSFLAG),+sdl-poly,+socket do clean, run

sp:
	lein with-profiles $(OSFLAG),+sdl-poly,+socket do clean, run


ncurses-poly: bindings
	lein with-profiles $(OSFLAG),+ncurses-poly do clean, run

bounce-poly: bindings
	lein with-profiles $(OSFLAG),+bounce-poly do clean, run

sdl-ni: bindings
	NATIVE_IMAGE=true NI_EXAMPLE=sdl ./compile && LD_LIBRARY_PATH=./libs ./sdl_example

sni:
	NATIVE_IMAGE=true NI_EXAMPLE=sdl ./compile && LD_LIBRARY_PATH=./libs ./sdl_example

ncurses-ni: bindings
	NATIVE_IMAGE=true NI_EXAMPLE=ncurses ./compile && LD_LIBRARY_PATH=./libs ./ncurses_example

bounce-ni: bindings
	NATIVE_IMAGE=true NI_EXAMPLE=bounce ./compile && LD_LIBRARY_PATH=./libs ./bounce_example

bni:
	NATIVE_IMAGE=true NI_EXAMPLE=bounce ./compile && LD_LIBRARY_PATH=./libs ./bounce_example
