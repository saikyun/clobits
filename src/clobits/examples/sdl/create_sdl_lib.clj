(ns clobits.examples.sdl.create-sdl-lib
  (:require [clojure.string :as str]
            
            [clobits.parse-c :as pc] 
            
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

(def structs
  {"SDL_Event" {:clj-sym 'SDL_Event
                :c-sym "SDL_Event"
                :attrs [{:sym "type" :type "int"}]}
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

(declare poly-wrappers)

(def poly-wrappers
  (merge (->> (map (fn [{:keys [clj-sym c-sym]}]
                     [(get-type-throw types {:type c-sym})
                      {:create (symbol (str "wrap-" (u/snake->kebab clj-sym)))
                       :type nil}])
                   (vals structs))
              (into {}))
         {'org.graalvm.nativeimage.c.type.CCharPointer
          {:create 'wrap-pointer :type nil}
          'org.graalvm.nativeimage.c.type.VoidPointer
          {:create 'wrap-pointer :type nil}
          'clobits.all_targets.IVoidPointer
          {:create 'wrap-pointer :type nil}}))

(def conversion-functions
  {'int '.asInt
   'bindings.sdl_ni.SDL_PixelFormat 'identity
   'org.graalvm.nativeimage.c.type.VoidPointer 'identity
   'org.graalvm.word.PointerBase 'identity
   'clobits.all_targets.IVoidPointer 'identity
   
   'org.graalvm.nativeimage.c.type.CCharPointer 'identity #_ '.asString ;; constant char* can't be coerced into strings
   'void 'identity
   'char `(~'.as Character)})

(def constructors
  (concat
   [(conj (map #(if (map? %) (:create %) %) (into #{} (vals poly-wrappers))) `declare)]
   (map (fn [{:keys [clj-sym attrs]}]
          (let [value-sym 'value
                struct-interface (at/struct-sym->interface-sym lib-name clj-sym)]
            `(defn ~(symbol (str "wrap-" (u/snake->kebab clj-sym))) [~value-sym]
               ~(concat
                 `(reify
                    ~struct-interface)
                 
                 (->> (for [{:keys [sym type] :as attr} attrs
                            :let [t (get-type-throw types attr)
                                  conv-func (when-not (poly-wrappers t)
                                              (at/convert-function-throw 
                                               conversion-functions
                                               (get-type-throw types attr)))
                                  wrap-convert (fn [body]
                                                 (if (and conv-func (not= conv-func 'identity))
                                                   `(-> ~body ~conv-func)
                                                   body))
                                  intf (at/struct-sym->interface-sym lib-name type)]]
                        [`(~(symbol sym) [~'_]
                           ~(-> (if-let [w (poly-wrappers t)]
                                  `(~(if (map? w) (:create w) w)
                                    (~'.getMember ~value-sym ~sym))
                                  `(~'.getMember ~value-sym ~sym))
                                wrap-convert))
                         
                         `(~(symbol (str "set_" sym)) [~'_ ~'v]
                           ~(if (poly-wrappers t)
                              `(~'.putMember ~value-sym ~sym (~'.unwrap ~'v))
                              `(~'.putMember ~value-sym ~sym ~'v)))])
                      (apply concat))
                 
                 `(~'clobits.all_targets.IWrapper
                   (~'unwrap [~'_]
                    (~'.as ~value-sym ~struct-interface)))))))
        (vals structs))
   
   [(let [value-sym 'value]
      `(defn ~(symbol "wrap-pointer") [~value-sym]
         (reify
           ~'clobits.all_targets.IWrapper
           (~'unwrap [~'_]
            ~value-sym))))]))

(def ni-interfaces (map #(ni/struct->gen-interface types % {:lib-name 'bindings.sdl}) (vals structs)))

(def poly-types
  {"void" {"*" 'clobits.all_targets.IVoidPointerYE
           nil 'void}
   "int" 'int
   "char" {"*" 'org.graalvm.nativeimage.c.type.CCharPointer
           nil 'char}
   "Uint32" 'int
   "Uint16" 'int
   "Sint16" 'int
   "Uint8" 'int
   "SDL_Surface" 'bindings.sdl_structs.ISDL_Surface
   "SDL_Rect" 'org.graalvm.nativeimage.c.type.VoidPointer
   "SDL_Event" 'bindings.sdl_structs.ISDL_Event
   "SDL_Window" 'org.graalvm.nativeimage.c.type.VoidPointer
   "SDL_PixelFormat" 'bindings.sdl_structs.ISDL_PixelFormat})

(def poly-interfaces (map #(gp/struct->gen-interface poly-types % {:lib-name 'bindings.sdl}) (vals structs)))

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
              :append-clj (concat poly-interfaces
                                  constructors)
              :append-ni ni-interfaces
              :primitives primitives
              :types types
              :wrappers wrappers
              :poly-wrappers poly-wrappers
              :poly-conversions conversion-functions
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
