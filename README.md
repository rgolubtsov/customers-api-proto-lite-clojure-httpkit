# Customers API Lite microservice prototype :small_orange_diamond: <img src="https://clojure.org/images/clojure-logo-icon-256.png" style="border:0;width:32px" alt="Clojure" />

**A daemon written in Clojure, designed and intended to be run as a microservice,
<br />implementing a special Customers API prototype with a smart yet simplified data scheme**

**Rationale:** This project is a *direct* **[Clojure](https://clojure.org "The Clojure Programming Language")** port of the earlier developed **Customers API Lite microservice prototype**, written in Crystal using **[Kemal](https://kemalcr.com "Lightning Fast, Super Simple Web Framework for Crystal")** web framework, and tailored to be run as a microservice in a Docker container. The following description of the underlying architecture and logics has been taken **[from here](https://github.com/rgolubtsov/customers-api-proto-lite-crystal-kemal/blob/main/README.md)** almost as is, without any principal modifications or adjustment.

This repo is dedicated to develop a microservice that implements a prototype of REST API service for ordinary Customers operations like adding/retrieving a Customer to/from the database, also doing the same ops with Contacts (phone or email) which belong to a Customer account.

The data scheme chosen is very simplified and consisted of only three SQL database tables, but that's quite sufficient because the service operates on only two entities: a **Customer** and a **Contact** (phone or email). And a set of these operations is limited to the following ones:

* Create a new customer (put customer data to the database).
* Create a new contact for a given customer (put a contact regarding a given customer to the database).
* Retrieve from the database and list all customer profiles.
* Retrieve profile details for a given customer from the database.
* Retrieve from the database and list all contacts associated with a given customer.
* Retrieve from the database and list all contacts of a given type associated with a given customer.

As it is clearly seen, there are no *mutating*, usually expected operations like *update* or *delete* an entity and that's made intentionally.

The microservice incorporates the **[SQLite](https://sqlite.org "A small, fast, self-contained, high-reliability, full-featured, SQL database engine")** database as its persistent store. It is located in the `data/db/` directory as an XZ-compressed database file with minimal initial data &mdash; actually having two Customers and by six Contacts for each Customer. The database file is automatically decompressed during build process of the microservice and ready to use as is even when containerized with Docker.

Generally speaking, this project might be explored as a PoC (proof of concept) on how to amalgamate Clojure REST API service backed by SQLite database, running standalone as a conventional daemon in host or VM environment, or in a containerized form as usually widely adopted nowadays.

Surely, one may consider this project to be suitable for a wide variety of applied areas and may use this prototype as: (1) a template for building similar microservices, (2) for evolving it to make something more universal, or (3) to simply explore it and take out some snippets and techniques from it for *educational purposes*, etc.

---

## Table of Contents

* **[Building](#building)**
* **[Running](#running)**
* **[Consuming](#consuming)**

## Building

The microservice might be built and run under **Arch Linux**. &mdash; First install the necessary dependencies (`jdk21-openjdk`, `leiningen`, `make`, `docker`):

```
$ sudo pacman -Syu jdk21-openjdk leiningen make docker
...
```

---

**Build** the microservice using **Leiningen**:

```
$ lein clean
$
$ lein compile :all
Compiling customers.api-lite.core
Compiling customers.api-lite.helper
$
$ lein uberjar && \
  UBERJAR_DIR="target/uberjar"; \
  DAEMON_NAME="customers-api-lite"; \
  DMN_VERSION="0.0.1"; \
  SIMPLE_JAR="${UBERJAR_DIR}/${DAEMON_NAME}-${DMN_VERSION}.jar"; \
  BUNDLE_JAR="${UBERJAR_DIR}/${DAEMON_NAME}-${DMN_VERSION}-standalone.jar"; \
  rm ${SIMPLE_JAR} && mv ${BUNDLE_JAR} ${SIMPLE_JAR} && \
  DB_DIR="data/db"; \
  if [ -f ${DB_DIR}/${DAEMON_NAME}.db.xz ]; then \
     unxz ${DB_DIR}/${DAEMON_NAME}.db.xz; \
  fi
Compiling customers.api-lite.core
Compiling customers.api-lite.helper
Created $HOME/customers-api-proto-lite-clojure-httpkit/target/uberjar/customers-api-lite-0.0.1.jar
Created $HOME/customers-api-proto-lite-clojure-httpkit/target/uberjar/customers-api-lite-0.0.1-standalone.jar
```

Or **build** the microservice using **GNU Make** (optional, but for convenience &mdash; it covers the same **Leiningen** build workflow under the hood):

```
$ make clean
...
$ make      # <== Compilation only phase (JVM classes).
...
$ make all  # <== Building the daemon (executable JAR bundle).
...
```

## Running

**Run** the microservice using **Leiningen** (recompiling sources on-the-fly, if required):

```
$ lein run; echo $?
...
```

**Run** the microservice using its all-in-one JAR bundle, built previously by the `uberjar` Leiningen task:

```
$ java -jar target/uberjar/customers-api-lite-0.0.1.jar; echo $?
...
```

To run the microservice as a *true* daemon, i.e. in the background, redirecting all the console output to `/dev/null`, the following form of invocation of its executable JAR bundle can be used:

```
$ java -jar target/uberjar/customers-api-lite-0.0.1.jar > /dev/null 2>&1 &
[1] <pid>
```

## Consuming

The microservice *should* expose **six REST API endpoints** to web clients... They are all intended to deal with customer entities and/or contact entities that belong to customer profiles. The following table displays their syntax:

No. | Endpoint name                                      | Request method and REST URI                                   | Request body
--: | -------------------------------------------------- | ------------------------------------------------------------- | ----------------------------------------------------------------
1   | Create customer                                    | **PUT** `/v1/customers`                                       | `{"name":"{customer_name}"}`
2   | Create contact                                     | **PUT** `/v1/customers/contacts`                              | `{"customer_id":"{customer_id}","contact":"{customer_contact}"}`
3   | List customers                                     | **GET** `/v1/customers`                                       | &ndash;
4   | Retrieve customer                                  | **GET** `/v1/customers/{customer_id}`                         | &ndash;
5   | List contacts for a given customer                 | **GET** `/v1/customers/{customer_id}/contacts`                | &ndash;
6   | List contacts of a given type for a given customer | **GET** `/v1/customers/{customer_id}/contacts/{contact_type}` | &ndash;

* The `{customer_name}` placeholder is a string &mdash; it usually means the full name given to a newly created customer.
* The `{customer_id}` placeholder is a decimal positive integer number, greater than `0`.
* The `{customer_contact}` placeholder is a string &mdash; it denotes a newly created customer contact (phone or email).
* The `{contact_type}` placeholder is a string and can take one of two possible values, case-insensitive: `phone` or `email`.

**TBD** :cd:

---

**WIP** :dvd:
