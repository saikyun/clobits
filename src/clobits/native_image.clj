(ns clobits.native-image
  (:require [clobits.patch-gen-class :as pgc]
            [clojure.java.io :as io]
            [clobits.all-targets :refer [gen-clojure-mapping get-type-throw]]
            [clobits.util :as u :refer [snake-case no-subdir]]
            [clojure.string :as str]
            [clojure.pprint :refer [pprint pp]]))

(defn java-friendly
  [sym]
  (str/replace (name sym) "-" "_"))

(defn lib->package
  [lib-name]
  (str lib-name "_ni_generated"))

(defn sym->classname
  [lib-name clj-sym]
  (str (lib->package lib-name) "." (java-friendly clj-sym)))

(defn attr->method
  [types {:keys [sym] :as arg}]
  [(with-meta (symbol sym)
     {org.graalvm.nativeimage.c.struct.CField sym}) []
   (get-type-throw types arg)])

(defn struct->gen-interface
  [types {:keys [c-sym clj-sym attrs]} {:keys [lib-name]}]
  (let [java-friendly-lib-name (java-friendly lib-name)
        context (symbol (str java-friendly-lib-name "_ni.Headers"))]
    `(gen-interface
      :name ~(with-meta #_clj-sym (symbol (sym->classname lib-name clj-sym))
               {org.graalvm.nativeimage.c.CContext context
                org.graalvm.nativeimage.c.function.CLibrary (u/so-lib-name-ni lib-name)
                org.graalvm.nativeimage.c.struct.CStruct c-sym})
      :extends [org.graalvm.word.PointerBase
                ~(symbol (str java-friendly-lib-name "_structs.I" (java-friendly clj-sym)))]
      :methods ~(->> (map #(attr->method types %) attrs)
                     (into [])))))

(defn gen-getter
  [types {:keys [type sym] :as attr}]
  (str "  public " (get-type-throw types attr) " " sym "() {
    return this.pointer." sym "();
  }"))

(defn struct->gen-wrapper-class
  [types {:keys [c-sym clj-sym attrs] :as s} {:keys [lib-name]}]
  (let [_ (println "s" s)
        _ (println "clj-sym" clj-sym)
        classname (str "Wrap" (java-friendly clj-sym))
        funcs (str/join "\n\n" (map #(gen-getter types %) attrs))]
    {:classname (str (lib->package lib-name) "." classname)
     :code (str ;; unused since same package.  "import " (sym->classname lib-name clj-sym) ";"
            "
package " (lib->package lib-name) ";

import " lib-name "_structs.I" (java-friendly clj-sym) ";
import clobits.all_targets.IWrapper;

public class " classname " implements I" (java-friendly clj-sym) ", IWrapper {
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

(comment

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
  
  (println
   (struct->gen-wrapper-class ttt {:clj-sym 'SDL_Event
                                   :c-sym "SDL_Event"
                                   :attrs [{:sym "type" :type "int"}]}
                              {:lib-name 'bindings.sdl})
   )
  
  (struct->gen-interface ttt {:clj-sym 'SDL_Event
                              :c-sym "SDL_Event"
                              :attrs [{:sym "type" :type "int"}]}
                         {:lib-name 'sdl})
  
  (struct->gen-wrapper-class ttt {:clj-sym 'SDL_Event
                                  :c-sym "SDL_Event"
                                  :attrs [{:sym "type" :type "int"}]}
                             {:lib-name 'sdl}))

(defn gen-defn
  "Takes kv pair, where k is a clojure symbol and v is proto data.
  Generates `defn`-calls.
  Needs `{:lib-sym ...}` as second argument.
  This should be a symbol declared above the defn-call, which contains a polyglot library.
  
  ```
  (-> (gen-clojure-mapping {:ret \"int\", :sym \"SDL_Init\", :args [{:type \"Uint32\", :sym \"flags\"}]}
                           {:prefixes [\"SDL\"]})
      (gen-defn {:lib-sym 'sdl-sym}))
   [(def init1687 (.getMember sdl-sym \"SDL_Init\"))
    (clojure.core/defn init ([flags] (.execute init1687 (clojure.core/object-array [flags]))))]
  ```"
  [[f-sym {:keys [ret pointer sym args]}] {:keys [types]}]
  (-> [(with-meta (symbol (str/replace (name f-sym) "-" "_"))
         {'org.graalvm.nativeimage.c.function.CFunction
          {:transition 'org.graalvm.nativeimage.c.function.CFunction$Transition/NO_TRANSITION
           :value sym}})
       (into [] (map (partial get-type-throw types) args))
       (get-type-throw types {:type ret, :pointer pointer})]
      (with-meta {:static true, :native true})))

(comment
  (binding [*print-meta* true]
    (-> (gen-clojure-mapping {:ret "int",
                              :sym "_SHADOWING_SDL_Init",
                              :args [{:type "Uint32", :sym "flags"}]}
                             {:prefixes ["_SHADOWING_SDL_"]})
        (gen-defn {:types types}))))

(defn gen-gen-class
  [{:keys [lib-name]}]
  (let [java-friendly-lib-name (str/replace lib-name "-" "_")]
    `(pgc/gen-class-native
      :name ~(with-meta (symbol (str java-friendly-lib-name))
               {org.graalvm.nativeimage.c.CContext (symbol (str java-friendly-lib-name "_ni.Headers"))
                org.graalvm.nativeimage.c.function.CLibrary (u/so-lib-name-ni lib-name)}))))

(comment
  (binding [*print-meta* true]
    (prn
     (gen-gen-class {:lib-name 'wat})))
  )

(defn gen-lib
  [{:keys [lib-name clojure-mappings append-ni] :as opts}]
  (concat [`(ns ~(symbol (str (name lib-name) "-ni"))
              (:require [~'clobits.patch-gen-class])
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
                                      [:methods (into [] (map #(gen-defn % opts) clojure-mappings))])))]))

(defn persist-lib
  [{:keys [ni-code java-code lib-name] :as opts}]
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


