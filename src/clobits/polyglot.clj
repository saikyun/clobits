(ns clobits.polyglot
  (:require [clojure.string :as str]
            [clobits.util :refer [add-prefix-to-sym create-subdirs! get-so-path]]
            [clobits.all-targets :refer [gen-clojure-mapping get-type-throw convert-function-throw]]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pp pprint]]))

(defn struct-sym->interface-sym
  [lib-name sym]
  (symbol (str lib-name "_structs.I" sym)))

(defn attr->poly-method
  [types {:keys [sym] :as arg}]
  [(symbol sym) [] (get-type-throw types arg)])

(defn struct->gen-interface
  [types {:keys [clj-sym attrs]} {:keys [lib-name]}]
  (let [java-friendly-lib-name (str/replace lib-name "-" "_")]
    `(gen-interface
      :name
      ~(symbol (str "^" {org.graalvm.polyglot.HostAccess$Implementable true}))
      ~(symbol (struct-sym->interface-sym lib-name clj-sym))
      
      :methods ~(->> (map #(attr->poly-method types %) attrs)
                     (into [])))))

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
  [[f-sym {:keys [ret pointer sym args]}] {:keys [types poly-conversions lib-name lib-sym structs]}]
  (let [f (if (= ret "void")
            '.executeVoid
            '.execute)
        f-gensym (gensym f-sym)
        ret-struct (get structs ret)
        conv-func (when-not ret-struct
                    (convert-function-throw poly-conversions
                                            (get-type-throw types {:type ret
                                                                   :pointer pointer})))
        wrap-cast (fn [body] (if ret-struct
                               `(~'.as ~body ~(struct-sym->interface-sym lib-name (:clj-sym ret-struct)))
                               body))
        wrap-convert (fn [body]
                       (if (and conv-func (not= conv-func 'identity))
                         `(-> ~body ~conv-func)
                         body))]
    `[(def ~(symbol (str "^" {:private true})) ~f-gensym (.getMember ~lib-sym ~sym))
      (defn ~f-sym
        ~(if-let [as (seq (->> args
                               (map-indexed #(or (:sym %2)
                                                 (str "arg" %1)))
                               (map symbol)))]
           `(~(into [] as)
             ~(-> `(~f ~f-gensym (object-array ~(into [] as)))
                  wrap-cast
                  wrap-convert))
           `([]
             ~(-> `(~f ~f-gensym ~'empty-array)
                  wrap-cast
                  wrap-convert))))]))

(comment
  (-> (gen-clojure-mapping {:ret "int", :sym "SDL_Init", :args [{:type "Uint32", :sym "flags"}]}
                           {:prefixes ["SDL"]})
      (gen-defn {:lib-sym 'sdl-sym}))
  ;;=> [(def init1687 (.getMember sdl-sym "SDL_Init")) (clojure.core/defn init ([flags] (.execute init1687 (clojure.core/object-array [flags]))))]
  )

(defn lib-boilerplate
  [lib-name {:keys [libs] :as opts}]
  (let [lib-sym 'polyglot-lib #_ (gensym (str "lib"))
        context-f-sym (gensym "context-f") 
        context-sym  'polyglot-context
        source-f-sym (gensym "source-f")
        so-path (get-so-path opts)]
    {:lib-name lib-name
     :libs libs
     :lib-sym lib-sym
     :forms (concat [`(ns ~(symbol (str lib-name "-ns"))
                        (:require [clojure.java.io])
                        (:import org.graalvm.polyglot.Context
                                 org.graalvm.polyglot.Source
                                 org.graalvm.polyglot.Value)
                        (:gen-class))
                     `(def ~(symbol (str "^" {:private true})) ~'empty-array (object-array 0))]
                    [`(defn ~context-f-sym
                        []
                        (-> (org.graalvm.polyglot.Context/newBuilder (into-array ["llvm"]))
                            (.allowAllAccess true)
                            #_(.allowIO true)
                            #_(.allowNativeAccess true)
                            (.build)))
                     `(defn ~(symbol (str "^" {:private true})) ~source-f-sym
                        []
                        (-> (org.graalvm.polyglot.Source/newBuilder "llvm" (if (string? ~so-path)
                                                                             (clojure.java.io/file ~so-path)
                                                                             ~so-path))
                            (.build)))
                     `(def ~context-sym (~context-f-sym))
                     `(def ~lib-sym (.eval ~context-sym (~source-f-sym)))])}))

(defn gen-lib*
  [lib-name fns {:keys [append-clj] :as opts}]
  (let [bp (lib-boilerplate lib-name opts)
        defn-forms (apply concat (map #(gen-defn % (assoc opts :lib-sym (:lib-sym bp))) fns))]
    (concat (:forms bp)
            append-clj
            defn-forms)))

(defn gen-lib
  [{:keys [lib-name protos shadow-prefix] :as opts}]
  (let [protos-as-data-shadowed (map (partial add-prefix-to-sym shadow-prefix) protos)
        clojure-mappings (concat
                          (map #(gen-clojure-mapping % 
                                                     {:prefixes ["_SHADOWING_SDL"
                                                                 "_SHADOWING_"]})
                               protos-as-data-shadowed))
        
        clojure-lib (gen-lib* lib-name clojure-mappings opts)]
    (merge opts
           {:clojure-mappings clojure-mappings
            :clojure-lib clojure-lib})))

(defn persist-lib
  [{:keys [src-dir lib-name] :as opts}]
  (let [path (str src-dir
                  "/"
                  (str/replace (str lib-name "_ns") "." "/") ".clj")]
    
    (create-subdirs! path)  
    
    (println "Persisting clj to:" path)
    (with-open [wrtr (io/writer path)]
      (.write wrtr ";; This file is autogenerated -- probably shouldn't modify it by hand\n")
      (.write wrtr
              (with-out-str (doseq [f (:clojure-lib opts)]
                              (pprint f)
                              (print "\n")))))
    
    opts))
