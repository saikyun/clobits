(ns clobits.polyglot
  (:require [clojure.string :as str]
            [clobits.util :refer [add-prefix-to-sym create-subdirs! get-so-path]]
            [clobits.all-targets :refer [gen-clojure-mapping get-type-throw convert-function-throw
                                         get-typing-throw
                                         struct-sym->interface-sym java-friendly]]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pp pprint]]))

(defn attr->poly-method
  [typing {:keys [sym type] :as arg}]
  (let [{:keys [poly/type]} (get-typing-throw typing arg)]
    [[(symbol sym) [] type]
     [(symbol (str "set_" sym)) [type] 'void]]))

(defn struct->gen-interface
  [{:keys [clj-sym attrs]} {:keys [lib-name typing]}]
  (let [java-friendly-lib-name (str/replace lib-name "-" "_")]
    `(gen-interface
      :name
      ~(symbol (str "^" {org.graalvm.polyglot.HostAccess$Implementable true}))
      ~(symbol (struct-sym->interface-sym lib-name clj-sym))
      
      :methods ~(->> (map #(attr->poly-method typing %) attrs)
                     (apply concat)
                     (into [])))))

(defn gen-defn
  "Generates a defn-call, which calls a method.
  The function resulting from evaluating the defn-call will wrap / unwrap native values / wrappers as needed."
  [[f-sym {:keys [ret pointer sym args] :as func}] {:keys [lib-sym typing]}]
  (let [f (if (= ret "void")
            '.executeVoid
            '.execute) 
        place-of-f-sym (symbol (str "-place-of-" f-sym)) #_(gensym f-sym)
        
        param-primitives (>= 4 (count args))
        
        get-annotation (fn [typing]
                         (if (not (:primitive typing))
                           (:poly/type typing)
                           (let [t (if (= 'int (:poly/type typing))
                                     'long
                                     (#{'double 'long} (:poly/type typing)))]
                             (if param-primitives
                               t                                        ; e.g. 'long
                               (some-> t str str/capitalize symbol)))))    ; e.g. 'Long
        
        params (->> args
                    (map-indexed (fn [i arg] (update arg :sym #(or % (str "arg" i)))))
                    (map #(let [t (get-typing-throw typing %)]
                            (merge t
                                   {:annotation (get-annotation t)
                                    :arg-symbol (-> % :sym symbol)})))
                    (into []))
        
        ret-typing (get-typing-throw typing {:type ret, :pointer pointer})
        ret-annotation (get-annotation ret-typing)]
    [`(def ~(symbol (str "^" {:private true})) ~place-of-f-sym (.getMember ~lib-sym ~sym))
     `(defn ~f-sym
        ~(str "Args:" params ", "
              "Ret: " ret-typing) ;; this docstring is just for fun
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
        ~(let [call `(~f ~place-of-f-sym (object-array ~(into []
                                                              (map (fn [{:keys [poly/unwrap arg-symbol]}]
                                                                     (if unwrap
                                                                       (list unwrap arg-symbol)
                                                                       arg-symbol))
                                                                   params))))]
           (if-let [w (:poly/wrapper ret-typing)]
             `(~w ~call)
             call)))]))

(defn lib-boilerplate
  [lib-name {:keys [libs] :as opts}]
  (let [lib-sym 'polyglot-lib #_ (gensym (str "lib"))
        context-f-sym 'context-f #_ (gensym "context-f") 
        context-sym  'polyglot-context
        source-f-sym 'source-f #_ (gensym "source-f")
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


(defn constructors
  [{:keys [typing structs]}]
  (let [gen-wrappers (->> (vals typing)
                          (into #{})
                          (filter #(:clobits.core/generate (meta (:poly/wrapper %)))))]
    
    (concat
     [(let [value-sym 'value]
        `(defn ~(symbol "wrap-pointer") [~value-sym]
           (reify
             ~'clobits.all_targets.IWrapper
             (~'unwrap [~'_]
              ~value-sym))))]
     
     [(conj (map :poly/wrapper gen-wrappers) `declare)]
     
     (map (fn [{:keys [c-sym poly/wrapper interface]}]
            (let [{:keys [attrs]} (structs c-sym)
                  value-sym 'value]
              `(defn ~(symbol wrapper) [~value-sym]
                 ~(concat
                   `(reify ~interface)
                   
                   (->> (for [{:keys [sym] :as attr} attrs
                              :let [{:keys [poly/wrapper poly/unwrap]}
                                    (get-typing-throw typing attr)
                                    wrapper #(if wrapper
                                               `(~wrapper ~%)
                                               %)
                                    unwrapper #(if unwrap
                                                 `(~unwrap ~%)
                                                 %)]]
                          [`(~(symbol sym) [~'_]
                             ~(wrapper `(~'.getMember ~value-sym ~sym)))
                           
                           `(~(symbol (str "set_" sym)) [~'_ ~'v]
                             ~`(~'.putMember ~value-sym ~sym ~(unwrapper 'v)))])
                        (apply concat))
                   
                   `(~'clobits.all_targets.IWrapper
                     (~'unwrap [~'_]
                      (~'.as ~value-sym ~interface)))))))
          gen-wrappers))))

(defn gen-lib*
  [lib-name fns {:keys [structs] :as opts}]
  (let [bp (lib-boilerplate lib-name opts)
        defn-forms (apply concat (map #(gen-defn % (assoc opts :lib-sym (:lib-sym bp))) fns))]
    (concat (:forms bp)
            (map #(struct->gen-interface % opts) (vals structs))
            (constructors opts)
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
