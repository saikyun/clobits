(ns test-examples
  (:require [digest :as d]
            [clojure.java.io :as io]))

(d/md5 (io/as-file "test-example/src/sdl_ns.clj"))
