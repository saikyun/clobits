(ns clobits.core
  (:require [clojure.string :as str]
            [clobits.all-targets :as at]
            [clobits.util :as u]))

(def int-type {#_#_:ni/wrapper 'int
               :ni/type 'int
               :poly/wrapper '.asInt
               :poly/type 'int
               :primitive true})

(def void-pointer-type {"*" {:interface    'clobits.all_targets.IVoidPointerYE
                             :poly/type    'clobits.all_targets.IVoidPointerYE
                             :ni/interface    'clobits.all_targets.IVoidPointer
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
  [lib-name struct-names]
  (->> struct-names
       (map (fn [sym]
              (let [i-sym (symbol (str lib-name "_structs." "I" sym))]
                [sym {:c-sym sym
                      :primitive    false
                      :interface    i-sym
                      
                      :poly/type    i-sym                                        
                      :poly/unwrap  '.unwrap                      
                      :poly/wrapper                      
                      (with-meta
                        (symbol (str #_ lib-name #_ ".poly/" "wrap-" 
                                     (-> (at/java-friendly sym)
                                         #_(u/remove-prefixes ["SDL_"])
                                         str/lower-case
                                         u/snake->kebab)))
                        {:clobits.core/generate true})
                      
                      :ni/interface (symbol (str lib-name "_ni_generated." sym))
                      :ni/unwrap    '.unwrap
                      :ni/wrapper   (symbol (str lib-name "_ni_generated.Wrap" sym))
                      :ni/java-wrapper   (str "new " lib-name "_ni_generated.Wrap" sym)
                      :ni/type      (symbol (str lib-name "_ni_generated." sym))}])))
       (into {})))

(defn generate-lib-names
  [{:keys [lib-name] :as opts}]
  {:ni/class-name (symbol (at/java-friendly lib-name))
   :ni/wrapper-ns (symbol (str (name lib-name) "-wrapper"))   
   :ni/generator-ns (symbol (str (name lib-name) "-ni"))   
   :ni/header-files [(str "\"" (System/getProperty "user.dir") "/" (u/get-h-path {:src-dir "src", :lib-name lib-name}) "\"")]   
   :ni/context    (symbol (str (at/java-friendly lib-name) "_ni.Headers"))   
   
   :ni/c-lib-name (u/so-lib-name-ni lib-name)
   :ni/so-path    (u/get-so-path-ni opts)
   :poly/so-path  (u/get-so-path opts)
   :poly/ns-name  (symbol (str lib-name "-ns"))
   :c/header-file (str (last (str/split (u/snake-case lib-name) #"/")) ".h")
   
   :c/c-path (u/get-c-path opts)
   :c/h-path (u/get-h-path opts)})
