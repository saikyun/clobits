(ns test-examples
  (:require [clojure.test :refer [deftest is run-tests]]
            [clojure.data :refer [diff]]
            [clojure.string :as str]
            [digest :as d]
            [clojure.java.io :as io]))

(doall (for [path ["src/bindings/ncurses.c"  
                   "src/bindings/ncurses_ni.clj"
                   "src/bindings/ncurses_wrapper.clj"
                   "src/bindings/sdl.h"
                   "src/bindings/sdl_ns.clj"
                   "src/bindings/ncurses.h"
                   "src/bindings/ncurses_ns.clj"
                   "src/bindings/sdl.c"
                   "src/bindings/sdl_ni.clj"
                   "src/bindings/sdl_wrapper.clj"
                   "java-src/bindings/sdl_ni_generated/WrapSDL_Event.java"
                   "java-src/bindings/sdl_ni_generated/WrapSDL_KeyboardEvent.java"
                   "java-src/bindings/sdl_ni_generated/WrapSDL_Keysym.java"
                   "java-src/bindings/sdl_ni_generated/WrapSDL_PixelFormat.java"
                   "java-src/bindings/sdl_ni_generated/WrapSDL_Rect.java"
                   "java-src/bindings/sdl_ni_generated/WrapSDL_Surface.java"]
             :let [new (d/md5 (io/as-file path))
                   old (d/md5 (io/as-file (str "test-example/" path)))]]
         (do #_(do (println (slurp path))
                   (println (slurp (str "test-example/" path))))
             
             (let [[new old both] (diff (slurp path) (slurp (str "test-example/" path)))]
               (when (or new old)
                 (println "Diff in" path)))
             
             (eval `(deftest ~(symbol (str "same-code-" (str/replace path "/" "_")))
                      (is (= ~new ~old)))))))

(run-tests)

