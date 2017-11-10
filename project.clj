(defproject clojurebrot "0.1.0-SNAPSHOT"
  :main clojurebrot.core
  :jvm-opts ^:replace ["-server"
                       "-XX:+AggressiveOpts"
                       "-XX:+UseFastAccessorMethods"
                       "-XX:+UseCompressedOops"
                       "-d64"]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [quil "2.6.0"]]
  :profiles {:dev {:plugins [[lein-nodisassemble "0.1.3"]]}})
