;
; src/customers/api_lite/controller.clj
; =============================================================================
; Customers API Lite microservice prototype (Clojure port). Version 0.1.7
; =============================================================================
; A daemon written in Clojure, designed and intended to be run
; as a microservice, implementing a special Customers API prototype
; with a smart yet simplified data scheme.
; =============================================================================
; (See the LICENSE file at the top of the source tree.)
;

(ns customers.api-lite.controller "The controller namespace of the daemon."
    (:use     [customers.api-lite.helper])
    (:require [clojure.string  :as s    ]
              [compojure.core  :refer   [
                  defroutes
                  context
                  PUT
                  GET
              ]]
              [compojure.route :refer   [
                  not-found
              ]]))

; Helper function. Used to expose a request method on incoming HTTP requests.
(defn -method [req]
;   (-dbg (str (O-BRACKET) req (C-BRACKET)))

    (let [method- (get req :request-method)]
    (let [method  (s/upper-case (s/replace method- (COLON) (str)))]
    (-dbg (str (O-BRACKET) method (C-BRACKET)))))
)

; REST API endpoints ----------------------------------------------------------

(defn add-customer
    "The `PUT /v1/customers` endpoint.

    Creates a new customer (puts customer data to the database).

    The request body is defined exactly in the form
    as `{\"name\":\"{customer_name}\"}`. It should be passed
    with the accompanied request header `content-type` just like the following:

    ```
    -H 'content-type: application/json' -d '{\"name\":\"{customer_name}\"}'
    ```

    `{customer_name}` is a name assigned to a newly created customer.

    Args:
        req: A hash map representing the incoming HTTP request object.

    Returns:
        HTTP status code `201 Created`, the `Location` response header
        (among others), and the response body in JSON representation,
        containing profile details of a newly created customer.
        May return client or server error depending on incoming request."
    {:added "0.1.6"} [req]

    (-method req)

    {:status 201 :headers {
        (HDR-LOCATION) (str (SLASH) (REST-VERSION)
                            (SLASH) (REST-PREFIX)
                            (SLASH) "?")
        (CONT-TYPE) (MIME-TYPE)
    }}
)

(defn add-contact
    "The `PUT /v1/customers/contacts` endpoint.

    Creates a new contact for a given customer (puts a contact
    regarding a given customer to the database).

    The request body is defined exactly in the form
    as `{\"customer_id\":\"{customer_id}\",\"contact\":\"{customer_contact}\"}`
    It should be passed with the accompanied request header `content-type`
    just like the following:

    ```
    -H 'content-type: application/json' -d '{\"customer_id\":\"{customer_id}\",\"contact\":\"{customer_contact}\"}'
    ```

    `{customer_id}` is the customer ID used to associate a newly created
    contact with this customer.

    Args:
        req: A hash map representing the incoming HTTP request object.

    Returns:
        HTTP status code `201 Created`, the `Location` response header
        (among others), and the response body in JSON representation,
        containing details of a newly created customer contact (phone or email)
        May return client or server error depending on incoming request."
    {:added "0.1.6"} [req]

    (-method req)

    {:status 201 :headers {
        (HDR-LOCATION) (str (SLASH) (REST-VERSION)
                            (SLASH) (REST-PREFIX)
                            (SLASH) "?"
                            (SLASH) (REST-CONTACTS)
                            (SLASH) "?")
        (CONT-TYPE) (MIME-TYPE)
    }}
)

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

    (-method req)

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

    (-method req)

    {:headers {
        (CONT-TYPE) (MIME-TYPE)
    }}
)

(defn list-contacts
    "The `GET /v1/customers/{customer_id}/contacts` endpoint.

    Retrieves from the database and lists all contacts
    associated with a given customer.

    Args:
        req: A hash map representing the incoming HTTP request object.

    Returns:
        HTTP status code `200 OK` and the response body in JSON representation,
        containing a list of all contacts associated with a given customer.
        May return client or server error depending on incoming request."
    {:added "0.1.6"} [req]

    (-method req)

    {:headers {
        (CONT-TYPE) (MIME-TYPE)
    }}
)

(defn list-contacts-by-type
    "The `GET /v1/customers/{customer_id}/contacts/{contact_type}` endpoint.

    Retrieves from the database and lists all contacts of a given type
    associated with a given customer.

    Args:
        req: A hash map representing the incoming HTTP request object.

    Returns:
        HTTP status code `200 OK` and the response body in JSON representation,
        containing a list of all contacts of a given type
        associated with a given customer.
        May return client or server error depending on incoming request."
    {:added "0.1.6"} [req]

    (-method req)

    {:headers {
        (CONT-TYPE) (MIME-TYPE)
    }}
)

; -----------------------------------------------------------------------------

(defroutes api-lite-routes
    "The compound request handler callback (Compojure routing facility).
    Gets called on each incoming HTTP request.

    Allowed and properly handled routes are the following ones:

    ```
    PUT /v1/customers
    PUT /v1/customers/contacts
    GET /v1/customers
    GET /v1/customers/:customer_id
    GET /v1/customers/:customer_id/contacts
    GET /v1/customers/:customer_id/contacts/:contact_type
    ```

    Accessing routes other than the above will likely end up in getting
    `404 Not Found` or `405 Method Not Allowed` responses."
    {:added "0.1.5"}

    ; /v1/customers
    (context (str (SLASH) (REST-VERSION) (SLASH) (REST-PREFIX)) []
        (PUT      (SLASH)                           []  add-customer )
        (PUT (str (SLASH)         (REST-CONTACTS))  []  add-contact  )
        (GET      (SLASH)                           [] list-customers)
        (GET (str (SLASH) (COLON) (REST-CUST-ID))   []  get-customer )
        (GET (str (SLASH) (COLON) (REST-CUST-ID)
                  (SLASH)         (REST-CONTACTS))  [] list-contacts )
        (GET (str (SLASH) (COLON) (REST-CUST-ID)
                  (SLASH)         (REST-CONTACTS)
                  (SLASH) (COLON) (REST-CONT-TYPE)) [] list-contacts-by-type))

    ; For any other route Compojure will automatically respond
    ; with the HTTP 404 Not Found status code.
    ; FIXME: Replace the hand-made JSON below with a production-grade,
    ;        lib-based one by incorporating any JSON lib for that.
    (not-found {:headers {
        (CONT-TYPE) (MIME-TYPE)
    } :body
        (str "{\"" (ERR-KEY) "\"" (COLON) "\"" (ERR-REQ-NOT-FOUND-1) "\"}")
    })
)

; vim:set nu et ts=4 sw=4:
