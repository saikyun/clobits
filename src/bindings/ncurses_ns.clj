;; This file is autogenerated -- probably shouldn't modify it by hand
(clojure.core/ns
 bindings.ncurses-ns
 (:require [clojure.java.io])
 (:import
  org.graalvm.polyglot.Context
  org.graalvm.polyglot.Source
  org.graalvm.polyglot.Value))

(def ^{:private true} empty-array (clojure.core/object-array 0))

(clojure.core/defn
 context-f615
 []
 (clojure.core/->
  (org.graalvm.polyglot.Context/newBuilder
   (clojure.core/into-array ["llvm"]))
  (.allowAllAccess true)
  (.build)))

(clojure.core/defn
 ^{:private true}
 source-f616
 []
 (clojure.core/->
  (org.graalvm.polyglot.Source/newBuilder
   "llvm"
   (if
    (clojure.core/string? "libs/libbindings$ncurses.so")
    (clojure.java.io/file "libs/libbindings$ncurses.so")
    "libs/libbindings$ncurses.so"))
  (.build)))

(def polyglot-context (context-f615))

(def polyglot-lib (.eval polyglot-context (source-f616)))

(def
 ^{:private true}
 silly617
 (.getMember polyglot-lib "_SHADOWING_silly"))

(clojure.core/defn
 silly
 ([arg0] (.executeVoid silly617 (clojure.core/object-array [arg0]))))

(def
 ^{:private true}
 string618
 (.getMember polyglot-lib "_SHADOWING_string"))

(clojure.core/defn
 string
 ([size] (.execute string618 (clojure.core/object-array [size]))))

(def
 ^{:private true}
 other-print-w619
 (.getMember polyglot-lib "_SHADOWING_other_print_w"))

(clojure.core/defn
 other-print-w
 ([value]
  (clojure.core/->
   (.execute other-print-w619 (clojure.core/object-array [value]))
   .asInt)))

(def
 ^{:private true}
 append-char620
 (.getMember polyglot-lib "_SHADOWING_append_char"))

(clojure.core/defn
 append-char
 ([str pos c]
  (.execute append-char620 (clojure.core/object-array [str pos c]))))

(def
 ^{:private true}
 free621
 (.getMember polyglot-lib "_SHADOWING_free"))

(clojure.core/defn
 free
 ([ptr] (.executeVoid free621 (clojure.core/object-array [ptr]))))

(def
 ^{:private true}
 malloc622
 (.getMember polyglot-lib "_SHADOWING_malloc"))

(clojure.core/defn
 malloc
 ([size] (.executeVoid malloc622 (clojure.core/object-array [size]))))

(def
 ^{:private true}
 initscr623
 (.getMember polyglot-lib "_SHADOWING_initscr"))

(clojure.core/defn initscr ([] (.execute initscr623 empty-array)))

(def
 ^{:private true}
 delwin624
 (.getMember polyglot-lib "_SHADOWING_delwin"))

(clojure.core/defn
 delwin
 ([win]
  (clojure.core/->
   (.execute delwin624 (clojure.core/object-array [win]))
   .asInt)))

(def
 ^{:private true}
 endwin625
 (.getMember polyglot-lib "_SHADOWING_endwin"))

(clojure.core/defn
 endwin
 ([] (clojure.core/-> (.execute endwin625 empty-array) .asInt)))

(def
 ^{:private true}
 printw626
 (.getMember polyglot-lib "_SHADOWING_printw"))

(clojure.core/defn
 printw
 ([arg0]
  (clojure.core/->
   (.execute printw626 (clojure.core/object-array [arg0]))
   .asInt)))

(def
 ^{:private true}
 refresh627
 (.getMember polyglot-lib "_SHADOWING_refresh"))

(clojure.core/defn
 refresh
 ([] (clojure.core/-> (.execute refresh627 empty-array) .asInt)))

(def
 ^{:private true}
 wrefresh628
 (.getMember polyglot-lib "_SHADOWING_wrefresh"))

(clojure.core/defn
 wrefresh
 ([win]
  (clojure.core/->
   (.execute wrefresh628 (clojure.core/object-array [win]))
   .asInt)))
