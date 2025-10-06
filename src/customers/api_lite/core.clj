;
; src/customers/api_lite/core.clj
; =============================================================================
; Customers API Lite microservice prototype (Clojure port). Version 0.0.5
; =============================================================================
; A daemon written in Clojure, designed and intended to be run
; as a microservice, implementing a special Customers API prototype
; with a smart yet simplified data scheme.
; =============================================================================
; (See the LICENSE file at the top of the source tree.)
;

(ns customers.api-lite.core "The main namespace of the daemon." (:gen-class)
    (:import  (org.graylog2.syslog4j.impl.unix UnixSyslogConfig)
              (org.graylog2.syslog4j.impl.unix UnixSyslog      )
              (org.graylog2.syslog4j           SyslogIF        ))
    (:use     [customers.api-lite.helper  ])
    (:require [clojure.tools.logging :as l]
              [org.httpkit.server :refer  [
                  run-server
                  server-status
              ]]))

(defn- -req-handler [req]
    (-dbg (str (O-BRACKET) req (C-BRACKET)))
)

(defn -main
    "The microservice entry point.

    Args:
        args: A vector of command-line arguments."
    {:added "0.0.1"} [& args]

    ; Opening the system logger.
    ; Calling <syslog.h> openlog(NULL, LOG_CONS | LOG_PID, LOG_DAEMON);
    (let [cfg (UnixSyslogConfig.)]
    (.setIdent cfg nil) (.setFacility cfg SyslogIF/FACILITY_DAEMON)
    (reset! s(UnixSyslog.))(.initialize@s SyslogIF/UNIX_SYSLOG cfg))

    ; Getting the daemon settings.
    (let [settings (-get-settings)]

    ; Identifying whether debug logging is enabled.
    (reset! dbg (get settings :logger.debug.enabled))

    (let [daemon-name (get settings :daemon.name)]

    (-dbg (str (O-BRACKET) daemon-name (C-BRACKET))))

    ; Getting the SQLite database path.
    (let [database-path (get settings :sqlite.database.path)])

    ; Getting the port number used to run the http-kit web server.
    (let [server-port (-get-server-port settings)]

    ; Starting up the http-kit web server.
    (let [server (run-server -req-handler {
        :port                 server-port
        :legacy-return-value? false
    })]

    (if (instance? org.httpkit.server.HttpServer server) (do
        (l/info  (str (MSG-SERVER-STARTED) server-port))
        (.info@s (str (MSG-SERVER-STARTED) server-port))

        (-dbg (str (O-BRACKET) (server-status server) (C-BRACKET)))
    )))))

    ; FIXME: Call (-cleanup) on SIGTERM / INT, not here.
;   (-cleanup)
)

; vim:set nu et ts=4 sw=4:
