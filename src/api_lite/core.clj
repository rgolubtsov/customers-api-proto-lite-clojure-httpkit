;
; src/api_lite/core.clj
; =============================================================================
; Customers API Lite microservice prototype (Clojure port). Version 0.0.1
; =============================================================================
; A daemon written in Clojure, designed and intended to be run
; as a microservice, implementing a special Customers API prototype
; with a smart yet simplified data scheme.
; =============================================================================
; (See the LICENSE file at the top of the source tree.)
;

(ns api-lite.core
    "The main namespace of the daemon."

    (:gen-class)
)

(defn -main
    "The microservice entry point." [& args]

    (println "[Customers API Lite]")
)

; vim:set nu et ts=4 sw=4:
