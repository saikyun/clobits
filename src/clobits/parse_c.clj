(ns clobits.parse-c
  (:require [clojure.string :as str]))

(def arg-s (str "\\s*"
                "(const\\s*)*" ;; prefixes
                "\\s*"
                "(\\w+)"       ;; type
                "((\\*|\\s)+)" ;; space / pointer stars
                "(\\w+)"       ;; symbol
                "\\s*"))

(def prototype-regex
  (re-pattern (str "(;|^)\\s*"     ;; whitespace
                   "(\\w+)"        ;; return type
                   "((\\*|\\s)+)"  ;; space / pointer stars
                   "(\\w+)"        ;; function name
                   "\\(*"          ;; beginning of args
                   "("
                   ,    arg-s      ;; first arg
                   "(," arg-s ")*" ;; more args
                   ")?"
                   "\\s*\\)"       ;; end of args
                   "\\s*(\\{|;|$)" ;; braces and stuff
                   )))

(def arg-regex (re-pattern arg-s))

(defn parse-args
  "Takes a string of c function arguments declaration
  I.e. the code between the parenthesises when declaring a c function.

  ```
  (parse-args \"int a, char* b\")
  ;;=> [{:type \"int\", :sym \"a\"} {:type \"char\", :sym \"b\", :pointer \"*\"}]
  ```"
  [args]
  (when args
    (let [args (str/split args #",")]
      (->> args
           (map #(let [[_ prefix type pointers _ sym] (re-find arg-regex %)
                       prefixes (when prefix (str/split prefix #" "))
                       pointer (when-let [s (seq (str/replace pointers " " ""))] (apply str s))]
                   (cond->
                       {:type type
                        :sym sym}
                     prefix (merge {:prefixes prefixes})
                     pointer (merge {:pointer pointer}))))
           (into [])))))

(comment
  
  )

(defn parse-c-prototype
  "Takes a c-prototype and returns prototype data.
  
  ```
  (parse-c-prototype \"int SDL_Init(Uint32 flags)\")
  ;;=> {:ret \"int\", :sym \"SDL_Init\", :args [{:type \"Uint32\", :sym \"flags\"}]}
  ```"
  [s]
  (try
    (let [[_ _ type pointers _ f-name args] (re-find prototype-regex s)
          args (parse-args args)
          pointer (when-let [s (seq (str/trim pointers))] (apply str s))]
      (cond-> {:ret type 
               :sym f-name}
        args (merge {:args args})
        pointer (merge {:pointer pointer})))
    (catch Exception e
      (println "Failed parsing" (pr-str s))
      (throw e))))

(comment
  (parse-c-prototype "int SDL_Init(Uint32 flags)")
  ;;=> {:ret "int", :sym "SDL_Init", :args [{:type "Uint32", :sym "flags"}]}
  )
