(set! *warn-on-reflection* true)

(ns clobits.examples.sdl.startup
  (:import #_Cool
           clobits.wrappers.WrapPointer
           [bindings.sdl_ni_generated
            WrapSDL_Event
            WrapSDL_PixelFormat           
            WrapSDL_Surface]
           [clobits.all_targets IWrapper])
  (:require [clobits.native-interop :refer [*native-image*]]) ;; this just sets *native-image*
  (:gen-class))

(if *native-image*
  (do (println "In native image context")
      (require '[bindings.sdl-wrapper :as sdl])
      #_(import '[bindings sdl]))
  (do (println "In polyglot context")
      (require '[bindings.sdl-ns :as sdl])))

#_(defn map-rgb
    [^WrapScreen screen]
    (.format screen)
    #_(sdl/map-rgb (.format (WrapPointer/unwrap screen)) 0xFF 0xFF 0xFF)
    0)

#_(defn quit-handler
    [^IWrapper v]
    (let [v2 (WrapSDL_Event. (sdl/get-e))]
      (let [quit (when-not (= 0 (sdl/poll-event (.unwrap v)))
                   (when (= 256 (.type v2))
                     true))]
        (println (.type (sdl/get-e)))
        quit)))

#_(defn get-window-surface
    ^WrapSDL_Surface [^IWrapper window]
    (WrapSDL_Surface. (sdl/get-window-surface (.unwrap window))))

#_(defn fill-rect
    [^IWrapper screen, ^IWrapper rect, color]
    (sdl/fill-rect (.unwrap screen) (.unwrap rect) color))

(defn -main [& args]
  (sdl/init (sdl/get-sdl-init-video))
  
  (sdl/create-rect 0 0 100 50)    
  
  #_(let [window (sdl/create-window (sdl/gen-title)
                                    0
                                    0
                                    640
                                    480
                                    (sdl/get-sdl-window-shown))
          screen (sdl/get-window-surface window)]
      (sdl/create-rect 0 0 100 50))
  
  (let [window (sdl/create-window (sdl/gen-title)
                                  0
                                  0
                                  640
                                  480
                                  (sdl/get-sdl-window-shown))
        screen (sdl/get-window-surface window)
        rect (sdl/create-rect 0 0 100 50)]
    
    (sdl/fill-rect screen rect (sdl/map-rgb (.format screen) 0xFF 0 0))
    
    
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
        (recur quit))))
  
  (sdl/quit))
