;
; src/customers/api_lite/core.clj
; =============================================================================
; Customers API Lite microservice prototype (Clojure port). Version 0.0.3
; =============================================================================
; A daemon written in Clojure, designed and intended to be run
; as a microservice, implementing a special Customers API prototype
; with a smart yet simplified data scheme.
; =============================================================================
; (See the LICENSE file at the top of the source tree.)
;

(ns customers.api-lite.core "The main namespace of the daemon." (:gen-class)
    (:use    [customers.api-lite.helper])
    (:import (org.graylog2.syslog4j.impl.unix UnixSyslogConfig)
             (org.graylog2.syslog4j.impl.unix UnixSyslog      )
             (org.graylog2.syslog4j           SyslogIF        )))

(defn -main
    "The microservice entry point.

    Args:
        args: A vector of command-line arguments."
    {:added "0.0.1"} [& args]

    (let [dbg true]

    ; Opening the system logger.
    ; Calling <syslog.h> openlog(NULL, LOG_CONS | LOG_PID, LOG_DAEMON);
    (let [cfg (UnixSyslogConfig.)]
    (.setIdent cfg nil) (.setFacility cfg SyslogIF/FACILITY_DAEMON)
    (let [s (UnixSyslog.)] (.initialize s SyslogIF/UNIX_SYSLOG cfg)

    (-dbg dbg s (str (O_BRACKET) (DAEMON_NAME) (C_BRACKET)))

    (-cleanup s))))
)

; vim:set nu et ts=4 sw=4:
