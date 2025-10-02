;
; project.clj
; =============================================================================
; Customers API Lite microservice prototype (Clojure port). Version 0.0.4
; =============================================================================
; A daemon written in Clojure, designed and intended to be run
; as a microservice, implementing a special Customers API prototype
; with a smart yet simplified data scheme.
; =============================================================================
; (See the LICENSE file at the top of the source tree.)
;

(defproject customers-api-lite "0.0.4"
    :description     "Customers API Lite microservice prototype."
    :url             "https://github.com/rgolubtsov/customers-api-proto-lite-clojure-httpkit"
    :license {
        :name "MIT License"
        :url  "https://raw.githubusercontent.com/rgolubtsov/customers-api-proto-lite-clojure-httpkit/main/LICENSE"
    }
    :dependencies [
        [org.clojure/clojure       "1.12.3"]
        [org.clojure/tools.logging "1.3.0" ]
        [org.slf4j/slf4j-reload4j  "2.0.17"]
        [org.graylog2/syslog4j     "0.9.61"]
        [net.java.dev.jna/jna      "5.18.0"]
        [http-kit                  "2.8.1" ]
    ]
    :main ^:skip-aot customers.api-lite.core
    :target-path     "target/%s"
    :profiles {
        :uberjar {
            :aot :all
            :jvm-opts [
                "-Dclojure.compiler.direct-linking=true"
                "-Dclojure.tools.logging.factory=clojure.tools.logging.impl/slf4j-factory"
            ]
        }
    }
)

; vim:set nu et ts=4 sw=4:
