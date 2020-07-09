(set! *warn-on-reflection* true)

(ns clobits.examples.sdl.startup
  (:require [clobits.native-interop :refer [*native-image*]]) ;; this just sets *native-image*
  (:gen-class))

(if *native-image*
  (do (println "In native image context")
      (require '[bindings.sdl-wrapper :as sdl])
      #_(import '[bindings sdl]))
  (do (println "In polyglot context")
      (require '[bindings.sdl-ns :as sdl])))















(comment
  (.getMemberKeys (.getMember (.execute (.getMember sdl/polyglot-lib "_SHADOWING_get_e") (clojure.core/object-array [])) "key"))
  )

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
          (case  (.sym (.keysym (.key (sdl/get-e))))
            1073741903
            (update state :down conj :right)
            
            1073741904
            (update state :down conj :left)
            
            state))
        
        769 ;; key up
        (case  (.sym (.keysym (.key (sdl/get-e))))
          1073741903
          (update state :down disj :right)
          
          1073741904
          (update state :down disj :left)
          
          state)
        
        (do
          (println "event type" (.type (sdl/get-e)))
          (recur state))))))

(defn main-loop
  [window screen rect state]
  (let [state (handle-input state)]
    (when (-> state :down :right)
      (.set_x rect (inc (.x rect))))
    (when (-> state :down :left)
      (.set_x rect (dec (.x rect))))
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
