(ns clobits.gen-clj
  "Functions that are used by both native-image and polyglot namespaces."
  (:require [clobits.util :refer [remove-prefixes snake->kebab]]))

(defn gen-clojure-mapping
  "Takes proto data and creates a pair.
  The key is a symbol looking like a clojure function name.
  The value is the proto data.
  
  ```  
  (gen-clojure-mapping {:ret \"int\", :sym \"SDL_Init\", :args [{:type \"Uint32\", :sym \"flags\"}]}
                       {:prefixes [\"SDL\"]})
  ;;=> [init {:ret \"int\", :sym \"SDL_Init\", :args [{:type \"Uint32\", :sym \"flags\"}]}]
  ```
  "  
  
  [{:keys [sym] :as f} & [{:keys [prefixes kebab] :or {kebab true}}]]
  [(symbol (cond-> sym
             prefixes (remove-prefixes prefixes)
             kebab snake->kebab))
   f])

(comment
  (gen-clojure-mapping {:ret "int", :sym "SDL_Init", :args [{:type "Uint32", :sym "flags"}]}
                       {:prefixes ["SDL"]})
  ;;=> [init {:ret "int", :sym "SDL_Init", :args [{:type "Uint32", :sym "flags"}]}]
  )
