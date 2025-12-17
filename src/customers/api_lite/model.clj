;
; src/customers/api_lite/model.clj
; =============================================================================
; Customers API Lite microservice prototype (Clojure port). Version 0.1.9
; =============================================================================
; A daemon written in Clojure, designed and intended to be run
; as a microservice, implementing a special Customers API prototype
; with a smart yet simplified data scheme.
; =============================================================================
; (See the LICENSE file at the top of the source tree.)
;

(ns customers.api-lite.model "The model namespace of the daemon.")

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

; vim:set nu et ts=4 sw=4:
