(ns clobits.examples.ncurses.create-ncurses-lib
  (:require [clojure.string :as str]
            [clobits.core :as cc]
            
            [clobits.parse-c :as pc] 
            
            [clojure.pprint :refer [pp pprint]]))

(def lib-name 'clobits.ncurses)

(def functions [])

(def prototypes
  ["void free(void *ptr);"
   "void* malloc(int size);"
   
   "WINDOW *initscr(void);"
   "int delwin(WINDOW *win);"
   "int endwin(void);"
   "int noecho(void);"
   "int curs_set(int visibility);"
   "int mvprintw(int y, int x, char *fmt);"
   "int clear(void);"
   "int getmaxx(WINDOW *win);"
   "int getmaxy(WINDOW *win);"
   "void getmaxyx(WINDOW *win, int y, int x);"
   "int printw(char *);"
   "int refresh(void);"
   "int wrefresh(WINDOW *win);"])

(def structs {})

(def typing
  (merge cc/default-typing
         {"WINDOW" cc/void-pointer-type}))

(def opts (let [opts {:inline-c (str/join "\n" functions)
                      :typing typing
                      :protos (concat (map pc/parse-c-prototype functions)
                                      (map pc/parse-c-prototype prototypes))
                      
                      :structs structs
                      :includes ["ncurses.h" "stdlib.h" "string.h"]
                      
                      :lib-name lib-name
                      :c-src-dir "examples/ncurses/src/c"
                      :src-dir "examples/ncurses/src/clj"
                      :java-src-dir "examples/ncurses/src/java"
                      :lib-dir "libs"
                      :libs ["ncurses"]}]
            (merge opts (cc/generate-lib-names opts))))

(defn -main
  []
  (cc/gen-and-persist! opts)

  (when-not (System/getenv "REPLING")
    (shutdown-agents) ;; see https://clojure.atlassian.net/browse/CLJ-124
    ))

(comment
  (-main)
  )
