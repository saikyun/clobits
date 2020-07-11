(set! *warn-on-reflection* true)        ; reflection warnings on structs means
                                        ; that native image will crash when accessing fields

(ns clobits.examples.sdl.startup
  (:require [clobits.native-interop :refer [*native-image*]] ; this just sets *native-image*
            [clobits.examples.sdl.constants :as cs])
  (:import [bindings.sdl_structs ISDL_Surface ISDL_Rect])
  (:gen-class))

(if *native-image*
  (do (println "In native image context")
      (require '[bindings.sdl-wrapper :as sdl])
      (import '[bindings.sdl_ni_generated WrapSDL_Surface WrapSDL_Rect]))
  (do (println "In polyglot context")
      (require '[bindings.sdl-ns :as sdl])))

(comment
  (.getMemberKeys (.getMember (.execute (.getMember sdl/polyglot-lib "_SHADOWING_get_e") (clojure.core/object-array [])) "key"))
  
  
  (.getMember sdl/polyglot-lib "SDL_KeyCode")
  )

(defmacro case+
  "Same as case, but evaluates dispatch values, needed for referring to
   class and def'ed constants as well as java.util.Enum instances."
  [value & clauses]
  (let [clauses (partition 2 2 nil clauses)
        default (when (-> clauses last count (== 1))
                  (last clauses))
        clauses (if default (drop-last clauses) clauses)
        eval-dispatch (fn [d]
                        (if (list? d)
                          (map eval d)
                          (eval d)))]
    `(case ~value
       ~@(concat (->> clauses
                      (map #(-> % first eval-dispatch (list (second %))))
                      (mapcat identity))
                 default))))

(defn handle-input
  [state]
  (loop [state state]
    (if (= 0 (sdl/poll-event (sdl/get-e)))
      state
      (case (.type (sdl/get-e))
        
        256 
        (assoc state :quit true)
        
        768 ;; key down
        (do
          (case+ (.sym (.keysym (.key (sdl/get-e))))
                 cs/e
                 (update state :down conj :right)                 
                 
                 cs/a                 
                 (update state :down conj :left)
                 
                 cs/ä
                 (update state :down conj :up)                 
                 
                 cs/o                 
                 (update state :down conj :down)
                 
                 (do
                   (println "key down" (.sym (.keysym (.key (sdl/get-e)))))
                   state)))
        
        769 ;; key up
        (case+ (.sym (.keysym (.key (sdl/get-e))))
               cs/e
               (update state :down disj :right)
               
               cs/a
               (update state :down disj :left)
               
               cs/ä
               (update state :down disj :up)                 
               
               cs/o                 
               (update state :down disj :down)
               
               (do
                 (println "key up" (.sym (.keysym (.key (sdl/get-e)))))
                 state))
        
        (do
          #_(println "event type" (.type (sdl/get-e)))
          (recur state))))))

(defn main-loop
  [window 
   ^ISDL_Surface screen
   ^ISDL_Rect rect
   state]
  (let [state (handle-input state)]
    
    (when (-> state :down :right)
      (.set_x rect (inc (.x rect))))
    (when (-> state :down :left)
      (.set_x rect (dec (.x rect))))    
    
    (when (-> state :down :down)
      (.set_y rect (inc (.y rect))))
    (when (-> state :down :up)
      (.set_y rect (dec (.y rect))))
    
    (sdl/fill-rect screen (sdl/get-null) (sdl/map-rgb (.format screen) 0 0 0))
    (sdl/fill-rect screen rect (sdl/map-rgb (.format screen) 0xFF 0 0))
    (sdl/update-window-surface window)    
    
    state))

(defn -main [& args]
  (sdl/init (sdl/get-sdl-init-video))
  
  (let [window (sdl/create-window (sdl/gen-title)
                                  0
                                  0
                                  640
                                  480
                                  (sdl/get-sdl-window-shown))
        screen (sdl/get-window-surface window)
        rect (sdl/create-rect 0 0 100 50)]
    (.set_format screen (.format screen))      
    
    (println "rgb1" (sdl/map-rgb (.format screen) 0xFF 0xFF 0xFF))
    (println "rgb2" (sdl/map-rgb (.format screen) 0xFF 0 0))
    
    (loop [state {:quit false
                  :down #{}}]
      (let [state (try (main-loop window screen rect state)
                       (catch Exception e
                         (println "Got error" e)
                         state))]
        (when-not (:quit state)
          (Thread/sleep 10)
          (recur state))))
    
    (sdl/quit)))

(comment
  (-main)
  )
