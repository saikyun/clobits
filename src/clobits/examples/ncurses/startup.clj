(ns clobits.examples.ncurses.startup
  (:require [clobits.native-interop :refer [*native-image*]]
            [clobits.c :as c])
  (:gen-class))

(if *native-image*
  (do (println "In native image context")
      (require '[bindings.ncurses_ni])
      (import '[bindings ncurses])
      (import '[org.graalvm.nativeimage.c.type CTypeConversion]))
  (do (println "In polyglot context")
      (require '[bindings.ncurses-ns :as ncurses])))

(defn -main [& args]
  (let [w (ncurses/initscr)]
    
    (ncurses/printw (c/char* "hello"))
    (ncurses/curs-set 0)
    (Thread/sleep 1000)
    
    ;;(ncurses/delwin w)
    (ncurses/endwin)))

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
