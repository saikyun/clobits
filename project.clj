(defproject clobits "0.1.0"
  :dependencies [[org.clojure/clojure "1.10.1"]]
  :plugins [[lein-exec "0.3.7"]]
  
  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]  
  
  :resources ["libs"]
  
  :clean-targets [:target-path "examples"]
  :prep-tasks [["with-profile" "compile-clobits" "compile"]]
  
  :profiles {:compare-files {:dependencies [[digest "1.4.9"]]
                             :source-paths ["test-src"]}
             
             :compile-clobits {:prep-tasks ^:replace []
                               :aot [clobits.wrappers]
                               ;;:compile-path "target/classes/"
                               }
             
             :gen-sdl {:prep-tasks ^:replace []
                       :main clobits.examples.sdl.create-sdl-lib}
             
             :gen-ncurses {:prep-tasks ^:replace []
                           :main clobits.examples.ncurses.create-ncurses-lib}
             
             :compile-java {:java-source-paths ["examples/src/java"]
                            :aot :all}
             
             :compile-sdl {:prep-tasks [["with-profile" "gen-sdl" "run"] "compile"]
                           :aot [clobits.sdl.structs clobits.sdl.ni.generate]
                           :java-source-paths ["examples/src/java"]
                           :source-paths ["examples/src/clj"]}             
             
             :compile-ncurses {:prep-tasks [["with-profile" "gen-ncurses" "run"] "compile"]
                               :aot [clobits.ncurses.ni.generate]
                               :java-source-paths ["examples/src/java"]
                               :source-paths ["examples/src/clj"]}
             
             :uberjar {:jvm-opts ["-Dclojure.compiler.direct-linking=true"
                                  "-Dclojure.spec.skip-macros=true"]}
             
             :sdl-uberjar {:prep-tasks [["with-profile" "compile-sdl" "compile"]
                                        "javac"
                                        "compile"]
                           :source-paths ["examples/src/clj"]
                           :java-source-paths ["examples/src/java"]
                           :uberjar-name "examples_sdl.jar"
                           :main clobits.examples.sdl.startup
                           :aot [clobits.examples.sdl.startup]}
             
             :ncurses-uberjar {:prep-tasks [["with-profile" "compile-ncurses" "compile"]
                                            "javac"
                                            "compile"]
                               :main clobits.examples.ncurses.startup
                               :aot [clobits.examples.ncurses.startup]
                               :source-paths ["examples/src/clj"]
                               :uberjar-name "examples_ncurses.jar"}
             
             :bounce-uberjar {:prep-tasks [["with-profile" "compile-ncurses" "compile"]
                                           "javac"
                                           "compile"]
                              :source-paths ["examples/src/clj"]
                              :uberjar-name "examples_bounce.jar"
                              :main clobits.examples.ncurses.bounce-startup                              
                              :aot [clobits.examples.ncurses.bounce-startup]}
             
             :sdl-poly {:prep-tasks [["with-profile" "compile-sdl" "compile"]]
                        :main clobits.examples.sdl.startup
                        :source-paths ["examples/src/clj"]}
             
             :ncurses-poly {:prep-tasks [["with-profile" "compile-ncurses" "compile"]]
                            :main clobits.examples.ncurses.startup
                            :source-paths ["examples/src/clj"]}
             
             :bounce-poly {:prep-tasks [["with-profile" "compile-ncurses" "compile"]]
                           :main clobits.examples.ncurses.bounce-startup
                           :source-paths ["examples/src/clj"]}
             
             
             :macos {:jvm-opts [;; sdl function regarding the window / renderer
                                ;; can only be run on first thread on macos
                                "-XstartOnFirstThread"
                                ;; needed to find libSDL2 on macos
                                "-Djava.library.path=/usr/local/lib"]}
             
             :linux {:jvm-opts [;; needed to find libSDL2 on linux       
                                "-Djava.library.path=/usr/lib/x86_64-linux-gnu"]}
             
             :clojure-1.10.2-alpha1 {:dependencies [[org.clojure/clojure "1.10.2-alpha1"]]}
             :socket {:jvm-opts ["-Dclojure.server.repl={:port 5555 :accept clojure.core.server/repl}"]}})
