(ns clobits.examples.sdl.create-sdl-lib
  (:require [clojure.string :as str]
            [clojure.test :as t]
            [clobits.parse-c :as pc] 
            [clobits.core :as cc]
            
            [clobits.all-targets :as at :refer [get-type-throw]]
            [clobits.util :as u]
            [clobits.native-image :as ni]
            [clobits.polyglot :as gp]
            [clobits.gen-c :as gcee]
            
            [clojure.pprint :refer [pp pprint]]))

(def lib-name 'bindings.sdl)

(def functions
  ["int GET_SDL_INIT_VIDEO() { return SDL_INIT_VIDEO; }"
   
   "int GET_SDL_WINDOW_SHOWN() { return SDL_WINDOW_SHOWN; }"
   
   "void* get_null() { return NULL; }"
   
   "char *gen_title() { return \"Clobits SDL Example\"; }"
   
   "
SDL_Rect *create_rect(int x, int y, int w, int h) {
  SDL_Rect *r = (SDL_Rect*)malloc(sizeof(SDL_Rect));
  r->x = x;
  r->y = y;
  r->w = w;
  r->h = h;
  return r;
}"
   
   "
SDL_Event e;

SDL_Event *get_e() {
  return &e;
}"])

(def prototypes
  ["int SDL_Init(Uint32 flags)"
   "int SDL_PollEvent(SDL_Event* event)"
   "void SDL_Delay(Uint32 ms)"
   "int SDL_UpdateWindowSurface(SDL_Window* window)"
   "SDL_Surface* SDL_GetWindowSurface(SDL_Window* window)"
   
   "
Uint32 SDL_MapRGB(const SDL_PixelFormat* format,
                  Uint8                  r, 
                  Uint8                  g, 
                  Uint8                  b)
"
   
   "
SDL_Window* SDL_CreateWindow(const char* title,
                             int         x,
                             int         y,
                             int         w,
                             int         h,
                             Uint32      flags)
"
   
   "
int SDL_FillRect(SDL_Surface*    dst,
                 const SDL_Rect* rect,
                 Uint32          color)
"])

;; the different parts of a struct
;; "SDL_Surface"                                c symbol
;; 'bindings.sdl_ni_generated.SDL_Surface       ni interface
;; 'bindings.sdl_structs.ISDL_Surface           generic interface
;; 'bindings.sdl_ni_generated.WrapSDL_Event     ni wrapper class
;; 'bindings.sdl_ni_generated.WrapSDL_Event.    ni wrapper constructor
;; 'bindings.sdl-ns.wrap-sdl-event              polyglot wrapper function
;; 'clobits.all_targets.IWrapperNI              ni wrapper interface
;; 'clobits.all_targets.IWrapper                generic wrapper interface


;; the different parts
;;

;; "SDL_Surface" -- it is what it is

;; bindings.sdl -- library name

;; bindings.sdl_ni_generated
;; -- package for generated ni code
;; -- perhaps change to bindings.sdl.ni
;;
;;      SDL_Surface
;;      -- interface for ni code, to get correct types
;;      -- perhaps ISDL_SurfaceNI instead
;;
;;      WrapSDL_Event
;;      -- class to wrap native object
;;      -- perhaps SDL_EventWrapper
;;

;; bindings.sdl-ns
;; -- namespace for polyglot namespace
;; -- perhaps bindings.sdl.poly
;;
;;      wrap-sdl-event
;;      -- function to reify ISDL_Event and IWrapper
;;      -- should be turned into class, like WrapSDL_Event
;;         because of tooling + performance
;;

;; bindings.sdl_structs
;; -- namespace for struct interfaces for both ni and poly
;; -- perhaps rename to bindings.sdl.core
;;
;;      ISDL_Surface
;;      -- interface that SDL_Surface and wrap-sdl-event implements
;;      -- should be"single source" of documentation
;;

;; clobits.all_targets
;; -- package for interfaces used in both NI and poly
;;
;;      IWrapper
;;      -- provides .unwrap, wrappers must implement this
;;      -- perhaps should be moved to clobits.core
;;
;;      IWrapperNI
;;      -- extends IWrapper, needed to provide correct types for ni
;;      -- perhaps should be moved to clobits.native-image
;;

(comment ;;example of format
  {"SDL_Event"
   {:c-sym        "SDL_Event"
    :interface    (symbol (str lib-name "." "ISDL_Event"))
    :ni/interface (symbol (str lib-name ".ni." "ISDL_Event"))
    :ni/wrapper   {:convert (symbol (str lib-name ".ni." "SDL_EventWrapper."))
                   :type    (symbol (str lib-name ".ni." "SDL_EventWrapper"))}                 
    :poly/wrapper {:convert (symbol (str lib-name ".poly/" "wrap-sdl-event"))}                 
    :primitive false}}
  )

(def structs
  {"SDL_Event" {:clj-sym 'SDL_Event
                :c-sym "SDL_Event"
                :attrs [{:sym "type" :type "int"}
                        {:sym "key" :type "SDL_KeyboardEvent"}]}
   "SDL_KeyboardEvent" {:clj-sym 'SDL_KeyboardEvent
                        :c-sym "SDL_KeyboardEvent"
                        :attrs [{:sym "keysym"
                                 :type "SDL_Keysym"}]}
   "SDL_Keysym" {:clj-sym 'SDL_Keysym
                 :c-sym "SDL_Keysym"
                 :attrs [{:sym "sym" :type "int"}]}
   "SDL_Surface" {:clj-sym 'SDL_Surface
                  :c-sym "SDL_Surface"
                  :attrs [{:sym "format" :type "SDL_PixelFormat" :pointer "*"}]}
   "SDL_PixelFormat" {:clj-sym 'SDL_PixelFormat
                      :c-sym "SDL_PixelFormat"
                      :attrs [{:sym "palette" :type "void" :pointer "*"}]}
   "SDL_Rect" {:clj-sym 'SDL_Rect
               :c-sym "SDL_Rect"
               :attrs [{:sym "x" :type "Sint16"}
                       {:sym "y" :type "Sint16"}
                       {:sym "w" :type "Uint16"}
                       {:sym "h" :type "Uint16"}]}})

(def typing
  (merge
   cc/default-typing
   
   (->> (map key structs)
        (map (fn [sym]
               (let [i-sym (symbol (str lib-name "_structs." "I" sym))]
                 [sym {:c-sym sym
                       :interface i-sym
                       :poly/type i-sym
                       :ni/interface (symbol (str lib-name ".ni." "I" sym))
                       :ni/unwrap '.unwrap
                       :ni/wrapper   {:convert (symbol (str lib-name ".ni." sym "."))
                                      :type    (symbol (str lib-name ".ni." sym))}
                       :poly/unwrap '.unwrap
                       :poly/wrapper
                       (with-meta
                         (symbol (str #_ lib-name #_ ".poly/" "wrap-" 
                                      (-> (at/java-friendly sym)
                                          #_(u/remove-prefixes ["SDL_"])
                                          str/lower-case
                                          u/snake->kebab)))
                         {:clobits.core/generate true})
                       :primitive false}])))
        (into {}))
   
   {"SDL_Window" cc/void-pointer-type}))

(comment
  (at/get-typing typing {:type "int"})
  )

(def primitives
  #{'int 'long 'char 'void})

(def types
  {"void" {"*" 'clobits.all_targets.IVoidPointer
           nil 'void}
   "int" 'int
   "char" {"*" 'org.graalvm.nativeimage.c.type.CCharPointer
           nil 'char}
   "Uint32" 'int
   "Uint16" 'int
   "Sint16" 'int
   "Uint8" 'int
   "SDL_Surface" 'bindings.sdl_ni_generated.SDL_Surface
   "SDL_Rect" 'bindings.sdl_ni_generated.SDL_Rect
   "SDL_Keysym" 'bindings.sdl_ni_generated.SDL_Keysym
   "SDL_KeyboardEvent" 'bindings.sdl_ni_generated.SDL_KeyboardEvent
   "SDL_Event" 'bindings.sdl_ni_generated.SDL_Event
   "SDL_Window" 'org.graalvm.nativeimage.c.type.VoidPointer
   "SDL_PixelFormat" 'bindings.sdl_ni_generated.SDL_PixelFormat})

(def struct-wrappers
  (into {}
        (map (fn [{:keys [clj-sym c-sym]}]
               (let [t (get-type-throw types {:type c-sym})]
                 [t (symbol (str (ni/lib->package 'bindings.sdl)
                                 "." (symbol (str "Wrap" clj-sym))))]))
             (vals structs))))

(def wrappers
  (merge struct-wrappers
         {'org.graalvm.nativeimage.c.type.VoidPointer 
          'clobits.wrappers.WrapVoid
          'clobits.all_targets.IVoidPointer
          'clobits.wrappers.WrapVoid
          'org.graalvm.word.PointerBase
          'clobits.wrappers.WrapPointer
          'org.graalvm.nativeimage.c.type.CCharPointer
          'clobits.wrappers.WrapPointer}))

(def ni-interfaces (map #(ni/struct->gen-interface types % {:lib-name 'bindings.sdl, :primitives primitives}) (vals structs)))

(defn -main
  []
  (println "Creating libs")
  (.mkdir (java.io.File. "libs"))
  
  (println "Generating bindings.sdl")
  (let [opts {:inline-c (str/join "\n" functions)
              :protos (concat (map pc/parse-c-prototype functions)
                              (map pc/parse-c-prototype prototypes) ;; utility function for turning c-prototypes into clojure data
                              [{:ret "void", :sym "SDL_Quit"}] ;; we can also just provide the data manually
                              )
              :structs structs
              :includes ["stdio.h" "SDL2/SDL.h"]
              :append-ni ni-interfaces
              :primitives primitives
              :typing typing
              :types types
              :wrappers wrappers
              :lib-name lib-name
              :src-dir "src"
              :lib-dir "libs"
              :libs ["SDL2"]}
        opts (merge opts (gcee/gen-lib opts))
        _ (gcee/persist-lib! opts)
        opts (gp/gen-lib opts)
        opts (gp/persist-lib opts)
        opts (assoc opts :ni-code (ni/gen-lib opts))
        opts (assoc opts :wrapper-code (ni/gen-wrapper-ns opts))
        opts (assoc opts :java-code (map #(ni/struct->gen-wrapper-class
                                           types
                                           %
                                           opts) (vals structs)))
        opts (ni/persist-lib opts)]
    opts)
  
  (println "Done!")
  
  ;;(shutdown-agents) ;; need this when running lein exec
  )

(comment
  (-main)
  
  (require 'clobits.examples.sdl.create-sdl-lib :reload-all)
  )

(do (-main)
    (load-file "src/bindings/sdl_ns.clj")
    (load-file "test-src/test_examples.clj"))


