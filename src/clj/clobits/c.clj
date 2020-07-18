(ns clobits.c
  (:require [clobits.native-interop :refer [*native-image*]]))

(defmacro with-heap
  [bindings & body]
  (@#'clojure.core/assert-args
   (vector? bindings) "a vector for its bindings"
   (even? (count bindings)) "an even number of forms in binding vector")
  (cond (= 0 (count bindings)) `(do ~@body)
        
        (symbol? (bindings 0))
        `(let ~(subvec bindings 0 2)
           (try (with-heap ~(subvec bindings 2) ~@body)
                (finally
                  (ncurses/free ~(bindings 0)))))
        
        :else (throw (IllegalArgumentException.
                      "with-heap only allows Symbols in bindings"))))

(defmacro with-window
  [bindings & body]
  (@#'clojure.core/assert-args
   (vector? bindings) "a vector for its bindings"
   (even? (count bindings)) "an even number of forms in binding vector")
  (cond (= 0 (count bindings)) `(do ~@body)
        
        (symbol? (bindings 0))
        `(let ~(subvec bindings 0 2)
           (try (with-heap ~(subvec bindings 2) ~@body)
                (finally
                  (ncurses/delwin ~(bindings 0)))))
        
        :else (throw (IllegalArgumentException.
                      "with-heap only allows Symbols in bindings"))))

#_(defn string->char-pointer
    [s]
    (let [size (inc (count s))
          str (ncurses/string size)]
      (doseq [i (range 0 (count s))
              :let [c (nth s i)]]
        (ncurses/append-char str i c))
      str))

(defmacro char*
  "More efficient than `str*` but single arity."
  [s]
  (if *native-image*
    `(.get (org.graalvm.nativeimage.c.type.CTypeConversion/toCString ~s))
    s))

(defmacro str*
  "Behaves like `clojure.core/str`.
  Returns a char* or polyglot value depending on context."
  ([& ss]
   (let [s# `(apply str ~(into [] ss))]
     (if *native-image*
       `(clobits.wrappers.WrapPointer. (.get (org.graalvm.nativeimage.c.type.CTypeConversion/toCString ~s#)))
       s#))))

