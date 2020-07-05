(ns clobits.examples.sdl.create-sdl-lib
  (:require [clojure.string :as str]
            
            [clobits.parse-c :as pc] 
            
            [clobits.all-targets :refer [get-type-throw]]
            [clobits.native-image :as ni]
            [clobits.polyglot :as gp]
            [clobits.gen-c :as gcee]
            
            [clojure.pprint :refer [pp pprint]]))

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

(def structs
  {"SDL_Event" {:clj-sym 'SDL_Event
                :c-sym "SDL_Event"
                :attrs [{:sym "type" :type "int"}]}
   "SDL_Surface" {:clj-sym 'SDL_Surface
                  :c-sym "SDL_Surface"
                  :attrs [{:sym "format" :type "SDL_PixelFormat" :pointer "*"}]}
   "SDL_PixelFormat" {:clj-sym 'SDL_PixelFormat
                      :c-sym "SDL_PixelFormat"
                      :attrs [{:sym "palette" :type "void" :pointer "*"}]}})

(def primitives
  #{'int 'long 'char 'void})

(def types
  {"void" {"*" 'clobits.all_targets.IVoidPointer
           nil 'void}
   "int" 'int
   "char" {"*" 'org.graalvm.nativeimage.c.type.CCharPointer
           nil 'char}
   "Uint32" 'int
   "Uint8" 'int
   "SDL_Surface" 'bindings.sdl_ni_generated.SDL_Surface
   "SDL_Rect" 'org.graalvm.nativeimage.c.type.VoidPointer
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

(def ni-interfaces (map #(ni/struct->gen-interface types % {:lib-name 'bindings.sdl}) (vals structs)))

(def poly-types
  {"void" {"*" 'clobits.all_targets.IVoidPointerYE
           nil 'void}
   "int" 'int
   "char" {"*" 'org.graalvm.nativeimage.c.type.CCharPointer
           nil 'char}
   "Uint32" 'int
   "Uint8" 'int
   "SDL_Surface" 'bindings.sdl_structs.ISDL_Surface
   "SDL_Rect" 'org.graalvm.nativeimage.c.type.VoidPointer
   "SDL_Event" 'bindings.sdl_structs.ISDL_Event
   "SDL_Window" 'org.graalvm.nativeimage.c.type.VoidPointer
   "SDL_PixelFormat" 'bindings.sdl_structs.ISDL_PixelFormat})

(def poly-interfaces (map #(gp/struct->gen-interface poly-types % {:lib-name 'bindings.sdl}) (vals structs)))

(def conversion-functions
  {'int '.asInt
   'bindings.sdl_ni.SDL_PixelFormat 'identity
   'org.graalvm.nativeimage.c.type.VoidPointer 'identity
   'org.graalvm.word.PointerBase 'identity
   'clobits.all_targets.IVoidPointer 'identity
   
   'org.graalvm.nativeimage.c.type.CCharPointer 'identity #_ '.asString ;; constant char* can't be coerced into strings
   'void 'identity
   'char `(~'.as Character)})

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
              :append-clj poly-interfaces #_ protocols-and-extend
              :append-ni ni-interfaces
              :primitives primitives
              :types types
              :wrappers wrappers
              :poly-conversions conversion-functions
              :lib-name 'bindings.sdl
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
                                           {:lib-name 'bindings.sdl
                                            :wrappers wrappers}) (vals structs)))
        opts (ni/persist-lib opts)]
    opts)
  
  (println "Done!")
  
  (shutdown-agents) ;; need this when running lein exec
  )

(comment
  (-main)
  )
