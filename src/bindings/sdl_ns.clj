;; This file is autogenerated -- probably shouldn't modify it by hand
(clojure.core/ns
 bindings.sdl-ns
 (:require [clojure.java.io])
 (:import
  org.graalvm.polyglot.Context
  org.graalvm.polyglot.Source
  org.graalvm.polyglot.Value))

(def empty-array (clojure.core/object-array 0))

(clojure.core/defn
 context-f594
 []
 (clojure.core/->
  (org.graalvm.polyglot.Context/newBuilder
   (clojure.core/into-array ["llvm"]))
  (.allowIO true)
  (.allowNativeAccess true)
  (.build)))

(clojure.core/defn
 source-f595
 []
 (clojure.core/->
  (org.graalvm.polyglot.Source/newBuilder
   "llvm"
   (if
    (clojure.core/string? "libs/libbindings$sdl.so")
    (clojure.java.io/file "libs/libbindings$sdl.so")
    "libs/libbindings$sdl.so"))
  (.build)))

(def lib593 (.eval (context-f594) (source-f595)))

(clojure.core/gen-interface
 :name
 ^{org.graalvm.polyglot.HostAccess$Implementable true}
 bindings.sdl_poly.SDL_Event
 :methods
 [[type [] int]])

(clojure.core/gen-interface
 :name
 ^{org.graalvm.polyglot.HostAccess$Implementable true}
 bindings.sdl_poly.SDL_Surface
 :methods
 [[format [] bindings.sdl_poly.SDL_PixelFormat]])

(clojure.core/gen-interface
 :name
 ^{org.graalvm.polyglot.HostAccess$Implementable true}
 bindings.sdl_poly.SDL_PixelFormat
 :methods
 [[palette [] org.graalvm.nativeimage.c.type.VoidPointer]])

(def
 get-sdl-init-video596
 (.getMember lib593 "_SHADOWING_GET_SDL_INIT_VIDEO"))

(clojure.core/defn
 get-sdl-init-video
 ([]
  (clojure.core/->
   (.execute get-sdl-init-video596 empty-array)
   .asInt)))

(def
 get-sdl-window-shown597
 (.getMember lib593 "_SHADOWING_GET_SDL_WINDOW_SHOWN"))

(clojure.core/defn
 get-sdl-window-shown
 ([]
  (clojure.core/->
   (.execute get-sdl-window-shown597 empty-array)
   .asInt)))

(def get-null598 (.getMember lib593 "_SHADOWING_get_null"))

(clojure.core/defn get-null ([] (.executeVoid get-null598 empty-array)))

(def gen-title599 (.getMember lib593 "_SHADOWING_gen_title"))

(clojure.core/defn gen-title ([] (.execute gen-title599 empty-array)))

(def create-rect600 (.getMember lib593 "_SHADOWING_create_rect"))

(clojure.core/defn
 create-rect
 ([x y w h]
  (.execute create-rect600 (clojure.core/object-array [x y w h]))))

(def get-e601 (.getMember lib593 "_SHADOWING_get_e"))

(clojure.core/defn
 get-e
 ([] (.as (.execute get-e601 empty-array) bindings.sdl_poly.SDL_Event)))

(def init602 (.getMember lib593 "_SHADOWING_SDL_Init"))

(clojure.core/defn
 init
 ([flags]
  (clojure.core/->
   (.execute init602 (clojure.core/object-array [flags]))
   .asInt)))

(def poll-event603 (.getMember lib593 "_SHADOWING_SDL_PollEvent"))

(clojure.core/defn
 poll-event
 ([event]
  (clojure.core/->
   (.execute poll-event603 (clojure.core/object-array [event]))
   .asInt)))

(def delay604 (.getMember lib593 "_SHADOWING_SDL_Delay"))

(clojure.core/defn
 delay
 ([ms] (.executeVoid delay604 (clojure.core/object-array [ms]))))

(def
 update-window-surface605
 (.getMember lib593 "_SHADOWING_SDL_UpdateWindowSurface"))

(clojure.core/defn
 update-window-surface
 ([window]
  (clojure.core/->
   (.execute
    update-window-surface605
    (clojure.core/object-array [window]))
   .asInt)))

(def
 get-window-surface606
 (.getMember lib593 "_SHADOWING_SDL_GetWindowSurface"))

(clojure.core/defn
 get-window-surface
 ([window]
  (.as
   (.execute
    get-window-surface606
    (clojure.core/object-array [window]))
   bindings.sdl_poly.SDL_Surface)))

(def map-rgb607 (.getMember lib593 "_SHADOWING_SDL_MapRGB"))

(clojure.core/defn
 map-rgb
 ([format r g b]
  (clojure.core/->
   (.execute map-rgb607 (clojure.core/object-array [format r g b]))
   .asInt)))

(def create-window608 (.getMember lib593 "_SHADOWING_SDL_CreateWindow"))

(clojure.core/defn
 create-window
 ([title x y w h flags]
  (.execute
   create-window608
   (clojure.core/object-array [title x y w h flags]))))

(def fill-rect609 (.getMember lib593 "_SHADOWING_SDL_FillRect"))

(clojure.core/defn
 fill-rect
 ([dst rect color]
  (clojure.core/->
   (.execute fill-rect609 (clojure.core/object-array [dst rect color]))
   .asInt)))

(def quit610 (.getMember lib593 "_SHADOWING_SDL_Quit"))

(clojure.core/defn quit ([] (.executeVoid quit610 empty-array)))
