(defproject clobits "0.1.0"
  :dependencies [[org.clojure/clojure "1.10.1"]]
  :plugins [[lein-exec "0.3.7"]]
  :source-paths ["src"]
  :jvm-opts []
  :resources ["src" "libs"]
  
  :profiles {:sdl-uberjar {:main clobits.examples.sdl.startup
                           :global-vars {*assert* false}
                           :uberjar-name "examples_sdl.jar"
                           :jvm-opts ["-Dclojure.compiler.direct-linking=true"
                                      "-Dclojure.spec.skip-macros=true"]
                           :aot :all}
             
             :ncurses-uberjar {:main clobits.examples.ncurses.startup
                               :global-vars {*assert* false}
                               :uberjar-name "examples_ncurses.jar"
                               :jvm-opts ["-Dclojure.compiler.direct-linking=true"
                                          "-Dclojure.spec.skip-macros=true"]
                               :aot :all}
             
             :runner {:main clobits.examples.sdl.startup
                      :source-paths ["src"]
                      :jvm-opts ["-Dclojure.compiler.direct-linking=true"
                                 "-Dclojure.spec.skip-macros=true"]}
             
             :ncurses-poly {:main clobits.examples.ncurses.startup
                            :source-paths ["src"]
                            :jvm-opts ["-Dclojure.compiler.direct-linking=true"
                                       "-Dclojure.spec.skip-macros=true"]}
             
             :macos {:jvm-opts [;; sdl function regarding the window / renderer
                                ;; can only be run on first thread on macos
                                "-XstartOnFirstThread"
                                ;; needed to find libSDL2 on macos
                                "-Djava.library.path=/usr/local/lib"]}
             
             :linux {:jvm-opts [;; needed to find libSDL2 on linux       
                                "-Djava.library.path=/usr/lib/x86_64-linux-gnu"]}
             
             :clojure-1.10.2-alpha1 {:dependencies [[org.clojure/clojure "1.10.2-alpha1"]]}
             :socket {:jvm-opts ["-Dclojure.server.repl={:port 5555 :accept clojure.core.server/repl}"]}})
