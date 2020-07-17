(set! *warn-on-reflection* true)        ; reflection warnings on structs means
                                        ; that native image will crash when accessing fields

(ns clobits.examples.sdl.startup
  (:require [clobits.native-interop :refer [*native-image*]] ; this just sets *native-image*
            [clobits.examples.sdl.constants :as cs])
  (:import [clobits.sdl Surface Rect])
  (:gen-class))

(if *native-image*
  (do (println "In native image context")
      (require '[clobits.sdl.ni :as sdl]))
  (do (println "In polyglot context")
      (require '[clobits.sdl.poly :as sdl])))

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
   ^Surface screen
   ^Rect rect
   state]
  (let [state (cond-> (handle-input state)
                (-> state :down :right) (update-in [:pos :x] inc)
                (-> state :down :left)  (update-in [:pos :x] dec)
                (-> state :down :down)  (update-in [:pos :y] inc)
                (-> state :down :up)    (update-in [:pos :y] dec))]
    (.set_x rect (-> state :pos :x))
    (.set_y rect (-> state :pos :y))
    
    (sdl/fill-rect screen (sdl/get-null) (sdl/map-rgb (.format screen) 0 0 0))
    (sdl/fill-rect screen rect (sdl/map-rgb (.format screen) 0xFF 0 0))
    (sdl/update-window-surface window)    
    
    state))

(defn -main []
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
                  :down #{}
                  :pos {:x 0
                        :y 0}}]
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
