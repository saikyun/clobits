(ns clobits.native-image
  (:require [clobits.patch-gen-class :as pgc]
            [clojure.java.io :as io]
            [clobits.all-targets :as at]
            [clobits.util :as u :refer [snake-case]]
            [clojure.string :as str]
            [clojure.pprint :refer [pprint pp]]))

(defn attr->method
  [{:keys [sym pointer] :as arg} {:keys [typing]}]
  (let [t (at/get-typing-throw typing arg)
        field-or-address (if (and (not (:primitive t))
                                  (not (seq pointer)))
                           'org.graalvm.nativeimage.c.struct.CFieldAddress
                           'org.graalvm.nativeimage.c.struct.CField)]
    (concat ;; getter
     [[(symbol (str "^" {field-or-address sym}))
       (symbol sym)
       
       []
       (or (:ni/interface t) (:ni/type t))]]
     
     ;; setter
     (when (or (:primitive t)
               (seq pointer)) ;; not sure if setters for non-pointer structs can work
       [[(symbol (str "^" {org.graalvm.nativeimage.c.struct.CField sym}))
         (symbol (str "set_" sym))
         
         [(or (:ni/interface t) (:ni/type t))]
         'void]]))))

(defn struct->gen-interface
  [{:keys [c-sym attrs]} {:keys [ni/context typing ni/c-lib-name] :as opts}]
  (let [{:keys [interface] :as t} (at/get-typing-throw typing {:type c-sym})]
    `(gen-interface
      :name ~(symbol (str "^"
                          {org.graalvm.nativeimage.c.CContext context
                           org.graalvm.nativeimage.c.function.CLibrary c-lib-name
                           org.graalvm.nativeimage.c.struct.CStruct c-sym}))
      ~(:ni/interface t)
      :extends [org.graalvm.word.PointerBase ~interface]
      :methods ~(->> (map #(attr->method % opts) attrs)
                     (apply concat)
                     (into [])))))

(defn gen-getter
  [{:keys [sym] :as attr} {:keys [typing]}]
  (let [t (at/get-typing-throw typing attr)
        w (:ni/java-wrapper t)]
    (str "  public " (or (:ni/wrapper t) (:ni/type t)) " " sym "() {
    " (if w
        (str "return " w "("
             ,  "this.pointer." sym "()"
             ");")
        (str "return this.pointer." sym "();"))
         "
  }")))

(defn gen-setters
  [{:keys [sym] :as attr} {:keys [typing]}]
  (let [t (at/get-typing-throw typing attr)
        w (:ni/java-wrapper t)
        specific-input-type (or (:ni/wrapper t) (:ni/type t))
        input-type (or (:interface t) (:ni/type t))]
    (if w
      (str (str "  public void set_" sym "(" specific-input-type " v) {
    " (str "this.pointer.set_" sym "(v.unwrap());")
                "
  }\n\n")
           
           (str "  public void set_" sym "(" input-type " v) {
    " (str "this.pointer.set_" sym "(v);")
                "
  }"))
      
      
      
      (str "  public void set_" sym "(" specific-input-type " v) {
    " (str "this.pointer.set_" sym "(v);")
           "
  }")
      
      )))

(defn struct->gen-wrapper-class
  [{:keys [c-sym attrs]} {:keys [typing] :as opts}]
  (let [funcs (str/join "\n\n" (concat (map #(gen-getter % opts) attrs)
                                       (map #(gen-setters % opts) attrs)))
        {:keys [ni/wrapper ni/type interface]} (at/get-typing-throw typing {:type c-sym})
        classname (at/unqualify-class wrapper)
        type (at/unqualify-class type)]
    {:classname wrapper
     :code (str
            "
package " (at/class-name->package wrapper) ";

import " interface ";
import clobits.all_targets.IWrapperNI;

public class " classname " implements " (at/unqualify-class interface) ", IWrapperNI {
  " type " pointer;

  // used when sending data to native functions
  public " type " unwrap() {
    return this.pointer;
  }

" funcs "

  public " classname "(" type " pointer) {
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

(defn gen-method
  "Generates a method that calls a native function."
  [[f-sym {:keys [ret pointer sym args]}] {:keys [typing]}]
  (let [rt (at/get-typing-throw typing {:type ret, :pointer pointer})]
    [(symbol (str "^" {:static true, :native true}))
     [(symbol (str "^" {'org.graalvm.nativeimage.c.function.CFunction
                        {:transition 'org.graalvm.nativeimage.c.function.CFunction$Transition/NO_TRANSITION
                         :value sym}}))
      (symbol (str/replace (name f-sym) "-" "_"))
      
      (into [] (map #(-> (at/get-typing-throw typing %)
                         :ni/type) args))
      (or (:ni/interface rt) (:ni/type rt))]]))

(defn gen-defn
  "Generates a defn-call, which calls a method.
  The function resulting from evaluating the defn-call will wrap / unwrap native values / wrappers as needed."
  [[f-sym {:keys [ret pointer args]}] {:keys [typing ni/class-name]}]
  (let [param-primitives (>= 4 (count args)) ;; primitive annotation only works with 4 or fewer args
        
        get-primitive-annotation (fn [typing]
                                   (let [t (if (= 'int (:ni/type typing))
                                             'long
                                             (#{'double 'long} (:ni/type typing)))]
                                     (if param-primitives
                                       t                                           ;; e.g. 'long
                                       (some-> t str str/capitalize symbol))))     ;; e.g. 'Long
        
        get-ret-annotation (fn [typing]
                             (if (not (:primitive typing))
                               (or (:ni/wrapper typing) (:ni/type typing))
                               (get-primitive-annotation typing)))
        
        get-param-annotation (fn [typing]
                               (if (not (:primitive typing))
                                 'clobits.all_targets.IWrapperNI
                                 (get-primitive-annotation typing)))    
        
        params (->> args
                    (map-indexed (fn [i arg] (update arg :sym #(or % (str "arg" i)))))
                    (map #(let [t (at/get-typing-throw typing %)]
                            (merge t
                                   {:annotation (get-param-annotation t)
                                    :arg-symbol (-> % :sym symbol)})))
                    (into []))
        
        ret-typing (at/get-typing-throw typing {:type ret, :pointer pointer})
        ret-annotation (get-ret-annotation ret-typing)]
    `(defn ~f-sym
       ~(if-let [a ret-annotation]
          (symbol (str "^" a))
          (symbol "") ;; please forgive me (no annotation on return value)
          )
       ~(->> params
             (map (fn [{:keys [arg-symbol annotation]}]
                    [(when annotation
                       (symbol (str "^" annotation)))
                     arg-symbol]))
             flatten
             (filter some?)
             (into []))
       ~(let [call (conj (map (fn [{:keys [ni/unwrap arg-symbol]}]
                                (if unwrap
                                  (list unwrap arg-symbol)
                                  arg-symbol))
                              params)
                         (symbol (str class-name "/" f-sym)))]
          (if-let [w (some->> (:ni/wrapper ret-typing) (str "new ") symbol)]
            `(~w ~call)
            call)))))

(defn gen-gen-class
  [{:keys [ni/c-lib-name ni/context ni/class-name]}]
  `(pgc/gen-class-native
    :name ~(symbol (str "^" {org.graalvm.nativeimage.c.CContext context
                             org.graalvm.nativeimage.c.function.CLibrary c-lib-name}))
    ~class-name))

(defn gen-wrapper-ns
  [{:keys [clojure-mappings wrappers ni/wrapper-ns] :as opts}]
  (concat [`(ns ~wrapper-ns
              ~(conj (into '() (into #{} (vals wrappers)))
                     'clobits.all_targets.IWrapperNI
                     :import)
              (:gen-class))]
          (map #(gen-defn % opts) clojure-mappings)))

(defn gen-lib
  [{:keys [clojure-mappings structs ni/generator-ns ni/header-files] :as opts}]
  (concat [`(ns ~generator-ns
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
               ~header-files))]
          
          (map #(struct->gen-interface % opts) (vals structs))
          
          [(reverse (into '() (concat (gen-gen-class opts)
                                      [:methods (into [] (apply concat (map #(gen-method % opts) clojure-mappings)))])))]))

(defn persist-lib
  [{:keys [ni-code wrapper-code java-code src-dir ni/wrapper-ns ni/generator-ns] :as opts}]
  
  (println "Persisting native image clj.")
  
  (letfn [(pathify [dir s ext] (str dir "/" (snake-case (str/replace (str s) "." "/")) ext))]
    (with-open [wrtr (io/writer (pathify src-dir wrapper-ns ".clj"))]
      (.write wrtr ";; This file is autogenerated -- probably shouldn't modify it by hand\n")
      (.write wrtr
              (with-out-str (doseq [f wrapper-code]
                              (pprint f)
                              (print "\n")))))
    
    (with-open [wrtr (io/writer (pathify src-dir generator-ns ".clj"))]
      (.write wrtr ";; This file is autogenerated -- probably shouldn't modify it by hand\n")
      (.write wrtr
              (with-out-str (doseq [f ni-code]
                              (pprint f)
                              (print "\n")))))
    
    (doseq [{:keys [classname code]} java-code]
      (let [path (pathify "java-src" classname ".java")]
        (println "Writing java to:" path)
        (u/create-subdirs! path)
        (with-open [wrtr (io/writer path)]
          (.write wrtr "// This file is autogenerated -- probably shouldn't modify it by hand\n")
          (.write wrtr code)))))
  
  opts)


