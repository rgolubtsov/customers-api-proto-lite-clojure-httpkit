# Customers API Lite microservice prototype :small_orange_diamond: <img src="https://clojure.org/images/clojure-logo-icon-256.png" style="border:0;width:32px" alt="Clojure" />

**A daemon written in Clojure, designed and intended to be run as a microservice,
<br />implementing a special Customers API prototype with a smart yet simplified data scheme**

**Rationale:** This project is a *direct* **[Clojure](https://clojure.org "The Clojure Programming Language | or simply Lisp-1 dialect for the JVM")** port of the earlier developed **Customers API Lite microservice prototype**, written in Crystal using **[Kemal](https://kemalcr.com "Lightning Fast, Super Simple Web Framework for Crystal")** web framework, and tailored to be run as a microservice in a Docker container. The following description of the underlying architecture and logics has been taken **[from here](https://github.com/rgolubtsov/customers-api-proto-lite-crystal-kemal/blob/main/README.md)** almost as is, without any principal modifications or adjustment.

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
  * **[Logging](#logging)**

## Building

The microservice might be built and run successfully under **Arch Linux** (proven). &mdash; First install the necessary dependencies (`jdk21-openjdk`, `leiningen`, `make`, `docker`):

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
Compiling customers.api-lite.controller
Compiling customers.api-lite.core
Compiling customers.api-lite.helper
Compiling customers.api-lite.model
$
$ lein uberjar && \
  UBERJAR_DIR="target/uberjar"; \
  DAEMON_NAME="customers-api-lite"; \
  DMN_VERSION="0.2.5"; \
  SIMPLE_JAR="${UBERJAR_DIR}/${DAEMON_NAME}-${DMN_VERSION}.jar"; \
  BUNDLE_JAR="${UBERJAR_DIR}/${DAEMON_NAME}-${DMN_VERSION}-standalone.jar"; \
  rm ${SIMPLE_JAR} && mv ${BUNDLE_JAR} ${SIMPLE_JAR} && \
  DB_DIR="data/db"; \
  if [ -f ${DB_DIR}/${DAEMON_NAME}.db.xz ]; then \
     unxz ${DB_DIR}/${DAEMON_NAME}.db.xz; \
  fi
Compiling customers.api-lite.controller
Compiling customers.api-lite.core
Compiling customers.api-lite.helper
Compiling customers.api-lite.model
Created $HOME/customers-api-proto-lite-clojure-httpkit/target/uberjar/customers-api-lite-0.2.5.jar
Created $HOME/customers-api-proto-lite-clojure-httpkit/target/uberjar/customers-api-lite-0.2.5-standalone.jar
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

**Run** the microservice using its all-in-one JAR bundle, built previously by the `uberjar` Leiningen task or GNU Make's `all` target:

```
$ java -jar target/uberjar/customers-api-lite-0.2.5.jar; echo $?
...
```

To run the microservice as a *true* daemon, i.e. in the background, redirecting all the console output to `/dev/null`, the following form of invocation of its executable JAR bundle can be used:

```
$ java -jar target/uberjar/customers-api-lite-0.2.5.jar > /dev/null 2>&1 &
[1] <pid>
```

**Note:** This will suppress all the console output only; logging to a logfile and to the Unix syslog will remain unchanged.

The daemonized microservice then can be stopped gracefully at any time by issuing the following command:

```
$ kill -SIGTERM <pid>
$
[1]+  Exit 143                java -jar target/uberjar/customers-api-lite-0.2.5.jar > /dev/null 2>&1
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

The following command-line snippets display the exact usage for these endpoints (the **cURL** utility is used as an example to access them)^:

1. **Create customer**

```
$ curl -vXPUT http://localhost:8765/v1/customers \
       -H 'content-type: application/json' \
       -d '{"name":"Jamison Palmer"}'
...
> PUT /v1/customers HTTP/1.1
...
> content-type: application/json
> Content-Length: 25
...
< HTTP/1.1 201 Created
< Content-Type: application/json
< Location: /v1/customers/3
< content-length: 32
< Server: http-kit
...
{"id":3,"name":"Jamison Palmer"}
```

2. **Create contact**

```
$ curl -vXPUT http://localhost:8765/v1/customers/contacts \
       -H 'content-type: application/json' \
       -d '{"customer_id":"3","contact":"+12197654320"}'
...
> PUT /v1/customers/contacts HTTP/1.1
...
> content-type: application/json
> Content-Length: 44
...
< HTTP/1.1 201 Created
< Content-Type: application/json
< Location: /v1/customers/3/contacts/phone
< content-length: 26
< Server: http-kit
...
{"contact":"+12197654320"}
```

Or create **email** contact:

```
$ curl -vXPUT http://localhost:8765/v1/customers/contacts \
       -H 'content-type: application/json' \
       -d '{"customer_id":"3","contact":"jamison.palmer@example.com"}'
...
> PUT /v1/customers/contacts HTTP/1.1
...
> content-type: application/json
> Content-Length: 58
...
< HTTP/1.1 201 Created
< Content-Type: application/json
< Location: /v1/customers/3/contacts/email
< content-length: 40
< Server: http-kit
...
{"contact":"jamison.palmer@example.com"}
```

3. **List customers**

```
$ curl -v http://localhost:8765/v1/customers
...
> GET /v1/customers HTTP/1.1
...
< HTTP/1.1 200 OK
< Content-Type: application/json
< content-length: 136
< Server: http-kit
...
[{"id":1,"name":"Jammy Jellyfish"},{"id":2,"name":"Noble Numbat"},{"id":3,"name":"Jamison Palmer"},{"id":4,"name":"Sarah Kitteringham"}]
```

4. **Retrieve customer**

```
$ curl -v http://localhost:8765/v1/customers/3
...
> GET /v1/customers/3 HTTP/1.1
...
< HTTP/1.1 200 OK
< Content-Type: application/json
< content-length: 32
< Server: http-kit
...
{"id":3,"name":"Jamison Palmer"}
```

5. **List contacts for a given customer**

```
$ curl -v http://localhost:8765/v1/customers/3/contacts
...
> GET /v1/customers/3/contacts HTTP/1.1
...
< HTTP/1.1 200 OK
< Content-Type: application/json
< content-length: 186
< Server: http-kit
...
[{"contact":"+12197654320"},{"contact":"+12197654321"},{"contact":"+12197654322"},{"contact":"jamison.palmer@example.com"},{"contact":"jp@example.com"},{"contact":"jpalmer@example.com"}]
```

6. **List contacts of a given type for a given customer**

```
$ curl -v http://localhost:8765/v1/customers/3/contacts/phone
...
> GET /v1/customers/3/contacts/phone HTTP/1.1
...
< HTTP/1.1 200 OK
< Content-Type: application/json
< content-length: 82
< Server: http-kit
...
[{"contact":"+12197654320"},{"contact":"+12197654321"},{"contact":"+12197654322"}]
```

Or list **email** contacts:

```
$ curl -v http://localhost:8765/v1/customers/3/contacts/email
...
> GET /v1/customers/3/contacts/email HTTP/1.1
...
< HTTP/1.1 200 OK
< Content-Type: application/json
< content-length: 105
< Server: http-kit
...
[{"contact":"jamison.palmer@example.com"},{"contact":"jpalmer@example.com"},{"contact":"jp@example.com"}]
```

> ^ The given names in customer accounts and in email contacts (in samples above) are for demonstrational purposes only. They have nothing common WRT any actual, ever really encountered names elsewhere.

### Logging

The microservice has the ability to log messages to a logfile and to the Unix syslog facility. To enable debug logging, the `:logger.debug.enabled` setting in the microservice main config file `etc/settings.conf` should be set to `true` *before building the microservice*. When running under Arch Linux (not in a Docker container), logs can be seen and analyzed in an ordinary fashion, by `tail`ing the `log/customers-api-lite.log` logfile:

```
$ tail -f log/customers-api-lite.log
[2025-12-29][15:10:00] [DEBUG] [Customers API Lite]
[2025-12-29][15:10:00] [DEBUG] [org.sqlite.jdbc4.JDBC4Connection@398694a6]
[2025-12-29][15:10:00] [INFO ] Server started on port 8765
[2025-12-29][15:10:30] [DEBUG] [PUT]
[2025-12-29][15:10:30] [DEBUG] [Jamison Palmer]
[2025-12-29][15:10:30] [DEBUG] [3|Jamison Palmer]
[2025-12-29][15:10:50] [DEBUG] [PUT]
[2025-12-29][15:10:50] [DEBUG] [Sarah Kitteringham]
[2025-12-29][15:10:50] [DEBUG] [4|Sarah Kitteringham]
[2025-12-29][15:11:10] [DEBUG] [PUT]
[2025-12-29][15:11:10] [DEBUG] customer_id=3
[2025-12-29][15:11:10] [DEBUG] [+12197654320]
[2025-12-29][15:11:10] [DEBUG] [phone|+12197654320]
[2025-12-29][15:11:40] [DEBUG] [PUT]
[2025-12-29][15:11:40] [DEBUG] customer_id=3
[2025-12-29][15:11:40] [DEBUG] [+12197654321]
[2025-12-29][15:11:40] [DEBUG] [phone|+12197654321]
[2025-12-29][15:12:00] [DEBUG] [PUT]
[2025-12-29][15:12:00] [DEBUG] customer_id=3
[2025-12-29][15:12:00] [DEBUG] [+12197654322]
[2025-12-29][15:12:00] [DEBUG] [phone|+12197654322]
[2025-12-29][15:12:20] [DEBUG] [PUT]
[2025-12-29][15:12:20] [DEBUG] customer_id=3
[2025-12-29][15:12:20] [DEBUG] [jamison.palmer@example.com]
[2025-12-29][15:12:20] [DEBUG] [email|jamison.palmer@example.com]
[2025-12-29][15:12:40] [DEBUG] [PUT]
[2025-12-29][15:12:40] [DEBUG] customer_id=3
[2025-12-29][15:12:40] [DEBUG] [jpalmer@example.com]
[2025-12-29][15:12:40] [DEBUG] [email|jpalmer@example.com]
[2025-12-29][15:12:50] [DEBUG] [PUT]
[2025-12-29][15:12:50] [DEBUG] customer_id=3
[2025-12-29][15:12:50] [DEBUG] [jp@example.com]
[2025-12-29][15:12:50] [DEBUG] [email|jp@example.com]
[2025-12-29][15:13:10] [DEBUG] [GET]
[2025-12-29][15:13:10] [DEBUG] [1|Jammy Jellyfish]
[2025-12-29][15:13:20] [DEBUG] [GET]
[2025-12-29][15:13:20] [DEBUG] customer_id=3
[2025-12-29][15:13:20] [DEBUG] [3|Jamison Palmer]
[2025-12-29][15:13:30] [DEBUG] [GET]
[2025-12-29][15:13:30] [DEBUG] customer_id=3
[2025-12-29][15:13:30] [DEBUG] [+12197654320]
[2025-12-29][15:13:40] [DEBUG] [GET]
[2025-12-29][15:13:40] [DEBUG] customer_id=3 | contact_type=phone
[2025-12-29][15:13:40] [DEBUG] [+12197654320]
[2025-12-29][15:13:50] [DEBUG] [GET]
[2025-12-29][15:13:50] [DEBUG] customer_id=3 | contact_type=email
[2025-12-29][15:13:50] [DEBUG] [jamison.palmer@example.com]
[2025-12-29][15:14:00] [INFO ] Server stopped
```

Messages registered by the Unix system logger can be seen and analyzed using the `journalctl` utility:

```
$ journalctl -f
...
Dec 29 15:10:00 <hostname> java[<pid>]: [Customers API Lite]
Dec 29 15:10:00 <hostname> java[<pid>]: [org.sqlite.jdbc4.JDBC4Connection@398694a6]
Dec 29 15:10:00 <hostname> java[<pid>]: Server started on port 8765
Dec 29 15:10:30 <hostname> java[<pid>]: [PUT]
Dec 29 15:10:30 <hostname> java[<pid>]: [Jamison Palmer]
Dec 29 15:10:30 <hostname> java[<pid>]: [3|Jamison Palmer]
Dec 29 15:10:50 <hostname> java[<pid>]: [PUT]
Dec 29 15:10:50 <hostname> java[<pid>]: [Sarah Kitteringham]
Dec 29 15:10:50 <hostname> java[<pid>]: [4|Sarah Kitteringham]
Dec 29 15:11:10 <hostname> java[<pid>]: [PUT]
Dec 29 15:11:10 <hostname> java[<pid>]: customer_id=3
Dec 29 15:11:10 <hostname> java[<pid>]: [+12197654320]
Dec 29 15:11:10 <hostname> java[<pid>]: [phone|+12197654320]
Dec 29 15:11:40 <hostname> java[<pid>]: [PUT]
Dec 29 15:11:40 <hostname> java[<pid>]: customer_id=3
Dec 29 15:11:40 <hostname> java[<pid>]: [+12197654321]
Dec 29 15:11:40 <hostname> java[<pid>]: [phone|+12197654321]
Dec 29 15:12:00 <hostname> java[<pid>]: [PUT]
Dec 29 15:12:00 <hostname> java[<pid>]: customer_id=3
Dec 29 15:12:00 <hostname> java[<pid>]: [+12197654322]
Dec 29 15:12:00 <hostname> java[<pid>]: [phone|+12197654322]
Dec 29 15:12:20 <hostname> java[<pid>]: [PUT]
Dec 29 15:12:20 <hostname> java[<pid>]: customer_id=3
Dec 29 15:12:20 <hostname> java[<pid>]: [jamison.palmer@example.com]
Dec 29 15:12:20 <hostname> java[<pid>]: [email|jamison.palmer@example.com]
Dec 29 15:12:40 <hostname> java[<pid>]: [PUT]
Dec 29 15:12:40 <hostname> java[<pid>]: customer_id=3
Dec 29 15:12:40 <hostname> java[<pid>]: [jpalmer@example.com]
Dec 29 15:12:40 <hostname> java[<pid>]: [email|jpalmer@example.com]
Dec 29 15:12:50 <hostname> java[<pid>]: [PUT]
Dec 29 15:12:50 <hostname> java[<pid>]: customer_id=3
Dec 29 15:12:50 <hostname> java[<pid>]: [jp@example.com]
Dec 29 15:12:50 <hostname> java[<pid>]: [email|jp@example.com]
Dec 29 15:13:10 <hostname> java[<pid>]: [GET]
Dec 29 15:13:10 <hostname> java[<pid>]: [1|Jammy Jellyfish]
Dec 29 15:13:20 <hostname> java[<pid>]: [GET]
Dec 29 15:13:20 <hostname> java[<pid>]: customer_id=3
Dec 29 15:13:20 <hostname> java[<pid>]: [3|Jamison Palmer]
Dec 29 15:13:30 <hostname> java[<pid>]: [GET]
Dec 29 15:13:30 <hostname> java[<pid>]: customer_id=3
Dec 29 15:13:30 <hostname> java[<pid>]: [+12197654320]
Dec 29 15:13:40 <hostname> java[<pid>]: [GET]
Dec 29 15:13:40 <hostname> java[<pid>]: customer_id=3 | contact_type=phone
Dec 29 15:13:40 <hostname> java[<pid>]: [+12197654320]
Dec 29 15:13:50 <hostname> java[<pid>]: [GET]
Dec 29 15:13:50 <hostname> java[<pid>]: customer_id=3 | contact_type=email
Dec 29 15:13:50 <hostname> java[<pid>]: [jamison.palmer@example.com]
Dec 29 15:14:00 <hostname> java[<pid>]: Server stopped
```

**TBD** :cd:

---

**WIP** :dvd:
