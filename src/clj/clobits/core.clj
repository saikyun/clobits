(ns clobits.core
  (:require [clojure.string :as str]
            [clobits.all-targets :as at]
            [clobits.util :as u]
            [clobits.polyglot :as gp]
            [clobits.gen-c :as gcee]
            [clobits.native-image :as ni]))

(def int-type {#_#_:ni/wrapper 'int
               :ni/type 'int
               :poly/wrapper '.asInt
               :poly/type 'int
               :primitive true})

(def void-pointer-type {"*" {:interface    'clobits.wrappers.IVoidPointerYE
                             :poly/type    'clobits.wrappers.IVoidPointerYE
                             :ni/interface    'clobits.wrappers.IVoidPointer
                             :ni/type      'org.graalvm.nativeimage.c.type.VoidPointer
                             :ni/wrapper   'clobits.wrappers.WrapVoid
                             :ni/java-wrapper "new clobits.wrappers.WrapVoid"
                             :ni/unwrap    '.unwrap
                             :poly/wrapper 'wrap-pointer
                             :poly/unwrap  '.unwrap
                             :primitive false}})

(def default-typing
  {"int"    int-type
   "Uint32" int-type
   "Uint16" int-type
   "Sint16" int-type
   "Uint8"  int-type
   
   "char" {"*" {:ni/type   'org.graalvm.nativeimage.c.type.CCharPointer
                :ni/wrapper 'clobits.wrappers.WrapPointer
                :ni/unwrap '.unwrap
                :poly/type nil
                :primitive false}
           :poly/type 'char
           :ni/type 'char
           :primitive true}    
   
   "void" (merge void-pointer-type
                 {:ni/type 'void
                  :primitive true})})

(defn generate-struct-typing
  [{:keys [lib-name prefixes]} struct-names]
  (->> struct-names
       (map (fn [sym]
              (let [tiny-sym (u/remove-prefixes sym prefixes)
                    i-sym (symbol (str lib-name "." tiny-sym))]
                [sym {:c-sym sym
                      :primitive    false
                      :interface    i-sym
                      
                      :poly/type    i-sym
                      :poly/unwrap  '.unwrap
                      :poly/wrapper
                      (with-meta
                        (symbol (str "wrap-" 
                                     (-> (at/java-friendly tiny-sym)
                                         str/lower-case
                                         u/snake->kebab)))
                        {:clobits.core/generate true})
                      
                      :ni/interface (symbol (str lib-name ".ni.I" tiny-sym))
                      :ni/unwrap    '.unwrap
                      :ni/wrapper   (symbol (str lib-name ".ni." tiny-sym "Wrapper"))
                      :ni/java-wrapper   (str "new " lib-name ".ni." tiny-sym "Wrapper")
                      :ni/type      (symbol (str lib-name ".ni.I" tiny-sym))}])))
       (into {})))

(defn generate-lib-names
  [{:keys [lib-name] :as opts}]
  {:structs-ns (symbol (str (name lib-name) ".structs"))
   
   :ni/class-name (symbol (str (at/java-friendly lib-name) ".ni.interop"))
   :ni/wrapper-ns (symbol (str (name lib-name) ".ni"))
   :ni/generator-ns (symbol (str (name lib-name) ".ni.generate"))
   :ni/header-files [(str "\"" (System/getProperty "user.dir") "/" (u/get-h-path opts) "\"")]
   :ni/context    (symbol (str (at/java-friendly lib-name) ".ni.generate.Headers"))
   
   :ni/c-lib-name (u/so-lib-name-ni lib-name)
   :ni/so-path    (u/get-so-path-ni opts)
   :poly/so-path  (u/get-so-path opts)
   :poly/ns-name  (symbol (str lib-name ".poly"))
   :c/header-file (str (last (str/split (u/snake-case lib-name) #"/")) ".h")
   
   :c/c-path (u/get-c-path opts)
   :c/h-path (u/get-h-path opts)})

(defn gen-and-persist!
  [opts]
  (println "Generating " (:lib-name opts))  
  (let [opts (merge opts (gcee/gen-lib opts))
        _ (gcee/persist-lib! opts)
        opts (gp/gen-lib opts)
        opts (gp/persist-lib opts)
        opts (assoc opts :ni-code (ni/gen-lib opts))
        opts (assoc opts :wrapper-code (ni/gen-wrapper-ns opts))
        opts (assoc opts :java-code (map #(ni/struct->gen-wrapper-class % opts) (vals (:structs opts))))
        opts (ni/persist-lib opts)]
    (println "Done!")
    opts))
