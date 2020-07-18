Structure of the examples:

## create-src 

Example code for generating a clojure library and the required interfaces / classes

## gen-src

The result of generating a library. This is representative of what a user of the library would `require`. This source (and some classes built from it) could be packaged as a .jar.

These files are in this repo so that one can inspect them on github. As soon as you run a lein command in this project, they will be re-generated.

## src

Examples of usage of the generated library.
