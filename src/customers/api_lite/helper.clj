;
; src/customers/api_lite/helper.clj
; =============================================================================
; Customers API Lite microservice prototype (Clojure port). Version 0.0.1
; =============================================================================
; A daemon written in Clojure, designed and intended to be run
; as a microservice, implementing a special Customers API prototype
; with a smart yet simplified data scheme.
; =============================================================================
; (See the LICENSE file at the top of the source tree.)
;

(ns customers.api-lite.helper "The helper namespace for the daemon.")

; Helper constants.
(defmacro O_BRACKET [] "[")
(defmacro C_BRACKET [] "]")

(defmacro DAEMON_NAME "The daemon name." [] "Customers API Lite")

; vim:set nu et ts=4 sw=4:
