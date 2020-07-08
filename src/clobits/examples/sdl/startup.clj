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

(defmacro cset!
  [[field struct] v]
  (let [field (apply str (rest (str field)))]
    (if *native-image*
      `(~(symbol (apply str ".set_" field)) ~struct ~v)
      `(~'.putMember ~struct ~field ~v))))

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
    (sdl/fill-rect screen rect (sdl/map-rgb (.format screen) 0xFF 0 0))
    
    ;;(cset! (.-format screen) (.format screen))
    (.set_format screen (.format screen))      
    
    (sdl/update-window-surface window)
    (println "rgb1" (sdl/map-rgb (.format screen) 0xFF 0xFF 0xFF))
    (println "rgb2" (sdl/map-rgb (.format screen) 0xFF 0 0))
    
    (loop [quit false]
      (if (let [quit (when-not (= 0 (sdl/poll-event (sdl/get-e)))
                       (when (= 256 (.type (sdl/get-e)))
                         true))]
            (println (.type (sdl/get-e)))
            quit)
        :quit
        (recur quit)))
    
    (sdl/quit)))

(comment
  (-main)
  )
