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
