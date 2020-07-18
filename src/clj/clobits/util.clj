(ns clobits.util
  "Functions that are usable by multiple targets.
  Generally have to do with naming and directories."
  (:require [clojure.string :as str]
            [clojure.java.io :as io]))

(defn snake->kebab
  "Turns snake casing to kebab-casing -- i.e. how clojure functions look"
  [s]
  (-> (name s)
      (str/replace #"^_" "")
      (str/replace #"(?!^)_" "-")
      (str/replace #"(?!^)([a-z][A-Z])" #(str (apply str (butlast (first %1))) "-" (second (second %1))))
      str/lower-case))

(defn remove-prefixes
  [s prefixes]
  (str/replace s (re-pattern (str/join "|" (map #(str "^" %) prefixes))) ""))

(defn snake-case
  [s]
  (-> s
      (str/replace "-" "_")
      (str/replace "." "/")))

(defn no-subdir
  [s]
  (snake-case (str/replace s "." "$")))

(defn get-c-path
  [{:keys [c-src-dir lib-name]}]
  (str c-src-dir "/" (snake-case lib-name) ".c"))

(defn get-h-path
  [{:keys [c-src-dir lib-name]}]
  (str c-src-dir "/" (snake-case lib-name) ".h"))

(defn so-lib-name-ni
  [s]
  (str (no-subdir s) "-ni"))

(defn get-so-path-ni
  [{:keys [lib-dir lib-name]}]
  (str lib-dir "/lib" (so-lib-name-ni lib-name) ".so"))

(defn get-so-path
  [{:keys [lib-dir lib-name]}]
  (str lib-dir "/lib" (no-subdir lib-name) ".so"))

(defn add-prefix-to-sym
  [prefix m]
  (update m :sym #(str prefix %)))

(defn create-subdirs!
  [path]
  (doseq [d (->> (reduce
                  (fn [acc curr]
                    (conj acc (str (last acc) "/" curr)))
                  []
                  (butlast (str/split path #"/")))
                 (map #(io/file (str (System/getProperty "user.dir") "/" %))))]
    #_ (println "Creating dir" d)
    (.mkdir d)))
