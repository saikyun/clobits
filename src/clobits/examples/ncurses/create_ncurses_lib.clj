(ns clobits.examples.ncurses.create-ncurses-lib
  (:require [clojure.string :as str]
            
            [clobits.parse-c :as pc] 
            
            [clobits.native-image :as ni]
            [clobits.polyglot :as gp]
            [clobits.gen-c :as gcee]
            
            [clojure.pprint :refer [pp pprint]]))

(def lib-name 'bindings.ncurses)

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

(def types
  {"void" {"*" 'org.graalvm.nativeimage.c.type.VoidPointer
           nil 'void}
   "int" 'int
   "char" {"*" 'org.graalvm.nativeimage.c.type.CCharPointer
           nil 'char}
   "va_list" 'org.graalvm.nativeimage.c.type.VoidPointer
   "WINDOW" 'org.graalvm.nativeimage.c.type.VoidPointer})

(def ni-interfaces (map #(ni/struct->gen-interface types % {:lib-name lib-name}) (vals structs)))
(def poly-interfaces (map #(gp/struct->gen-interface types % {:lib-name lib-name}) (vals structs)))

(def conversion-functions
  {'int '.asInt
   'org.graalvm.nativeimage.c.type.VoidPointer 'identity
   'org.graalvm.nativeimage.c.type.CCharPointer 'identity #_ '.asString ;; constant char* can't be coerced into strings
   'void 'identity})

(defn -main
  []
  (println "Creating libs")
  (.mkdir (java.io.File. "libs"))
  
  (println "Generating" lib-name)
  (let [opts {:inline-c (str/join "\n" functions)
              :protos (concat (map pc/parse-c-prototype functions)
                              (map pc/parse-c-prototype prototypes))
              :structs structs
              :includes ["ncurses.h" "stdlib.h" "string.h"]
              :append-clj poly-interfaces
              :poly-conversions conversion-functions
              :append-ni ni-interfaces
              :types types
              :lib-name lib-name
              :src-dir "src"
              :lib-dir "libs"
              :libs ["ncurses"]}
        opts (merge opts (gcee/gen-lib opts))
        _ (gcee/persist-lib! opts)
        opts (gp/gen-lib opts)
        opts (gp/persist-lib opts)
        opts (assoc opts :ni-code (ni/gen-lib opts))
        opts (ni/persist-lib opts)]
    opts)
  
  (println "Done!")
  
  (shutdown-agents) ;; need this when running lein exec
  )

(comment
  (-main)
  )
