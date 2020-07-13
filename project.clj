(defproject clobits "0.1.0"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [digest "1.4.9"]]
  :plugins [[lein-exec "0.3.7"]]
  :source-paths ["src"]
  :jvm-opts []
  :resources ["src" "libs"]
  :resource-paths ["classes"]
  
  :profiles {:compare-files {:dependencies [[digest "1.4.9"]]}
             
             :with-java {:java-source-paths ["java-src"]
                         :aot :all}
             
             :compile-clobits {:aot [clobits.all-targets]
                               :compile-path "classes/"}
             
             :compile-sdl {:aot [bindings.sdl-ns bindings.sdl-ni]
                           :compile-path "classes/"}
             
             :uberjar {:global-vars {*assert* false}
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"
                                  "-Dclojure.spec.skip-macros=true"]                           
                       :aot :all}
             
             :sdl-uberjar {:main clobits.examples.sdl.startup
                           :uberjar-name "examples_sdl.jar"}
             
             :ncurses-uberjar {:main clobits.examples.ncurses.startup
                               :uberjar-name "examples_ncurses.jar"}
             
             :bounce-uberjar {:main clobits.examples.ncurses.bounce-startup
                              :uberjar-name "examples_bounce.jar"}
             
             :sdl-poly {:main clobits.examples.sdl.startup}
             
             :ncurses-poly {:main clobits.examples.ncurses.startup}
             
             :bounce-poly {:main clobits.examples.ncurses.bounce-startup}
             
             
             :macos {:jvm-opts [;; sdl function regarding the window / renderer
                                ;; can only be run on first thread on macos
                                "-XstartOnFirstThread"
                                ;; needed to find libSDL2 on macos
                                "-Djava.library.path=/usr/local/lib"]}
             
             :linux {:jvm-opts [;; needed to find libSDL2 on linux       
                                "-Djava.library.path=/usr/lib/x86_64-linux-gnu"]}
             
             :clojure-1.10.2-alpha1 {:dependencies [[org.clojure/clojure "1.10.2-alpha1"]]}
             :socket {:jvm-opts ["-Dclojure.server.repl={:port 5555 :accept clojure.core.server/repl}"]}})
