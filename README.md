# Clobits — Clojure ❤️ bits

Use C inside Clojure, then run it on the JVM or compile a native binary.

## Features

- One source to rule them all — write code once, run it on both jvm and native binary

```
(ns clobits.examples.sdl.startup
  (:require [clobits.native-interop :refer [*native-image*]])
  (:gen-class))

(if *native-image*
  (do (println "In native image context")
      (require '[bindings.sdl_ni])
      (import '[bindings sdl]))
  (do (println "In polyglot context")
      (require '[bindings.sdl-ns :as sdl])))

(defn -main [& args]
  (sdl/init (sdl/get-sdl-init-video)))
```

For a more complete example, check out `src/clobits/examples/sdl/startup.clj`

### Misc. features

- Parse C function prototypes to Clojure maps
- Generate .h/.c-files and Clojure namespaces based off of C function prototypes or using Clojure maps

## Project status

This project is a work in progress. I wanted to share this mostly to add another example of how to use Clojure + C using GraalVM.

I'd be very happy if you tried to use it and have questions. If you have a specific C library you want to get working, let me know. Since the generated code can be reused, it'd be nice if we could host the bits required for various C libraries at clojars. :)

## Prerequisites

* graalvm -- tested with `graalvm-ce-java11-20.2.0-dev`: https://github.com/graalvm/graalvm-ce-dev-builds/releases
  * download, then add the following to e.g. .zprofile
  * `export GRAALVM_HOME = "/path/to/graalvm-ce-java11-20.2.0-dev/Contents/Home/"`
  * `export JAVA_HOME = $GRAALVM_HOME`
* install native-image: `$GRAALVM_HOME/bin/gu install native-image` (will be installed by `./compile` otherwise)
* llvm toolchain -- https://www.graalvm.org/docs/reference-manual/languages/llvm/
  * export LLVM_TOOLCHAIN as in the instructions
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

This creates the folder `src/bindings`, in which you'll find:

### Generated files

These are included in the repo so that one can read the generated source when looking at github. As soon as you start using this library, you will overwrite these files. They should not be modified by hand.

#### sdl.c and sdl.h -- generated c code

These are necessary for polyglot to be able to call native libs.

### sdl_ni.clj

Clojure code that generates classes and interfaces to be with native-image

#### sdl_ns.clj

Clojure namespace that is used with polyglot.

### How to put it all together

Look at `src/clobits/examples/startup.clj` to see how you can use the same code for both polyglot and native-image.

## Running the SDL example

You need libSDL2 on your path, and possibly added to `"-Djava.library.path=<LIB_PATH_HERE>"` in `project.clj`.

## Run on JVM using Polyglot

```
make clean poly
```

## Compile and run a native binary using native-image

```
make clean ni
```

You should see a red square on a white background.
Cmd+Q to exit on MacOS. You can also press the X.

## Errors

If you don't run `clean` when switching between poly / ni targets you can get complaints about not finding `clojure.core/seq?`. I think this has to do with how leningen caches .class-files. Just run `make clean ni / poly` and you'll be fine.

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
