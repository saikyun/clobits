The structure of an example, in this case sdl, is as follows:

## sdl/create-src 

Example code for generating a clojure library and the required interfaces / classes

## sdl/gen-src

The result of generating a library. This is representative of what a user of sdl would `require`. This source (and some classes built from it) could be packaged as a .jar.

These files are in this repo so that one can inspect them on github. As soon as you run a lein command in this project, they will be re-generated.

## sdl/src

Examples of usage of the generated library.
