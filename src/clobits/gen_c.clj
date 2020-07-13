(ns clobits.gen-c
  (:require [clojure.string :as str]
            [clobits.util :as u :refer [add-prefix-to-sym create-subdirs! snake-case get-c-path get-h-path get-so-path]]
            [clojure.java.shell :refer [sh]]))

(defn get-arg
  [i {:keys [sym]}]
  (if-not sym (str "arg" i) sym))

(defn gen-args
  [args]
  (str/join ", " (map-indexed (fn [i {:keys [type pointer sym prefixes]}]
                                (if (= type 'variadic)
                                  "..."
                                  (str (str/join " " prefixes) " " type " " pointer " " 
                                       (if-not sym (str "arg" i)
                                               sym)))) args)))

(defn is-char*?
  [t]
  (and (= "char" (:type t))
       (some? (:pointer t))))

(defn wrap-char*
  [args ret-f]
  (let [char-args (filter is-char*? args)
        new-syms (into {} (map-indexed #(vector (:sym %2) (str "_wrap_" (:sym %2) %1)) char-args))
        declarations (map #(str (apply str (:prefixes %)) " char *" (new-syms (:sym %)) ";") char-args)
        setters (map #(let [new-sym (new-syms (:sym %))]
                        (str
                         "if (polyglot_is_string(" (:sym %) ")) {
 int length = polyglot_get_string_size(" (:sym %) ");
 char str[length + 1];
 polyglot_as_string(" (:sym %) ", str, length + 1, \"ascii\");
 " new-sym " = str;
 } else {
 " new-sym " = " (:sym %) ";
 }"))
                     char-args)
        new-args (map (fn [a] (update a :sym #(if-let [sym (new-syms %)]
                                                sym
                                                %))) args)]
    
    (str
     "
#if IS_POLYGLOT
"
     (str/join "\n" declarations)
     "\n"
     (str/join "\n" setters)
     "
 " (ret-f new-args) "
#else
  " (ret-f args) "
#endif
"
     ))
  
  )

(defn generate-shadowing-function
  "Takes prototype data and generates a c function declaration.
  
  ```
  (generate-shadowing-function {:ret \"int\", :sym \"SDL_Init\", :args [{:type \"Uint32\", :sym \"flags\"}]})
  ;;=> \"int  _SHADOWING_SDL_Init(Uint32  flags) {\\n  return SDL_Init(flags);\\n}\"
  ```"
  [{:keys [ret sym pointer args]}]
  (let [args (map-indexed (fn [i arg]
                            (update arg :sym #(if % % (str "arg" i)))) args)
        ret-part-f
        (fn [args]
          (str "  " (when (not (and (= ret "void")
                                    (nil? pointer))) "return ")
               sym "(" (str/join ", " (map-indexed get-arg args)) ");"))]
    (str ret
         " "
         pointer
         " _SHADOWING_"
         sym
         "(" (gen-args args) ") {\n"
         
         (if (seq (filter is-char*? args))
           (wrap-char* args ret-part-f)
           (ret-part-f args))
         
         "\n}")))

(comment
  (generate-shadowing-function {:ret "int", :sym "SDL_Init", :args [{:type "Uint32", :sym "flags"}]})
  ;;=> "int  _SHADOWING_SDL_Init(Uint32  flags) {\n  return SDL_Init(flags);\n}"
  
  (generate-shadowing-function {:ret "int", :sym "printw", :args [{:type "char", :pointer "*", :sym "flags"}]})
  )

(defn generate-c-prototype
  "Takes prototype data and generates a c function prototype."
  [{:keys [ret sym pointer args]}]
  (str ret
       " "
       pointer
       " "
       sym
       "(" (gen-args args) ");"))

(defn gen-c-file
  [includes fns & [{:keys [inline-c]}]]
  (let [incs (str/join "\n" (map #(str "#include \"" % "\"") includes))]
    (str (when (seq incs)
           (str "// includes\n"
                incs
                "\n"))
         (when inline-c
           (str "\n// inline-c\n"
                inline-c
                "\n"))
         (when (seq fns)
           (str "\n// fns\n"
                (str/join "\n" fns))))))

(defn gen-h-file
  [includes fn-declarations]
  (let [incs (str/join "\n"(map #(str "#include <" % ">") includes))]
    (str incs
         "\n\n"
         (str/join "\n" fn-declarations))))

(defn gen-lib
  [{:keys [lib-name includes protos shadow-prefix] :or {shadow-prefix "_SHADOWING_"} :as opts}]
  (let [c-code (str (gen-c-file [(str (last (str/split (snake-case lib-name) #"/")) ".h")]
                                (map generate-shadowing-function protos)
                                opts))
        protos-as-data-shadowed (map (partial add-prefix-to-sym shadow-prefix) protos)
        h-code (str "#if IS_POLYGLOT
#include <polyglot.h>
#endif\n"
                    (gen-h-file includes
                                (map generate-c-prototype protos-as-data-shadowed)))]
    {:shadow-prefix shadow-prefix    
     :c-code c-code
     :h-code h-code}))

(defn compile-c
  [{:keys [c-code h-code libs] :as opts}]
  (let [c-path (get-c-path opts) 
        h-path (get-h-path opts)]
    
    (create-subdirs! c-path)
    (create-subdirs! h-path)
    
    (println "Spitting c-code:" (apply str (take 100 c-code)) "...\n")
    (spit c-path (str c-code))
    
    (println "Spitting h-code:" (apply str (take 100 h-code)) "...\n")
    (spit h-path h-code)
    
    (println "Going to compile...")
    
    (let [r1 (let [sh-opts (concat [(str (System/getenv "LLVM_TOOLCHAIN") "/clang") c-path]
                                   (map #(str "-l" %) (conj libs "polyglot-mock"))
                                   ["-shared" "-fPIC" "-D" "IS_POLYGLOT=1" "-o" (get-so-path opts)])]
               (apply sh sh-opts))
          
          r2 (let [sh-opts (concat [(str (System/getenv "LLVM_TOOLCHAIN") "/clang") c-path]
                                   (map #(str "-l" %) libs)
                                   ["-shared" "-fPIC" "-o" (u/get-so-path-ni opts)])]
               (apply sh sh-opts))]
      (println "Compilation results:")
      (println r1)
      (println r2)
      [r1 r2])))

(defn persist-lib!
  [opts]
  (let [{:keys [err]} (compile-c opts)]
    (when (seq err) 
      (println "ERROR:" err)
      (println "Compilation failed with options:" opts)
      (throw (Error. err)))))
