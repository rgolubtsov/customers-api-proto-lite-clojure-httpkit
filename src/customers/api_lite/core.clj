;
; src/customers/api_lite/core.clj
; =============================================================================
; Customers API Lite microservice prototype (Clojure port). Version 0.0.2
; =============================================================================
; A daemon written in Clojure, designed and intended to be run
; as a microservice, implementing a special Customers API prototype
; with a smart yet simplified data scheme.
; =============================================================================
; (See the LICENSE file at the top of the source tree.)
;

(ns customers.api-lite.core "The main namespace of the daemon." (:gen-class)
    (:use [customers.api-lite.helper]))

(defn -main
    "The microservice entry point.

    Args:
        args: A vector of command-line arguments."
    {:added "0.0.1"} [& args]

    (let [dbg true]

    (-dbg dbg (str (O_BRACKET) (DAEMON_NAME) (C_BRACKET))))
)

; vim:set nu et ts=4 sw=4:
