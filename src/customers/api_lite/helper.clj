;
; src/customers/api_lite/helper.clj
; =============================================================================
; Customers API Lite microservice prototype (Clojure port). Version 0.2.4
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
(defmacro EXIT-FAILURE []   1) ;    Failing exit status.
(defmacro EXIT-SUCCESS []   0) ; Successful exit status.
(defmacro SPACE        [] " ")
(defmacro SLASH        [] "/")
(defmacro COLON        [] ":")
(defmacro V-BAR        [] "|")
(defmacro EQUALS       [] "=")
(defmacro O-BRACKET    [] "[")
(defmacro C-BRACKET    [] "]")

; Common notification messages.
(defmacro MSG-SERVER-STARTED [] "Server started on port ")
(defmacro MSG-SERVER-STOPPED [] "Server stopped")

; Common error messages.
(defmacro ERR-PORT-VALID-MUST-BE-POSITIVE-INT [] (str
    "Valid server port must be a positive integer value, in the range "
    "1024 .. 49151. The default value of 8080 will be used instead."))
(defmacro ERR-CANNOT-START-SERVER []
    "FATAL: Cannot start server ")
(defmacro ERR-ADDR-ALREADY-IN-USE []
    "due to address requested already in use. Quitting...")
(defmacro ERR-SERV-UNKNOWN-REASON []
    "for an unknown reason. Quitting...")
(defmacro MSG-ADDR-ALREADY-IN-USE [] "Address already in use")
(defmacro ERR-REQ-MALFORMED []
    "HTTP 400 Bad Request: Request is malformed. Please check your inputs.")
(defmacro ERR-REQ-NOT-FOUND-1 [] (str
    "HTTP 404 Not Found: Bad HTTP method used or no such "
    "REST URI path exists. Please check your inputs."))
(defmacro ERR-REQ-NOT-FOUND-2 []
    "HTTP 404 Not Found: No such customer exists.")
(defmacro ERR-REQ-NOT-FOUND-3 [] (str
    "HTTP 404 Not Found: No contacts belonging to a given customer exist, "
    "or no such customer exists."))

(defmacro SETTINGS "The filename of the daemon settings
    (in edn (Extensible Data Notation) format)." [] "settings.conf")

(defmacro MIN-PORT "The minimum port number allowed." [] 1024 )
(defmacro MAX-PORT "The maximum port number allowed." [] 49151)
(defmacro DEF-PORT "The default server port number."  [] 8080 )

; REST URI path-related constants.
(defmacro REST-VERSION   [] "v1"          )
(defmacro REST-PREFIX    [] "customers"   )
(defmacro REST-CUST-ID   [] "customer_id" )
(defmacro REST-CONTACTS  [] "contacts"    )
(defmacro REST-CONT-TYPE [] "contact_type")
(defmacro PHONE          [] "phone"       )
(defmacro EMAIL          [] "email"       )

; HTTP response status codes.
(defmacro HTTP-201 [] 201)
(defmacro HTTP-400 [] 400)
(defmacro HTTP-404 [] 404)

; HTTP response-related constants.
(defmacro CONT-TYPE    [] "content-type"    )
(defmacro MIME-TYPE    [] "application/json")
(defmacro HDR-LOCATION [] "Location"        )

; Regex patterns for contact phones and emails.
(defmacro PHONE-REGEX [] #"^\+\d{9,14}"     )
(defmacro EMAIL-REGEX [] #".{1,63}@.{3,190}")

; Globals.
(def s   "The Unix system logger."    (atom {}))
(def dbg "The debug logging enabler." (atom {}))
(def cnx "The database connection."   (atom {}))

; Helper function. Used to get the daemon settings.
(defn -get-settings [] (edn/read-string (slurp (io/resource (SETTINGS)))))

; Helper function. Retrieves the port number used to run the http-kit
;                  web server, from daemon settings.
(defn -get-server-port [settings]
    (let [server-port (:server.port settings)]

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
(defn -dbg [message]
    (if @dbg (do
        (l/debug  message)
        (.debug@s message)
    ))
)

; Helper function. Makes final cleanups, closes streams, etc.
(defn -cleanup []
    (.close@cnx)

    ; Closing the system logger.
    ; Calling <syslog.h> closelog();
    (.shutdown@s)
)

; vim:set nu et ts=4 sw=4:
