(ns clobits.native-image
  (:require [clobits.patch-gen-class :as pgc]
            [clojure.java.io :as io]
            [clobits.all-targets :as at :refer [gen-clojure-mapping get-type-throw java-friendly]]
            [clobits.util :as u :refer [snake-case no-subdir]]
            [clojure.string :as str]
            [clojure.pprint :refer [pprint pp]]))

(defn lib->package
  [lib-name]
  (str lib-name "_ni_generated"))

(defn sym->classname
  [lib-name clj-sym]
  (str (lib->package lib-name) "." (java-friendly clj-sym)))

(defn attr->method
  [types {:keys [sym pointer] :as arg} {:keys [primitives]}]
  (def dafe "true")
  (def ppp pointer)
  (def pri primitives)
  (let [t (get-type-throw types arg)
        field-or-address (if (and (not (primitives t))
                                  (not (seq pointer)))
                           'org.graalvm.nativeimage.c.struct.CFieldAddress
                           'org.graalvm.nativeimage.c.struct.CField)]
    (concat ;; getter
     [[(with-meta (symbol sym)
         {field-or-address sym})
       []
       t]]
     
     ;; setter
     (when (or (primitives t) ;; not sure if setters for non-pointer structs can work
               (seq pointer))
       [[(with-meta (symbol (str "set_" sym))
           {org.graalvm.nativeimage.c.struct.CField sym})
         [(get-type-throw types arg)]
         'void]]))))

(defn struct->gen-interface
  [types {:keys [c-sym clj-sym attrs]} {:keys [lib-name] :as opts}]
  (let [java-friendly-lib-name (java-friendly lib-name)
        context (symbol (str java-friendly-lib-name "_ni.Headers"))]
    `(gen-interface
      :name ~(with-meta #_clj-sym (symbol (sym->classname lib-name clj-sym))
               {org.graalvm.nativeimage.c.CContext context
                org.graalvm.nativeimage.c.function.CLibrary (u/so-lib-name-ni lib-name)
                org.graalvm.nativeimage.c.struct.CStruct c-sym})
      :extends [org.graalvm.word.PointerBase
                ~(symbol (str java-friendly-lib-name "_structs.I" (java-friendly clj-sym)))]
      :methods ~(->> (map #(attr->method types % opts) attrs)
                     (apply concat)
                     (into [])))))

(defn gen-getter
  [types {:keys [type sym] :as attr} {:keys [wrappers]}]
  (let [t (get-type-throw types attr)
        w (wrappers t)]
    (str "  public " (or w t) " " sym "() {
    " (if w
        (str "return new " w "(this.pointer." sym "());")
        (str "return this.pointer." sym "();"))
         "
  }")))

(defn gen-setters
  [types {:keys [type sym] :as attr} {:keys [lib-name wrappers structs]}]
  (let [t (get-type-throw types attr)
        w (wrappers t)
        input-type (cond (get structs type)
                         (at/struct-sym->interface-sym lib-name type)
                         
                         (= type "void")
                         'clobits.all_targets.IVoidPointerYE
                         
                         :else
                         t)]
    (if w
      (str (str "  public void set_" sym "(" (or w t) " v) {
    " (str "this.pointer.set_" sym "(v.unwrap());")
                "
  }\n\n")
           
           (str "  public void set_" sym "(" input-type " v) {
    " (str "this.pointer.set_" sym "(v);")
                "
  }"))
      
      
      
      (str "  public void set_" sym "(" (or w t) " v) {
    " (str "this.pointer.set_" sym "(v);")
           "
  }")
      
      )))

(defn struct->gen-wrapper-class
  [types {:keys [c-sym clj-sym attrs] :as s} {:keys [lib-name] :as opts}]
  (let [classname (str "Wrap" (java-friendly clj-sym))
        funcs (str/join "\n\n" (concat (map #(gen-getter types % opts) attrs)
                                       (map #(gen-setters types % opts) attrs)))]
    {:classname (str (lib->package lib-name) "." classname)
     :code (str ;; unused since same package.  "import " (sym->classname lib-name clj-sym) ";"
            "
package " (lib->package lib-name) ";

import " lib-name "_structs.I" (java-friendly clj-sym) ";
import clobits.all_targets.IWrapperNI;

public class " classname " implements I" (java-friendly clj-sym) ", IWrapperNI {
  " (java-friendly clj-sym) " pointer;

  // used when sending data to native functions
  public " (java-friendly clj-sym) " unwrap() {
    return this.pointer;
  }

" funcs "

  public " classname "(" (java-friendly clj-sym) " pointer) {
    this.pointer = pointer;
  }
}
"
            
            )})
  )



(def ttt {"void" {"*" 'org.graalvm.nativeimage.c.type.VoidPointer
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

(def example-wrappers
  {'bindings.sdl_ni_generated.SDL_Event
   'bindings.sdl_ni_generated.WrapSDL_Event,
   'bindings.sdl_ni_generated.SDL_Surface
   'bindings.sdl_ni_generated.WrapSDL_Surface,
   'bindings.sdl_ni_generated.SDL_PixelFormat
   'bindings.sdl_ni_generated.WrapSDL_PixelFormat})

(println
 
 
 (struct->gen-wrapper-class ttt {:clj-sym 'SDL_Surface
                                 :c-sym "SDL_Surface"
                                 :attrs [{:sym "format" :type "SDL_PixelFormat" :pointer "*"}]}
                            {:lib-name 'bindings.sdl
                             :wrappers example-wrappers
                             :primitives #{}
                             })
 )

(comment
  
  (struct->gen-interface ttt {:clj-sym 'SDL_Event
                              :c-sym "SDL_Event"
                              :attrs [{:sym "type" :type "int"}]}
                         {:lib-name 'sdl
                          :primitives #{}})
  
  (struct->gen-wrapper-class ttt {:clj-sym 'SDL_Event
                                  :c-sym "SDL_Event"
                                  :attrs [{:sym "type" :type "int"}]}
                             {:lib-name 'sdl}))

(defn gen-method
  "Generates a method that calls a native function."
  [[f-sym {:keys [ret pointer sym args]}] {:keys [types]}]
  (-> [(with-meta (symbol (str/replace (name f-sym) "-" "_"))
         {'org.graalvm.nativeimage.c.function.CFunction
          {:transition 'org.graalvm.nativeimage.c.function.CFunction$Transition/NO_TRANSITION
           :value sym}})
       (into [] (map (partial get-type-throw types) args))
       (get-type-throw types {:type ret, :pointer pointer})]
      (with-meta {:static true, :native true})))

(defn gen-defn
  "Generates a defn-call, which calls a method.
  The function resulting from evaluating the defn-call will wrap / unwrap native values / wrappers as needed."
  [[f-sym {:keys [ret pointer sym args] :as func}] {:keys [types primitives lib-name wrappers]}]
  (let [param-primitives (>= 4 (count args))
        params (into [] (map (fn [arg]
                               {:unwrap (let [t (get-type-throw types arg)]
                                          (cond
                                            (= 'int (primitives t))
                                            'int
                                            
                                            (not (primitives t)) '.unwrap))
                                :annotation (let [t (get-type-throw types arg)]
                                              (cond
                                                (= 'int (primitives t))
                                                (if param-primitives
                                                  'long
                                                  'Long)
                                                
                                                (primitives t)
                                                (if param-primitives
                                                  t
                                                  (symbol (str/upper-case (str t))))
                                                
                                                :else 'clobits.all_targets.IWrapperNI))
                                :arg-symbol (-> arg :sym symbol)}) args))
        ret {:wrapper (let [t (get-type-throw types {:type ret :pointer pointer})]
                        (cond
                          (= 'int (primitives t))
                          'long
                          
                          (not (primitives t))
                          (symbol (str (wrappers t) "."))))
             :annotation (let [t (get-type-throw types {:type ret :pointer pointer})]
                           (cond
                             (= 'int (primitives t))
                             'long
                             
                             (#{'double 'long} (primitives t)) ;; only primitives annotateable
                             t
                             
                             :else
                             (wrappers t)))}]
    `(defn ~f-sym
       ~(str "Ret: " ret)                  ;; this docstring is just for fun
       ~(if-let [a (:annotation ret)]
          (symbol (str "^" a))
          (symbol "")                      ;; please forgive me (no annotation on return value)
          )
       ~(->> params
             (map (fn [{:keys [arg-symbol annotation]}]
                    [(when annotation
                       (symbol (str "^" annotation)))
                     arg-symbol]))
             flatten
             (into []))
       ~(let [call (conj (map (fn [{:keys [unwrap arg-symbol]}]
                                (if unwrap
                                  (list unwrap arg-symbol)
                                  arg-symbol))
                              params)
                         (symbol (str (java-friendly lib-name) "/" f-sym)))]
          (if-let [w (:wrapper ret)]
            `(~w ~call)
            call)))))

(def primitives
  #{'int 'char})

(def example-types
  {"void" {"*" 'org.graalvm.nativeimage.c.type.VoidPointer
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

(def example-structs
  {"SDL_Event" {:clj-sym 'SDL_Event
                :c-sym "SDL_Event"
                :ni-class 'bindings.sdl_ni_generated.SDL_Event
                :attrs [{:sym "type" :type "int"}]}
   "SDL_Surface" {:clj-sym 'SDL_Surface
                  :ni-class 'bindings.sdl_ni_generated.SDL_Surface
                  :c-sym "SDL_Surface"
                  :attrs [{:sym "format" :type "SDL_PixelFormat" :pointer "*"}]}
   "SDL_PixelFormat" {:clj-sym 'SDL_PixelFormat
                      :ni-class 'bindings.sdl_ni_generated.SDL_PixelFormat
                      :c-sym "SDL_PixelFormat"
                      :attrs [{:sym "palette" :type "void" :pointer "*"}]}})

(def example-struct-wrappers
  (into {}
        (map (fn [{:keys [clj-sym c-sym]}]
               (let [t (get-type-throw example-types {:type c-sym})]
                 [t (symbol (str (lib->package 'sdl.bindings)
                                 "." (symbol (str "Wrap" clj-sym))))]))
             (vals example-structs))))

(def example-wrappers
  (merge example-struct-wrappers
         {'org.graalvm.nativeimage.c.type.VoidPointer
          'clobits.wrappers.WrapPointer}))



#_(binding [*print-meta* true]
    (-> (gen-clojure-mapping {:ret "int",
                              :sym "SDL_PollEvent",
                              :args [{:type "SDL_Event", :sym "event", :pointer "*"}]}
                             {:prefixes ["_SHADOWING_SDL_"]})
        (gen-defn {:types example-types
                   :structs example-structs
                   :primitives primitives
                   :lib-name 'bindings.sdl})
        prn))

(binding [*print-meta* true]
  (-> ['get-window-surface
       {:ret "SDL_Surface",
        :sym "_SHADOWING_SDL_GetWindowSurface",
        :args [{:type "SDL_Window", :sym "window", :pointer "*"}],
        :pointer "*"}]
      (gen-defn {:types example-types
                 :structs example-structs
                 :primitives primitives
                 :lib-name 'bindings.sdl
                 :wrappers example-wrappers})
      prn))



(comment
  
  
  (binding [*print-meta* true]
    (-> (gen-clojure-mapping {:ret "int",
                              :sym "_SHADOWING_SDL_Init",
                              :args [{:type "Uint32", :sym "flags"}]}
                             {:prefixes ["_SHADOWING_SDL_"]})
        (gen-method {:types example-types})
        prn))
  )

(defn gen-gen-class
  [{:keys [lib-name]}]
  `(pgc/gen-class-native
    :name ~(with-meta (symbol (java-friendly lib-name))
             {org.graalvm.nativeimage.c.CContext (symbol (str (java-friendly lib-name) "_ni.Headers"))
              org.graalvm.nativeimage.c.function.CLibrary (u/so-lib-name-ni lib-name)})))

(comment
  (binding [*print-meta* true]
    (prn
     (gen-gen-class {:lib-name 'wat})))
  )


(defn gen-wrapper-ns
  [{:keys [lib-name clojure-mappings wrappers] :as opts}]
  (concat [`(ns ~(symbol (str (name lib-name) "-wrapper"))
              ~(conj (into '() (into #{} (vals wrappers)))
                     'clobits.all_targets.IWrapperNI
                     :import)
              (:gen-class))]
          (map #(gen-defn % opts) clojure-mappings)))


(defn gen-lib
  [{:keys [lib-name clojure-mappings append-ni] :as opts}]
  (concat [`(ns ~(symbol (str (name lib-name) "-ni"))
              (:require [~'clobits.patch-gen-class]
                        [~'clobits.all-targets])
              (:import org.graalvm.word.PointerBase
                       org.graalvm.nativeimage.c.struct.CField
                       org.graalvm.nativeimage.c.CContext
                       org.graalvm.nativeimage.c.function.CFunction
                       org.graalvm.nativeimage.c.function.CLibrary
                       org.graalvm.nativeimage.c.struct.CFieldAddress
                       org.graalvm.nativeimage.c.struct.CStruct
                       org.graalvm.nativeimage.c.struct.AllowWideningCast
                       org.graalvm.nativeimage.c.function.CFunction
                       org.graalvm.word.WordFactory
                       
                       clobits.all_targets.IVoidPointer
                       
                       [org.graalvm.nativeimage.c.type ~'CCharPointer ~'VoidPointer])
              (:gen-class))
           
           `(deftype ~'Headers
                []
              org.graalvm.nativeimage.c.CContext$Directives
              (~'getHeaderFiles
               [~'_]
               [~(str "\"" (System/getProperty "user.dir") "/" (u/get-h-path opts) "\"")]))]
          
          append-ni
          
          [(reverse (into '() (concat (gen-gen-class opts)
                                      [:methods (into [] (map #(gen-method % opts) clojure-mappings))])))]))

(defn persist-lib
  [{:keys [ni-code wrapper-code java-code lib-name] :as opts}]
  
  (println "Persisting native image clj.")  

  (with-open [wrtr (io/writer (str "src"
                                   "/"
                                   (snake-case (str/replace (str lib-name) "." "/")) "_wrapper.clj"))]
    (.write wrtr ";; This file is autogenerated -- probably shouldn't modify it by hand\n")
    (.write wrtr
            (with-out-str (doseq [f wrapper-code]
                            (pprint f)
                            (print "\n")))))
  
  (with-open [wrtr (io/writer (str "src"
                                   "/"
                                   (snake-case (str/replace (str lib-name) "." "/")) "_ni.clj"))]
    (.write wrtr ";; This file is autogenerated -- probably shouldn't modify it by hand\n")
    (.write wrtr
            (with-out-str (doseq [f ni-code]
                            (binding [*print-meta* true]
                              (prn f))
                            (print "\n")))))
  
  (doseq [{:keys [classname code]} java-code]
    (let [path (str "java-src"
                    "/"
                    (snake-case (str/replace (str classname) "." "/"))
                    ".java")]
      (u/create-subdirs! path)
      (with-open [wrtr (io/writer path)]
        (.write wrtr "// This file is autogenerated -- probably shouldn't modify it by hand\n")
        (.write wrtr code))))
  
  opts)


