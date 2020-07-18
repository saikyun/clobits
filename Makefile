JAVA_HOME := $(GRAALVM_HOME)
PATH := $(GRAALVM_HOME)/bin:$(shell echo $$PATH)
LLVM_TOOLCHAIN := $(shell $(GRAALVM_HOME)/bin/lli --print-toolchain-path)
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

sdl-poly: assert-graal assert-clang
	LLVM_TOOLCHAIN=${LLVM_TOOLCHAIN} lein with-profiles $(OSFLAG),+sdl-poly,+socket do clean, run

ncurses-poly: assert-graal assert-clang
	LLVM_TOOLCHAIN=${LLVM_TOOLCHAIN} lein with-profiles $(OSFLAG),+ncurses-poly,+socket do clean, run

bounce-poly: assert-graal assert-clang
	LLVM_TOOLCHAIN=${LLVM_TOOLCHAIN} lein with-profiles $(OSFLAG),+bounce-poly,+socket do clean, run

sdl-ni: assert-graal assert-clang
	NATIVE_IMAGE=true NI_EXAMPLE=sdl ./compile && LD_LIBRARY_PATH=./libs ./sdl_example

ncurses-ni: assert-graal assert-clang
	NATIVE_IMAGE=true NI_EXAMPLE=ncurses ./compile && LD_LIBRARY_PATH=./libs ./ncurses_example

bounce-ni: assert-graal assert-clang
	NATIVE_IMAGE=true NI_EXAMPLE=bounce ./compile && LD_LIBRARY_PATH=./libs ./bounce_example

assert-graal:
	@ if [ "${GRAALVM_HOME}" = "" ]; then \
		echo "\n  Error: You must set or pass in the GRAALVM_HOME environment variable.\n"; \
		exit 1; \
	fi

assert-clang:
	${LLVM_TOOLCHAIN}/clang --version

