(ns clobits.examples.ncurses.startup
  (:require [clobits.native-interop :refer [*native-image*]])
  (:gen-class))

(if *native-image*
  (do (println "In native image context")
      (require '[bindings.ncurses_ni])
      (import '[bindings ncurses])
      (import '[org.graalvm.nativeimage.c.type CTypeConversion]))
  (do (println "In polyglot context")
      (require '[bindings.ncurses-ns :as ncurses])))

(defmacro with-heap
  [bindings & body]
  (@#'clojure.core/assert-args
   (vector? bindings) "a vector for its bindings"
   (even? (count bindings)) "an even number of forms in binding vector")
  (cond (= 0 (count bindings)) `(do ~@body)
        
        (symbol? (bindings 0))
        `(let ~(subvec bindings 0 2)
           (try (with-heap ~(subvec bindings 2) ~@body)
                (finally
                  (ncurses/free ~(bindings 0)))))
        
        :else (throw (IllegalArgumentException.
                      "with-heap only allows Symbols in bindings"))))

(defmacro with-window
  [bindings & body]
  (@#'clojure.core/assert-args
   (vector? bindings) "a vector for its bindings"
   (even? (count bindings)) "an even number of forms in binding vector")
  (cond (= 0 (count bindings)) `(do ~@body)
        
        (symbol? (bindings 0))
        `(let ~(subvec bindings 0 2)
           (try (with-heap ~(subvec bindings 2) ~@body)
                (finally
                  (ncurses/delwin ~(bindings 0)))))
        
        :else (throw (IllegalArgumentException.
                      "with-heap only allows Symbols in bindings"))))

(defn string->char-pointer
  [s]
  (let [size (inc (count s))
        str (ncurses/string size)]
    (doseq [i (range 0 (count s))
            :let [c (nth s i)]]
      (ncurses/append-char str i c))
    str))

(defmacro char*
  [s]
  (if *native-image*
    `(.get (CTypeConversion/toCString ~s))
    `(string->char-pointer ~s)))

(defn -main [& args]
  (let [w (ncurses/initscr)]
    
    (let [yo "hej"]
      (ncurses/printw (char* yo)))
    (ncurses/refresh)
    (Thread/sleep 1000)
    
    (ncurses/delwin w)
    
    (ncurses/endwin)))

(comment
  (with-heap [s (char* "\nbye!")]
    (ncurses/printw s)
    (ncurses/refresh)
    )
  )

(comment
  (def s (ncurses/string 512))
  (ncurses/free s)
  
  (ncurses/other-print-w "hej")
  
  (-main)
  (do
    (use 'clojure.pprint)
    (pprint (ns-publics 'bindings.ncurses-ns)))
  
  (def s (ncurses/string 512))
  (ncurses/append-char s 0 \h)  
  (ncurses/append-char s 1 \e)  
  (ncurses/append-char s 2 \j)  
  
  (ncurses/printw s)
  
  (.asValue ncurses/polyglot-context "hej")
  
  (.asValue ncurses/polyglot-context nil)
  
  (def www   (ncurses/initscr))
  
  www
  
  (import 'org.graalvm.polyglot.proxy.ProxyArray)
  
  
  
  (ncurses/vw-printw www 
                     (org.graalvm.polyglot.proxy.ProxyArray/fromList ['a 'b 'c])
                     (org.graalvm.polyglot.proxy.ProxyArray/fromList []))
  
  (ncurses/printw (.execute (.getMember ncurses/polyglot-lib "string") (object-array [])))
  
  (ncurses/printw "hej")
  
  (type "")
  
  (.getMember ncurses/polyglot-lib "vw_printw")
  (.getMember ncurses/polyglot-lib "silly")
  
  (ncurses/silly \a)
  
  (ncurses/silly (org.graalvm.polyglot.proxy.ProxyArray/fromList []))
  
  
  
  (require '[bindings.ncurses-ns :as ncurses]  :reload-all)
  
  )
