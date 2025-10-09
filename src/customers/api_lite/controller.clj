;
; src/customers/api_lite/controller.clj
; =============================================================================
; Customers API Lite microservice prototype (Clojure port). Version 0.1.0
; =============================================================================
; A daemon written in Clojure, designed and intended to be run
; as a microservice, implementing a special Customers API prototype
; with a smart yet simplified data scheme.
; =============================================================================
; (See the LICENSE file at the top of the source tree.)
;

(ns customers.api-lite.controller "The controller namespace of the daemon."
    (:use [customers.api-lite.helper]))

(defn req-handler
    "The request handler callback. Gets called on each incoming HTTP request.

    Args:
        req: A hash map representing the incoming HTTP request object."
    {:added "0.1.0"} [req]

    (-dbg (str (O-BRACKET) req (C-BRACKET)))
)

; vim:set nu et ts=4 sw=4:
