;
; src/customers/api_lite/helper.clj
; =============================================================================
; Customers API Lite microservice prototype (Clojure port). Version 0.0.2
; =============================================================================
; A daemon written in Clojure, designed and intended to be run
; as a microservice, implementing a special Customers API prototype
; with a smart yet simplified data scheme.
; =============================================================================
; (See the LICENSE file at the top of the source tree.)
;

(ns customers.api-lite.helper "The helper namespace for the daemon."
    (:require [clojure.tools.logging :as l]))

; Helper constants.
(defmacro O_BRACKET [] "[")
(defmacro C_BRACKET [] "]")

(defmacro DAEMON_NAME "The daemon name." [] "Customers API Lite")

; Helper function. Used to log messages for debugging aims in a free form.
(defn -dbg [dbg s message]
    (if dbg (do
        (l/debug  message)
        (.debug s message)
    ))
)

; Helper function. Makes final cleanups, closes streams, etc.
(defn -cleanup [s]
    ; Closing the system logger.
    ; Calling <syslog.h> closelog();
    (.shutdown s)
)

; vim:set nu et ts=4 sw=4:
