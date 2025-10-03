;
; src/customers/api_lite/helper.clj
; =============================================================================
; Customers API Lite microservice prototype (Clojure port). Version 0.0.4
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

; Common error messages.
(defmacro ERR-PORT-VALID-MUST-BE-POSITIVE-INT [] (str
    "Valid server port must be a positive integer value, in the range "
    "1024 .. 49151. The default value of 8080 will be used instead."))

(defmacro SETTINGS "The filename of the daemon settings
    (in edn (Extensible Data Notation) format)." [] "settings.conf")

(defmacro MIN-PORT "The minimum port number allowed." [] 1024 )
(defmacro MAX-PORT "The maximum port number allowed." [] 49151)
(defmacro DEF-PORT "The default server port number."  [] 8080 )

; Helper function. Used to get the daemon settings.
(defn -get-settings [] (edn/read-string (slurp (io/resource (SETTINGS)))))

; Helper function. Retrieves the port number used to run the http-kit
;                  web server, from daemon settings.
(defn -get-server-port [settings]
    (let [server-port (get settings :server.port)]

    (if-not (nil? server-port)
        (cond
            (and (>= server-port (MIN-PORT)) (<= server-port (MAX-PORT)))
                server-port
            :else
                (do (l/error (ERR-PORT-VALID-MUST-BE-POSITIVE-INT)) (DEF-PORT))
        )
        (do (l/error (ERR-PORT-VALID-MUST-BE-POSITIVE-INT)) (DEF-PORT))
    ))
)

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
