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

bindings:
	lein exec -ep "(require '[clobits.examples.sdl.create-sdl-lib]) (clobits.examples.sdl.create-sdl-lib/-main)"
	-rm -r target

poly: bindings
	lein with-profiles +runner,$(OSFLAG) do clean, run

ni: bindings
	NATIVE_IMAGE=true ./compile && LD_LIBRARY_PATH=./libs ./sdl_example
