(set! *warn-on-reflection* true)

(ns clobits.examples.sdl.startup
  (:import #_Cool
           #_WrapFormat
           WrapPointer
           [bindings.sdl_ni_generated
            WrapSDL_Event
            WrapSDL_Surface]
           [clobits.all_targets IWrapper])
  (:require [clobits.native-interop :refer [*native-image*]]) ;; this just sets *native-image*
  (:gen-class))

(if *native-image*
  (do (println "In native image context")
      (require '[bindings.sdl_ni])             ;; without this, sdl bindings won't get compiled
      (import '[bindings sdl]))
  (do (println "In polyglot context")
      (require '[bindings.sdl-ns :as sdl])))

#_(defn map-rgb
    [^WrapScreen screen]
    (.format screen)
    #_(sdl/map-rgb (.format (WrapPointer/unwrap screen)) 0xFF 0xFF 0xFF)
    0)

(defn quit-handler
  [^IWrapper v]
  (let [v2 (WrapSDL_Event. (sdl/get-e))]
    (let [quit (when-not (= 0 (sdl/poll-event (.unwrap v)))
                 (when (= 256 (.type v2))
                   true))]
      (println (.type (sdl/get-e)))
      quit)))

(defn get-window-surface
  ^WrapSDL_Surface [^IWrapper window]
  (WrapSDL_Surface. (sdl/get-window-surface (.unwrap window))))

(defn fill-rect
  [^IWrapper screen, ^IWrapper rect, color]
  (sdl/fill-rect (.unwrap screen) (.unwrap rect) color))

(defn -main [& args]
  (sdl/init (sdl/get-sdl-init-video))
  
  (sdl/create-rect 0 0 100 50)
  
  (let [window (sdl/create-window (sdl/gen-title)
                                  0
                                  0
                                  640
                                  480
                                  (sdl/get-sdl-window-shown))
        screen (get-window-surface (WrapPointer. window))
        rect (WrapPointer. (sdl/create-rect 0 0 100 50))]
    #_(println "so format" (.format (WrapFormat.)))
    #_(println "so cool" (.format (Cool.)))
    #_  (sdl/fill-rect screen (sdl/get-null) ;; just nil doesn't work for ni
                       (map-rgb (WrapScreen. screen)))
    
    (fill-rect screen rect (sdl/map-rgb (.format screen) 0xFF 0 0))
    
    
    (sdl/update-window-surface window)
    
    (println "rgb1" (sdl/map-rgb (.format screen) 0xFF 0xFF 0xFF))
    (println "rgb2" (sdl/map-rgb (.format screen) 0xFF 0 0))
    
    (loop [quit false]
      (let [v (WrapSDL_Event. (sdl/get-e))]
        (let [q (quit-handler v)]
          (if q
            :quit
            (recur quit)))))
    
    (sdl/quit)))
