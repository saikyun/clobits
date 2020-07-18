UNAME := $(shell uname)

ifeq ($(UNAME), Linux)
	OSFLAG = +linux
endif

ifeq ($(UNAME), Darwin)
	OSFLAG = +macos
endif

clean:
	-rm sdl_example
	-rm -r example-src/*
	-rm -r target
	-rm -r libs/*
	-rm -r classes/*
	-rm -r examples/*/gen-src

dev-repl:
	REPLING=true lein with-profiles +socket,+compare-files repl

sdl-poly:
	lein with-profiles $(OSFLAG),+sdl-poly,+socket do clean, run

ncurses-poly:
	lein with-profiles $(OSFLAG),+ncurses-poly,+socket do clean, run

bounce-poly:
	lein with-profiles $(OSFLAG),+bounce-poly,+socket do clean, run

sdl-ni:
	NATIVE_IMAGE=true NI_EXAMPLE=sdl ./compile && LD_LIBRARY_PATH=./libs ./sdl_example

ncurses-ni:
	NATIVE_IMAGE=true NI_EXAMPLE=ncurses ./compile && LD_LIBRARY_PATH=./libs ./ncurses_example

bounce-ni:
	NATIVE_IMAGE=true NI_EXAMPLE=bounce ./compile && LD_LIBRARY_PATH=./libs ./bounce_example
