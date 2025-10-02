;
; src/customers/api_lite/helper.clj
; =============================================================================
; Customers API Lite microservice prototype (Clojure port). Version 0.0.3
; =============================================================================
; A daemon written in Clojure, designed and intended to be run
; as a microservice, implementing a special Customers API prototype
; with a smart yet simplified data scheme.
; =============================================================================
; (See the LICENSE file at the top of the source tree.)
;

(ns customers.api-lite.helper "The helper namespace for the daemon."
    (:require [clojure.tools.logging :as l  ]
              [clojure.java.io       :as io ]
              [clojure.edn           :as edn]))

; Helper constants.
(defmacro O-BRACKET [] "[")
(defmacro C-BRACKET [] "]")

; Common notification messages.
(defmacro MSG-SERVER-STARTED [] "Server started on port ")
(defmacro MSG-SERVER-STOPPED [] "Server stopped")

(defmacro SETTINGS "The filename of the daemon settings
    (in edn (Extensible Data Notation) format)." [] "settings.conf")

; Helper function. Used to get the daemon settings.
(defn -get-settings [] (edn/read-string (slurp (io/resource (SETTINGS)))))

; Helper function. Used to log messages for debugging aims in a free form.
(defn -dbg [dbg s message]
    (if dbg (do
        (l/debug  message)
        (.debug s message)
    ))
)

; Helper function. Makes final cleanups, closes streams, etc.
(defn -cleanup [s]
    (l/info  (MSG-SERVER-STOPPED))
    (.info s (MSG-SERVER-STOPPED))

    ; Closing the system logger.
    ; Calling <syslog.h> closelog();
    (.shutdown s)
)

; vim:set nu et ts=4 sw=4:
