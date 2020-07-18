# Clobits — Clojure ❤️ C

Use C inside Clojure, then run it on the JVM or compile a native binary.

## Features

- One source to rule them all — write code once, run it on both jvm and native binary

```clojure
(ns clobits.examples.ncurses.hello-world
  (:require [clobits.native-interop :refer [*native-image*]]
            [clobits.c :as c])
  (:gen-class))

(if *native-image*
  (do (println "In native image context")
      (require '[clobits.ncurses.ni :as ncurses]))
  (do (println "In polyglot context")
      (require '[clobits.ncurses.poly :as ncurses])))

(defn -main [& args]
  (let [w (ncurses/initscr)]
    
    (ncurses/printw (c/str* "hello"))
    (ncurses/curs-set 0)
    (ncurses/refresh)
    (Thread/sleep 1000)
    
    (ncurses/endwin)))
```

For a bigger example, check out [`wasd_rect.clj`](https://github.com/Saikyun/clobits/blob/master/examples/sdl/src/clobits/examples/sdl/wasd_rect.clj)

### Misc. features

- Parse C function prototypes to Clojure maps
- Generate .h/.c-files and Clojure namespaces based off of C function prototypes or using Clojure maps

## Project status

This project is a work in progress. I wanted to share this mostly to add another example of how to use Clojure + C using GraalVM.

I'd be very happy if you tried to use it and have questions. If you have a specific C library you want to get working, let me know. Since the generated code can be reused, it'd be nice if we could host the bits required for various C libraries at clojars. :)

### Known unknowns and possible solutions

- Calling variadic C functions that do not have a `va_args` variation (see [printf](http://www.cplusplus.com/reference/cstdio/printf/?kw=printf) vs [vprintf](http://www.cplusplus.com/reference/cstdio/vprintf/))
  - If the function has a `va_args` variant, I'd recommend just using that for now
- Sending pointer values to clojure functions doesn't work. This is because all clojure functions assume the arguments are Objects, which makes the ni compiler complain, with this message: `Expected Object but got Word for call argument`. Sadly there doesn't seem to be any practical way to tell clojure functions to accept Word-types. I've digged through deftype / gen-class and even tried modifying some of them, to no avail. The solution I came up with is wrapping all Word types (pointers) with wrapper classes. This is fine in order to get things working, but I'm not sure how performant it is.

If you know how to solve the problems below, please tell me how! :) Either through an issue, or [@Saikyun](https://twitter.com/Saikyun) on twitter.

## Prerequisites

* graalvm -- tested with `graalvm-ce-java11-20.2.0-dev`: https://github.com/graalvm/graalvm-ce-dev-builds/releases
  * download, then add the following to e.g. .zprofile
  * `export GRAALVM_HOME="/path/to/graalvm-ce-java11-20.2.0-dev/Contents/Home/"`
* install native-image: `$GRAALVM_HOME/bin/gu install native-image` (will be installed by `./compile` otherwise)
* install llvm toolchain: `$GRAALVM_HOME/bin/gu install llvm-toolchain`
* leiningen -- https://leiningen.org/

Tested on macos and linux.

## Get the source

```
git clone https://github.com/Saikyun/clobits
cd clobits
```


## Generate bindings (optional)

```
make clean bindings
```

# ncurses Example

Displays `hello` for a second.

## Run on JVM using Polyglot

```
make clean ncurses-poly
```

## Compile and run a native binary using native-image

```
make clean ncurses-ni
```

# SDL Example

## Get libSDL2

You need libSDL2 on your path, and possibly added to `"-Djava.library.path=<LIB_PATH_HERE>"` under your OS profile in `project.clj`. I've put some defaults there, but I'm not sure they're universal.

## Run on JVM using Polyglot

```
make clean sdl-poly
```

## Compile and run a native binary using native-image

```
make clean sdl-ni
```

You should see a red square on a white background.
Cmd+Q to exit on MacOS. You can also press the X.

## Errors

If you don't run `clean` when switching between poly / ni targets you can get complaints about not finding `clojure.core/seq?`. I think this has to do with how leningen caches .class-files. Just run `make clean ni / poly` and you'll be fine.

## The generated files

Running `make clean bindings` generates the folder `src/bindings`. This folder is included in the repo so that one can read the generated source when looking at github. As soon as you start using this library, you will overwrite these files. They should not be modified by hand.

In `src/bindings` you'll find:

#### sdl.c and sdl.h -- generated c code

These are necessary for polyglot to be able to call native libs.

### sdl_ni.clj

Clojure code that generates classes and interfaces to be with native-image

#### sdl_ns.clj

Clojure namespace that is used with polyglot.

### How to put it all together

Look at `src/clobits/examples/startup.clj` to see how you can use the same code for both polyglot and native-image.

## Thanks

- sogaiu -- support, testing and helpful discussion
- cornerwings -- java + native image example: https://github.com/cornerwings/graal-native-interaction
- u/duhace -- java + polyglot example: https://www.reddit.com/r/java/comments/8s7sr8/interacting_with_c_using_graalvm/
- borkdude -- help, project setup for clojure / native image compilation: https://github.com/borkdude/clj-kondo

## License

Copyright © 2020 Jona Ekenberg

Distributed under the EPL License. See LICENSE.

This project contains code from:

- Clojure, which is licensed under the same EPL License.
- clj-kondo, which is licensed under the same EPL License.
