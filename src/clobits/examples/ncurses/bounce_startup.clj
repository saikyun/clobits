#_ (set! *warn-on-reflection* true)

;; following along
;; https://www.viget.com/articles/game-programming-in-c-with-the-ncurses-library/
(ns clobits.examples.ncurses.bounce-startup
  (:require [clobits.native-interop :refer [*native-image*]]
            [clobits.c :as c]
            [clojure.pprint :refer [pprint]]
            [wrappers.void-wrapper])
  (:import #_WrapVoid
           WrapPointer
           org.graalvm.nativeimage.c.type.VoidPointer
           java.util.Vector)
  (:gen-class
   :methods [[rs2 [org.graalvm.nativeimage.c.type.VoidPointer] clojure.lang.PersistentArrayMap]]))

(if *native-image*
  (do (println "In native image context")
      (require '[bindings.ncurses_ni])
      (import '[bindings ncurses])
      (import '[org.graalvm.nativeimage.c.type CTypeConversion])
      (import 'org.graalvm.nativeimage.c.type.VoidPointer))
  (do (println "In polyglot context")
      (require '[bindings.ncurses-ns :as ncurses])))

(defonce win (atom nil))


(defn -rs2
  [win]
  {:x 0, :y 0
   :max-x (ncurses/getmaxx win)
   :max-y (ncurses/getmaxy win)
   :dir 1})

(defn reset-state
  ([] (reset-state {}))
  ([state]
   (merge state
          {:x 0, :y 0
           :max-x (ncurses/getmaxx (WrapPointer/unwrap @win))
           :max-y (ncurses/getmaxy (WrapPointer/unwrap @win))
           :dir 1})))

(defn print-state!
  [{:keys [max-y] :as state}]
  (let [ks      (sort (into [] (keys state)))
        max-x   (inc (apply max (map (comp count name) ks)))
        start-y (- max-y (count ks))]
    (doseq [i (range (count ks))
            :let [k (nth ks i)
                  v (state k)
                  y (+ start-y i)]]
      (ncurses/mvprintw y 0 (c/str* (format (str "%-" max-x "s %s")
                                            k v))))))

(defn main-loop
  [win {:keys [x y dir max-x] :as state}]
  (try
    (ncurses/clear)
    
    (ncurses/mvprintw y x (c/str* "o"))
    
    (print-state! state)
    
    (ncurses/refresh)
    
    (Thread/sleep 30)
    (let [next-x (+ x dir)
          state (if (or (>= next-x max-x)
                        (< next-x 0))
                  (update state :dir -)
                  (update state :x + dir))]
      (-> state
          (assoc :max-x (ncurses/getmaxx (WrapPointer/unwrap win)))
          (assoc :max-y (ncurses/getmaxy (WrapPointer/unwrap win)))
          (assoc :y 0)))
    
    (catch Exception e
      (ncurses/clear)
      (ncurses/mvprintw
       0 0
       (c/str* "Fail: " (with-out-str (pprint (Throwable->map e)))))
      
      (print-state! state)
      (ncurses/refresh)
      (Thread/sleep 1000)
      state)))

(defn lul
  [win]
  (println "ye boi")
  :ok
  )

(defn hmm
  [^String win]
  (println "ye boi")
  :ok
  )

(defn -main []
  (let [www (ncurses/initscr)]
    (reset! win (WrapPointer. www))
    
    (ncurses/refresh)
    (ncurses/noecho)
    
    (loop [state (reset-state)]
      (recur (main-loop @win state)))
    
    (ncurses/endwin))
  
  :ok)
