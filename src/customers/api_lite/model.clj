;
; src/customers/api_lite/model.clj
; =============================================================================
; Customers API Lite microservice prototype (Clojure port). Version 0.2.3
; =============================================================================
; A daemon written in Clojure, designed and intended to be run
; as a microservice, implementing a special Customers API prototype
; with a smart yet simplified data scheme.
; =============================================================================
; (See the LICENSE file at the top of the source tree.)
;

(ns customers.api-lite.model "The model namespace of the daemon.")

; The SQL query for creating a new customer
; (putting customer data to the database).
;
; Used by the `PUT /v1/customers` REST endpoint.
(defmacro SQL-PUT-CUSTOMER [] "insert into customers (name) values (?)")

; The SQL queries for creating a new contact for a given customer
; (putting a contact regarding a given customer to the database).
;
; Used by the `PUT /v1/customers/contacts` REST endpoint.
(defmacro SQL-PUT-CONTACT []
   ["insert into contact_phones (contact, customer_id) values (?, ?)"
    "insert into contact_emails (contact, customer_id) values (?, ?)"])

; The SQL query for retrieving all customer profiles.
;
; Used by the `GET /v1/customers` REST endpoint.
(defmacro SQL-GET-ALL-CUSTOMERS [] (str
    "select id ," ; as 'Customer ID'
    "       name" ; as 'Customer Name'
    " from"
    "       customers"
    " order by"
    "       id"))

; The SQL query for retrieving profile details for a given customer.
;
; Used by the `GET /v1/customers/{customer_id}` REST endpoint.
(defmacro SQL-GET-CUSTOMER-BY-ID [] (str
    "select id ," ; as 'Customer ID'
    "       name" ; as 'Customer Name'
    " from"
    "       customers"
    " where"
    "      (id = ?)"))

; The SQL query for retrieving all contacts for a given customer.
;
; Used by the `GET /v1/customers/{customer_id}/contacts` REST endpoint.
(defmacro SQL-GET-ALL-CONTACTS [] (str
    "select phones.contact" ; as 'Phone(s)'
    " from"
    "       contact_phones phones,"
    "       customers      cust"
    " where"
    "      (cust.id = phones.customer_id) and"
    "      (cust.id =                  ?)"
    " union "
    "select emails.contact" ; as 'Email(s)'
    " from"
    "       contact_emails emails,"
    "       customers      cust"
    " where"
    "      (cust.id = emails.customer_id) and"
    "      (cust.id =                  ?)"))

; The SQL queries for retrieving all contacts of a given type
; for a given customer.
;
; Used by the `GET /v1/customers/{customer_id}/contacts/{contact_type}`
; REST endpoint.
(defmacro SQL-GET-CONTACTS-BY-TYPE [] [(str
    "select phones.contact" ; as 'Phone(s)'
    " from"
    "       contact_phones phones,"
    "       customers      cust"
    " where"
    "      (cust.id = phones.customer_id) and"
    "      (cust.id =                  ?)") (str
    "select emails.contact" ; as 'Email(s)'
    " from"
    "       contact_emails emails,"
    "       customers      cust"
    " where"
    "      (cust.id = emails.customer_id) and"
    "      (cust.id =                  ?)")])

; The intermediate part of an SQL query,
; used to order contact records by ID.
(defmacro SQL-ORDER-CONTACTS-BY-ID []
   [" order by phones.id"
    " order by emails.id"])

; The terminating part of an SQL query,
; used to retrieve the last record created.
(defmacro SQL-DESC-LIMIT-1 [] " desc limit 1")

; vim:set nu et ts=4 sw=4:
