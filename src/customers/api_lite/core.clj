;
; src/customers/api_lite/core.clj
; =============================================================================
; Customers API Lite microservice prototype (Clojure port). Version 0.2.6
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
    (:use     [customers.api-lite.helper    ]
              [customers.api-lite.controller])
    (:require [clojure.tools.logging :as l  ]
              [hikari-cp.core        :as cp ]
              [next.jdbc             :as db ]
              [org.httpkit.server    :refer [
                  run-server
                  server-status
                  server-stop!
              ]]))

(defn -main
    "The microservice entry point.

    Args:
        args: A list of command-line arguments."
    {:added "0.0.1"} [& args]

    ; Opening the system logger.
    ; Calling <syslog.h> openlog(NULL, LOG_CONS | LOG_PID, LOG_DAEMON);
    (let [cfg (UnixSyslogConfig.)]
    (.setIdent cfg nil) (.setFacility cfg SyslogIF/FACILITY_DAEMON)
    (reset! s(UnixSyslog.))(.initialize@s SyslogIF/UNIX_SYSLOG cfg))

    ; Getting the daemon settings.
    (let [settings (-get-settings)]

    ; Identifying whether debug logging is enabled.
    (reset! dbg (:logger-debug-enabled settings))

    (let [daemon-name (:daemon-name settings)]
    (-dbg (str (O-BRACKET) daemon-name (C-BRACKET))))

    ; Getting the SQLite database JDBC URL.
    (let [datasource-url (:sqlite-datasource-url settings)]

    ; Making the HikariCP-based datasource.
    (reset! hds (cp/make-datasource {:jdbc-url datasource-url})))

    ; Connecting to the database.
    (reset! cnx (db/get-connection@hds))
    (-dbg (str (O-BRACKET) @cnx (C-BRACKET)))

    ; Getting the port number used to run the http-kit web server.
    (let [server-port (-get-server-port settings)]

    ; Trying to start up the http-kit web server.
    (let [server (try
        (run-server api-lite-routes {
            :port                 server-port
            :legacy-return-value? false
        })
    (catch Exception e
        (if (and (instance? java.net.BindException e)
            (= (ex-message e) (MSG-ADDR-ALREADY-IN-USE)))

            (l/error (str (ERR-CANNOT-START-SERVER) (ERR-ADDR-ALREADY-IN-USE)))
            (l/error (str (ERR-CANNOT-START-SERVER) (ERR-SERV-UNKNOWN-REASON)))
        )

        (-cleanup)
        (System/exit (EXIT-FAILURE))
    ))]

    (if (and (instance? org.httpkit.server.HttpServer server)
        (= (server-status server) :running)) (do

        (l/info  (str (MSG-SERVER-STARTED) server-port))
        (.info@s (str (MSG-SERVER-STARTED) server-port))
    ))

    ; Trapping SIGINT / SIGTERM signals by adding a shutdown hook,
    ; just as it can be written in pure Java:
    ; Runtime.getRuntime().addShutdownHook(new Thread() {
    ;     @Override
    ;     public void run() {...}
    ; });
    (.addShutdownHook (Runtime/getRuntime) (Thread. #(
        (l/info  (MSG-SERVER-STOPPED))
        (.info@s (MSG-SERVER-STOPPED))

        (-cleanup)
        (server-stop! server)
    ))))))
)

; vim:set nu et ts=4 sw=4:
