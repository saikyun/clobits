(ns clobits.examples.ncurses.create-ncurses-lib
  (:require [clojure.string :as str]
            
            [clobits.parse-c :as pc] 
            
            [clobits.native-image :as ni]
            [clobits.polyglot :as gp]
            [clobits.gen-c :as gcee]
            
            [clojure.pprint :refer [pp pprint]]))

(def lib-name 'bindings.ncurses)

(def functions ["
void  silly(char arg0) {
  printf(\"%c\", arg0);
}
"
                
                "
char* string(int size) {
  char* s = (char*)malloc(sizeof(char) * size);
  memset(s, 0, size);
  return s;
}
"
                
                "
int other_print_w(const void *value) {
#if IS_POLYGLOT
  if (polyglot_is_string(value)) {
    int length = polyglot_get_string_size(value);
    char str[length];
    polyglot_as_string(value, str, length, \"UTF-8\");
    return printw(str);
  } else {
    return printw(value);
  }
#else
  return printw(value);
#endif
}
"
                
                "
char* append_char(char *str, int pos, char c) {
  str[pos] = c;
  return str;
}
"])

(def prototypes
  ["void free(void *ptr);"
   "void* malloc(int size);"
   
   "WINDOW *initscr(void);"
   "int delwin(WINDOW *win);"
   "int endwin(void);"
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
