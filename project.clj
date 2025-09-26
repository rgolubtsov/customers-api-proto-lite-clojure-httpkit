;
; project.clj
; =============================================================================
; Customers API Lite microservice prototype (Clojure port). Version 0.0.1
; =============================================================================
; A daemon written in Clojure, designed and intended to be run
; as a microservice, implementing a special Customers API prototype
; with a smart yet simplified data scheme.
; =============================================================================
; (See the LICENSE file at the top of the source tree.)
;

(defproject api-lite "0.0.1"
    :description     "Customers API Lite microservice prototype."
    :url             "https://github.com/rgolubtsov/customers-api-proto-lite-clojure-httpkit"
    :license {
        :name "MIT License"
        :url  "https://raw.githubusercontent.com/rgolubtsov/customers-api-proto-lite-clojure-httpkit/main/LICENSE"
    }
    :dependencies [
        [org.clojure/clojure "1.12.3"]
        [http-kit            "2.8.1" ]
    ]
    :main            api-lite.core
    :target-path     "target/%s"
    :profiles        {:uberjar {:aot :all}}
)

; vim:set nu et ts=4 sw=4:
