(defproject rest-in-lease-api-aws "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [ [org.clojure/clojure "1.10.1"]
;                 [ring/ring-json "0.5.0"]
                  [org.clojure/data.json "0.2.6"]     ; Clojure data.JSON library
                  [com.amazonaws/aws-lambda-java-core "1.0.0"]
  ]
  :main ^:skip-aot rest-in-lease-api-aws.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
