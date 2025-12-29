;
; src/customers/api_lite/controller.clj
; =============================================================================
; Customers API Lite microservice prototype (Clojure port). Version 0.2.4
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
                  read-str
              ]]
              [next.jdbc         :refer [
                  execute!
                  execute-one!
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

    (let [method (s/upper-case (name (:request-method req)))]
    (-dbg (str (O-BRACKET) method (C-BRACKET))))
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

; Helper function. Used to parse and validate a customer contact.
;                  Returns the type of contact: phone or email.
(defn -parse-contact [contact]
    (let [phone? (re-find (PHONE-REGEX) contact)]
    (if (nil? phone?) (do
    (let [email? (re-find (EMAIL-REGEX) contact)]
    (if (nil? email?) (str)
        (if (= email? contact) (EMAIL) (str))
    )))
        (if (= phone? contact) (PHONE) (str))
    ))
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

    (let [payload (:body req)]

    (if (nil? payload)
        (-response {:error (ERR-REQ-MALFORMED)} nil (HTTP-400))
    (do
        (let [customer (read-str (slurp payload) :key-fn keyword)]

        (let [customer-name (:name customer)]
        (-dbg (str (O-BRACKET) customer-name (C-BRACKET)))

        ; Creating a new customer (putting customer data to the database).
        (execute-one! @cnx [(SQL-PUT-CUSTOMER) customer-name])))

        (let [customer0 (execute-one! @cnx [(str
            (SQL-GET-ALL-CUSTOMERS)
            (SQL-DESC-LIMIT-1)
        )])]
        (-dbg (str (O-BRACKET) (:customers/id   customer0) ; getId()
                   (V-BAR)     (:customers/name customer0) ; getName()
                   (C-BRACKET)))

        (-response customer0 {
            (HDR-LOCATION) (str (SLASH) (REST-VERSION)
                                (SLASH) (REST-PREFIX)
                                (SLASH) (:customers/id customer0)) ; getId()
        } (HTTP-201)))
    )))
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

    (let [payload (:body req)]

    (if (nil? payload)
        (-response {:error (ERR-REQ-MALFORMED)} nil (HTTP-400))
    (do
        (let [contact (read-str (slurp payload) :key-fn keyword)]

        (let [contact-cust-id (:customer_id contact)]
        (let [contact-contact (:contact     contact)]
        (-dbg (str (REST-CUST-ID) (EQUALS) contact-cust-id))
        (-dbg (str (O-BRACKET) contact-contact (C-BRACKET)))

        ; Trying to parse and validate the request payload {customer_id}.
        (let [cust-id (try
            (let [cust-id- (edn/read-string contact-cust-id)]
            (if-not (number? cust-id-) 0 cust-id-))
        (catch NumberFormatException e 0))]

        (if (zero? cust-id)
            (-response {:error (ERR-REQ-MALFORMED)} nil (HTTP-400))
        (do
            ; Parsing and validating a customer contact: phone or email.
            (let [contact-type (-parse-contact contact-contact)]

            (if (empty? contact-type)
                (-response {:error (ERR-REQ-MALFORMED)} nil (HTTP-400))
            (do
                (let [sql-query
                    (if (= contact-type (PHONE)) (nth (SQL-PUT-CONTACT) 0)
                    (if (= contact-type (EMAIL)) (nth (SQL-PUT-CONTACT) 1)
                                                 (nth (SQL-PUT-CONTACT) 1)
                ))]

                ; Creating a new contact (putting a contact regarding a given
                ; customer to the database).
                (execute-one!@cnx [sql-query contact-contact contact-cust-id]))

                (let [sql-query-
                    (if (= contact-type (PHONE)) (str
                        (nth (SQL-GET-CONTACTS-BY-TYPE) 0)
                        (nth (SQL-ORDER-CONTACTS-BY-ID) 0))
                    (if (= contact-type (EMAIL)) (str
                        (nth (SQL-GET-CONTACTS-BY-TYPE) 1)
                        (nth (SQL-ORDER-CONTACTS-BY-ID) 1))
                        (nth (SQL-GET-CONTACTS-BY-TYPE) 1)
                ))]

                (let [contact0 (execute-one! @cnx [
                    (str sql-query- (SQL-DESC-LIMIT-1)) cust-id])]
                (let [contact0-type
                    (if (= contact-type (PHONE)) :contact_phones/contact
                    (if (= contact-type (EMAIL)) :contact_emails/contact
                                                 :contact_emails/contact
                ))]
                (-dbg (str (O-BRACKET) contact-type
                           (V-BAR)   (contact0-type contact0) ; getContact()
                           (C-BRACKET))))

                (-response contact0 {
                    (HDR-LOCATION) (str (SLASH) (REST-VERSION)
                                        (SLASH) (REST-PREFIX)
                                        (SLASH) contact-cust-id
                                        (SLASH) (REST-CONTACTS)
                                        (SLASH) contact-type)
                } (HTTP-201))))
            )))
        ))))))
    )))
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
    (-dbg (str (O-BRACKET) (:customers/id   customer0) ; getId()
               (V-BAR)     (:customers/name customer0) ; getName()
               (C-BRACKET))))

    (-response customers nil nil))
)

(defn get-customer
    "The `GET /v1/customers/{customer_id}` endpoint.

    Retrieves profile details for a given customer from the database.

    Args:
        req: A hash map representing the incoming HTTP request object.

    Returns:
        HTTP status code `200 OK` with profile details for a given customer
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

    (if (zero? cust-id)
        (-response {:error (ERR-REQ-MALFORMED)} nil (HTTP-400))
    (do
        ; Retrieving profile details for a given customer from the database.
        (let [customer (execute-one! @cnx [(SQL-GET-CUSTOMER-BY-ID) cust-id])]

        (if (nil? customer)
            (-response {:error (ERR-REQ-NOT-FOUND-2)} nil (HTTP-404))
        (do
            (-dbg (str (O-BRACKET) (:customers/id   customer) ; getId()
                       (V-BAR)     (:customers/name customer) ; getName()
                       (C-BRACKET)))

            (-response customer nil nil)
        )))
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

    (let [customer-id (-> req :params :customer_id)]
    (-dbg (str (REST-CUST-ID) (EQUALS) customer-id))

    ; Trying to parse and validate the request path variable.
    (let [cust-id (try
        (let [cust-id- (edn/read-string customer-id)]
        (if-not (number? cust-id-) 0 cust-id-))
    (catch NumberFormatException e 0))]

    (if (zero? cust-id)
        (-response {:error (ERR-REQ-MALFORMED)} nil (HTTP-400))
    (do
        ; Retrieving all contacts associated with a given customer
        ; from the database.
        (let [contacts (execute! @cnx [(SQL-GET-ALL-CONTACTS)
            cust-id ; <== For retrieving phones.
            cust-id ; <== For retrieving emails.
        ])]

        (if (zero? (count contacts))
            (-response {:error (ERR-REQ-NOT-FOUND-3)} nil (HTTP-404))
        (do
            (let [contact0 (nth contacts 0)]
            (-dbg (str (O-BRACKET) (:contact_phones/contact contact0)
                       (C-BRACKET)))) ; getContact()

            (-response contacts nil nil)
        )))
    ))))
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

    (let [customer-id  (-> req :params :customer_id )]
    (let [contact-type (-> req :params :contact_type)]
    (-dbg (str (REST-CUST-ID)   (EQUALS) customer-id (SPACE) (V-BAR) (SPACE)
               (REST-CONT-TYPE) (EQUALS) contact-type))

    ; Trying to parse and validate the request path variable {customer_id}.
    (let [cust-id (try
        (let [cust-id- (edn/read-string customer-id)]
        (if-not (number? cust-id-) 0 cust-id-))
    (catch NumberFormatException e 0))]

    (if (zero? cust-id)
        (-response {:error (ERR-REQ-MALFORMED)} nil (HTTP-400))
    (do
        (let [cont-type (s/lower-case contact-type)]
        (let [sql-query
            (if (= cont-type (PHONE)) (nth (SQL-GET-CONTACTS-BY-TYPE) 0)
            (if (= cont-type (EMAIL)) (nth (SQL-GET-CONTACTS-BY-TYPE) 1)
                                      (nth (SQL-GET-CONTACTS-BY-TYPE) 1)
        ))]

        ; Retrieving all contacts of a given type associated
        ; with a given customer from the database.
        (let [contacts (execute! @cnx [sql-query cust-id])]

        (if (zero? (count contacts))
            (-response {:error (ERR-REQ-NOT-FOUND-3)} nil (HTTP-404))
        (do
            (let [contact0 (nth contacts 0)]
            (let [contact0-type
                (if (= cont-type (PHONE)) :contact_phones/contact
                (if (= cont-type (EMAIL)) :contact_emails/contact
                                          :contact_emails/contact
            ))]
            (-dbg (str (O-BRACKET) (contact0-type contact0) ; getContact()
                       (C-BRACKET)))))

            (-response contacts nil nil)
        )))))
    )))))
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
