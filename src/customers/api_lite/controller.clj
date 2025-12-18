;
; src/customers/api_lite/controller.clj
; =============================================================================
; Customers API Lite microservice prototype (Clojure port). Version 0.1.9
; =============================================================================
; A daemon written in Clojure, designed and intended to be run
; as a microservice, implementing a special Customers API prototype
; with a smart yet simplified data scheme.
; =============================================================================
; (See the LICENSE file at the top of the source tree.)
;

(ns customers.api-lite.controller "The controller namespace of the daemon."
    (:use     [customers.api-lite.helper]
              [customers.api-lite.model ])
    (:require [clojure.string    :as s  ]
              [clojure.edn       :as edn]
              [clojure.data.json :refer [
                  write-str
              ]]
              [next.jdbc         :refer [
                  execute!
              ]]
              [compojure.core    :refer [
                  defroutes
                  context
                  PUT
                  GET
              ]]
              [compojure.route   :refer [
                  not-found
              ]]))

; Helper function. Used to expose a request method on incoming HTTP requests.
(defn -method [req]
;   (-dbg (str (O-BRACKET) req (C-BRACKET)))

    (let [method- (get req :request-method)]
    (let [method  (s/upper-case (s/replace method- (COLON) (str)))]
    (-dbg (str (O-BRACKET) method (C-BRACKET)))))
)

; Helper function. Used to send an HTTP response.
(defn -response [body headers status]
    (let [resp {:body (write-str body) :headers {(CONT-TYPE) (MIME-TYPE)}}]
    (let [resp-  (if-not (nil? headers)
        (assoc-in resp  [:headers] (into (:headers resp) headers))
        resp
    )]
    (let [resp-- (if-not (nil? status)
        (assoc-in resp- [:status] status)
        resp-
    )]
    resp--)))
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

    ; TODO: Create a new customer (put customer data to the database).

    (-response (str) {
        (HDR-LOCATION) (str (SLASH) (REST-VERSION)
                            (SLASH) (REST-PREFIX)
                            (SLASH) (EQUALS))
    } 201)
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

    ; TODO: Create a new contact (put a contact regarding a given customer
    ;       to the database).

    (-response (str) {
        (HDR-LOCATION) (str (SLASH) (REST-VERSION)
                            (SLASH) (REST-PREFIX)
                            (SLASH) (EQUALS)
                            (SLASH) (REST-CONTACTS)
                            (SLASH) (EQUALS))
    } 201)
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

    ; Retrieving all customer profiles from the database.
    (let [customers (execute! @cnx [(SQL-GET-ALL-CUSTOMERS)])]

    (let [customer0 (nth customers 0)]
    (-dbg (str (O-BRACKET) (get customer0 :customers/id  ) ; getId()
               (V-BAR)     (get customer0 :customers/name) ; getName()
               (C-BRACKET))))

    (-response customers nil nil))
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

    (let [customer-id (-> req :params :customer_id)]
    (-dbg (str (REST-CUST-ID) (EQUALS) customer-id))

    ; Trying to parse and validate the request path variable.
    (let [cust-id (try
        (let [cust-id- (edn/read-string customer-id)]
        (if-not (number? cust-id-) 0 cust-id-))
    (catch NumberFormatException e 0))]

    (if (== cust-id 0)
        (-response {:error (ERR-REQ-MALFORMED)} nil 400)
    (do
        ; Retrieving profile details for a given customer from the database.
        (let [customer- (execute! @cnx [(SQL-GET-CUSTOMER-BY-ID) cust-id])]

        (let [customer (nth customer- 0)]
        (-dbg (str (O-BRACKET) (get customer :customers/id  ) ; getId()
                   (V-BAR)     (get customer :customers/name) ; getName()
                   (C-BRACKET)))

        (-response customer nil nil)))
    ))))
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

    ; TODO: Retrieve all contacts associated with a given customer
    ;       from the database.

    (-response [
        {:contact (COLON)}
        {:contact (SLASH)}
        {:contact (O-BRACKET)}
        {:contact (C-BRACKET)}
    ] nil nil)
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

    ; TODO: Retrieve all contacts of a given type associated
    ;       with a given customer from the database.

    (-response [
        {:contact (COLON)}
        {:contact (SLASH)}
    ] nil nil)
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
    (not-found {
        :body (write-str {:error (ERR-REQ-NOT-FOUND-1)})
        :headers {(CONT-TYPE) (MIME-TYPE)}
    })
)

; vim:set nu et ts=4 sw=4:
