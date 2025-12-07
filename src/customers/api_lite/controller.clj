;
; src/customers/api_lite/controller.clj
; =============================================================================
; Customers API Lite microservice prototype (Clojure port). Version 0.1.5
; =============================================================================
; A daemon written in Clojure, designed and intended to be run
; as a microservice, implementing a special Customers API prototype
; with a smart yet simplified data scheme.
; =============================================================================
; (See the LICENSE file at the top of the source tree.)
;

(ns customers.api-lite.controller "The controller namespace of the daemon."
    (:use     [customers.api-lite.helper])
    (:require [clojure.string :as s     ]
              [compojure.core :refer    [
                  defroutes
                  context
                  GET
              ]]))

(defn root-req-handler
    "The root request handler callback.
    Gets called on each `/` incoming HTTP request.

    Args:
        req: A hash map representing the incoming HTTP request object."
    {:added "0.1.0"} [req]

;   (-dbg (str (O-BRACKET) req (C-BRACKET)))

    (let [method- (get req :request-method)]
    (let [method  (s/upper-case (s/replace method- (COLON) (str)))]
    (-dbg (str (O-BRACKET) method (C-BRACKET)))))

;   (-dbg (str (O-BRACKET) @cnx (C-BRACKET)))

    {:headers {
        (CONT-TYPE) (MIME-TYPE)
    }}
)

; REST API endpoints ----------------------------------------------------------

(defn list-customers
    "The `GET /v1/customers` endpoint.
    Retrieves from the database and lists all customer profiles.

    Args:
        req: A hash map representing the incoming HTTP request object.

    Returns:
        HTTP status code `200 OK` and the response body in JSON representation,
        containing a list of all customer profiles.
        May return client or server error depending on incoming request."
    {:added "0.1.5"} [req]

    (let [method- (get req :request-method)]
    (let [method  (s/upper-case (s/replace method- (COLON) (str)))]
    (-dbg (str (O-BRACKET) method (C-BRACKET)))))

    {:headers {
        (CONT-TYPE) (MIME-TYPE)
    }}
)

(defn get-customer
    "The `GET /v1/customers/{customer_id}` endpoint.
    Retrieves profile details for a given customer from the database.

    Args:
        req: A hash map representing the incoming HTTP request object.

    Returns:
        A specific HTTP status code with profile details for a given customer
        (in the response body in JSON representation).
        May return client or server error depending on incoming request."
    {:added "0.1.5"} [req]

    (let [method- (get req :request-method)]
    (let [method  (s/upper-case (s/replace method- (COLON) (str)))]
    (-dbg (str (O-BRACKET) method (C-BRACKET)))))

    {:headers {
        (CONT-TYPE) (MIME-TYPE)
    }}
)

; -----------------------------------------------------------------------------

(defroutes api-lite-routes
    "The compound request handler callback (Compojure middleware).
    Gets called on each incoming HTTP request."
    {:added "0.1.5"}

    (GET (SLASH) [] root-req-handler) ; <== GET /

    ; /v1/customers
    (context (str (SLASH) (REST-VERSION) (SLASH) (REST-PREFIX)) []
;       (PUT      (SLASH)                           []  add-customer )
;       (PUT (str (SLASH)         (REST-CONTACTS))  []  add-contact  )
        (GET      (SLASH)                           [] list-customers)
        (GET (str (SLASH) (COLON) (REST-CUST-ID))   []  get-customer ))
;       (GET (str (SLASH) (COLON) (REST-CUST-ID)
;                 (SLASH)         (REST-CONTACTS))  [] list-contacts )
;       (GET (str (SLASH) (COLON) (REST-CUST-ID)
;                 (SLASH)         (REST-CONTACTS)
;                 (SLASH) (COLON) (REST-CONT-TYPE)) [] list-contacts-by-type))
)

; vim:set nu et ts=4 sw=4:
