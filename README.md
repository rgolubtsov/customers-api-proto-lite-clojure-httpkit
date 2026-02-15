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
  * **[Creating a Docker image](#creating-a-docker-image)**
* **[Running](#running)**
  * **[Running a Docker image](#running-a-docker-image)**
  * **[Exploring a Docker image payload](#exploring-a-docker-image-payload)**
* **[Consuming](#consuming)**
  * **[Logging](#logging)**
  * **[Error handling](#error-handling)**

## Building

The microservice might be built and run successfully under **Ubuntu Server (Ubuntu 24.04.4 LTS x86-64)** and **Arch Linux** (both proven). &mdash; First install the necessary dependencies (`openjdk-21-jdk-headless`, `leiningen`, `make`, `docker.io`):

* In Ubuntu Server:

```
$ sudo apt-get update && \
  sudo apt-get install openjdk-21-jdk-headless leiningen make docker.io -y
...
```

* In Arch Linux:

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
  DMN_VERSION="0.3.0"; \
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
Created $HOME/customers-api-proto-lite-clojure-httpkit/target/uberjar/customers-api-lite-0.3.0.jar
Created $HOME/customers-api-proto-lite-clojure-httpkit/target/uberjar/customers-api-lite-0.3.0-standalone.jar
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

### Creating a Docker image

**Build** a Docker image for the microservice:

```
$ # Pull the Azul Zulu JRE image first (based on Alpine Linux), if not already there:
$ sudo docker pull azul/zulu-openjdk-alpine:21-jre-headless-latest
...
$ # Then build the microservice image:
$ sudo docker build -tcustomersapi/api-lite-clj .
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
$ java -jar target/uberjar/customers-api-lite-0.3.0.jar; echo $?
...
```

To run the microservice as a *true* daemon, i.e. in the background, redirecting all the console output to `/dev/null`, the following form of invocation of its executable JAR bundle can be used:

```
$ java -jar target/uberjar/customers-api-lite-0.3.0.jar > /dev/null 2>&1 &
[1] <pid>
```

**Note:** This will suppress all the console output only; logging to a logfile and to the Unix syslog will remain unchanged.

The daemonized microservice then can be stopped gracefully at any time by issuing the following command:

```
$ kill -SIGTERM <pid>
$
[1]+  Exit 143                java -jar target/uberjar/customers-api-lite-0.3.0.jar > /dev/null 2>&1
```

### Running a Docker image

**Run** a Docker image of the microservice, deleting all stopped containers prior to that (if any):

```
$ sudo docker rm `sudo docker ps -aq`; \
  export PORT=8765 && sudo docker run -dp${PORT}:${PORT} --name api-lite-clj customersapi/api-lite-clj; echo $?
...
```

### Exploring a Docker image payload

The following is not necessary but might be considered somewhat interesting &mdash; to look into the running container and check out that the microservice's executable JAR bundle, logfile, and accompanied SQLite database are at their expected places and in effect:

```
$ sudo docker ps -a
CONTAINER ID   IMAGE                       COMMAND                   CREATED              STATUS              PORTS                                         NAMES
<container_id> customersapi/api-lite-clj   "java -jar api-lite..."   About a minute ago   Up About a minute   0.0.0.0:8765->8765/tcp, [::]:8765->8765/tcp   api-lite-clj
$
$ sudo docker exec -it api-lite-clj sh; echo $?
/var/tmp/api-lite $
/var/tmp/api-lite $ uname -a
Linux <container_id> 6.8.0-100-generic #100-Ubuntu SMP PREEMPT_DYNAMIC Tue Jan 13 16:40:06 UTC 2026 x86_64 Linux
/var/tmp/api-lite $
/var/tmp/api-lite $ cat /etc/os-release /etc/alpine-release
NAME="Alpine Linux"
ID=alpine
VERSION_ID=3.20.9
PRETTY_NAME="Alpine Linux v3.20"
HOME_URL="https://alpinelinux.org/"
BUG_REPORT_URL="https://gitlab.alpinelinux.org/alpine/aports/-/issues"
3.20.9
/var/tmp/api-lite $
/var/tmp/api-lite $ java --version
openjdk 21.0.10 2026-01-20 LTS
OpenJDK Runtime Environment Zulu21.48+17-CA (build 21.0.10+7-LTS)
OpenJDK 64-Bit Server VM Zulu21.48+17-CA (build 21.0.10+7-LTS, mixed mode, sharing)
/var/tmp/api-lite $
/var/tmp/api-lite $ ls -al
total 26348
drwxr-xr-x    1 daemon   daemon        4096 Feb 15 18:00 .
drwxrwxrwt    1 root     root          4096 Feb 15 10:10 ..
-rw-rw-r--    1 daemon   daemon    26949863 Feb 15 10:00 api-lite.jar
drwxr-xr-x    1 daemon   daemon        4096 Feb 15 10:10 data
drwxr-xr-x    2 daemon   daemon        4096 Feb 15 18:00 log
/var/tmp/api-lite $
/var/tmp/api-lite $ ls -al data/db/ log/
data/db/:
total 40
drwxr-xr-x    1 daemon   daemon        4096 Feb 15 10:10 .
drwxr-xr-x    1 daemon   daemon        4096 Feb 15 10:10 ..
-rw-rw-r--    1 daemon   daemon       24576 Feb 15 09:40 customers-api-lite.db

log/:
total 16
drwxr-xr-x    2 daemon   daemon        4096 Feb 15 18:00 .
drwxr-xr-x    1 daemon   daemon        4096 Feb 15 18:00 ..
-rw-r--r--    1 daemon   daemon         454 Feb 15 18:00 customers-api-lite.log
/var/tmp/api-lite $
/var/tmp/api-lite $ netstat -plunt
Active Internet connections (only servers)
Proto Recv-Q Send-Q Local Address           Foreign Address         State       PID/Program name
tcp        0      0 :::8765                 :::*                    LISTEN      1/java
/var/tmp/api-lite $
/var/tmp/api-lite $ ps aux
PID   USER     TIME  COMMAND
    1 daemon    0:10 java -jar api-lite.jar
   25 daemon    0:00 sh
   48 daemon    0:00 ps aux
/var/tmp/api-lite $
/var/tmp/api-lite $ exit # Or simply <Ctrl-D>.
0
```

To stop a running container of the microservice gracefully at any time, simply issue the following command:

```
$ sudo docker stop api-lite-clj; echo $?
api-lite-clj
0
```

## Consuming

The microservice exposes **six REST API endpoints** to web clients. They are all intended to deal with customer entities and/or contact entities that belong to customer profiles. The following table displays their syntax:

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
[2026-02-15][15:10:00] [DEBUG] [Customers API Lite]
[2026-02-15][15:10:00] [INFO ] HikariPool-1 - Starting...
[2026-02-15][15:10:00] [INFO ] HikariPool-1 - Added connection org.sqlite.jdbc4.JDBC4Connection@7109b603
[2026-02-15][15:10:00] [INFO ] HikariPool-1 - Start completed.
[2026-02-15][15:10:00] [DEBUG] [HikariProxyConnection@1040733616 wrapping org.sqlite.jdbc4.JDBC4Connection@7109b603]
[2026-02-15][15:10:00] [INFO ] Server started on port 8765
[2026-02-15][15:10:30] [DEBUG] [PUT]
[2026-02-15][15:10:30] [DEBUG] [Saturday Sunday]
[2026-02-15][15:10:30] [DEBUG] [5|Saturday Sunday]
[2026-02-15][15:10:50] [DEBUG] [PUT]
[2026-02-15][15:10:50] [DEBUG] customer_id=5
[2026-02-15][15:10:50] [DEBUG] [Saturday.Sunday@example.com]
[2026-02-15][15:10:50] [DEBUG] [email|Saturday.Sunday@example.com]
[2026-02-15][15:11:10] [DEBUG] [GET]
[2026-02-15][15:11:10] [DEBUG] customer_id=5
[2026-02-15][15:11:10] [DEBUG] [5|Saturday Sunday]
[2026-02-15][15:11:40] [DEBUG] [GET]
[2026-02-15][15:11:40] [DEBUG] customer_id=5 | contact_type=email
[2026-02-15][15:11:40] [DEBUG] [Saturday.Sunday@example.com]
[2026-02-15][15:12:00] [INFO ] Server stopped
[2026-02-15][15:12:00] [INFO ] HikariPool-1 - Shutdown initiated...
[2026-02-15][15:12:00] [INFO ] HikariPool-1 - Shutdown completed.
```

Messages registered by the Unix system logger can be seen and analyzed using the `journalctl` utility:

```
$ journalctl -f
...
Feb 15 15:10:00 <hostname> java[<pid>]: [Customers API Lite]
Feb 15 15:10:00 <hostname> java[<pid>]: [HikariProxyConnection@1040733616 wrapping org.sqlite.jdbc4.JDBC4Connection@7109b603]
Feb 15 15:10:00 <hostname> java[<pid>]: Server started on port 8765
Feb 15 15:10:30 <hostname> java[<pid>]: [PUT]
Feb 15 15:10:30 <hostname> java[<pid>]: [Saturday Sunday]
Feb 15 15:10:30 <hostname> java[<pid>]: [5|Saturday Sunday]
Feb 15 15:10:50 <hostname> java[<pid>]: [PUT]
Feb 15 15:10:50 <hostname> java[<pid>]: customer_id=5
Feb 15 15:10:50 <hostname> java[<pid>]: [Saturday.Sunday@example.com]
Feb 15 15:10:50 <hostname> java[<pid>]: [email|Saturday.Sunday@example.com]
Feb 15 15:11:10 <hostname> java[<pid>]: [GET]
Feb 15 15:11:10 <hostname> java[<pid>]: customer_id=5
Feb 15 15:11:10 <hostname> java[<pid>]: [5|Saturday Sunday]
Feb 15 15:11:40 <hostname> java[<pid>]: [GET]
Feb 15 15:11:40 <hostname> java[<pid>]: customer_id=5 | contact_type=email
Feb 15 15:11:40 <hostname> java[<pid>]: [Saturday.Sunday@example.com]
Feb 15 15:12:00 <hostname> java[<pid>]: Server stopped
```

Inside the running container logs might be queried also by `tail`ing the `log/customers-api-lite.log` logfile:

```
/var/tmp/api-lite $ tail -f log/customers-api-lite.log
[2026-02-15][18:00:15] [DEBUG] [Customers API Lite]
[2026-02-15][18:00:15] [INFO ] HikariPool-1 - Starting...
[2026-02-15][18:00:15] [INFO ] HikariPool-1 - Added connection org.sqlite.jdbc4.JDBC4Connection@50bb1c1f
[2026-02-15][18:00:15] [INFO ] HikariPool-1 - Start completed.
[2026-02-15][18:00:15] [DEBUG] [HikariProxyConnection@1540140763 wrapping org.sqlite.jdbc4.JDBC4Connection@50bb1c1f]
[2026-02-15][18:00:15] [INFO ] Server started on port 8765
[2026-02-15][18:10:20] [DEBUG] [PUT]
[2026-02-15][18:10:20] [DEBUG] [Saturday Sunday]
[2026-02-15][18:10:20] [DEBUG] [5|Saturday Sunday]
[2026-02-15][18:10:25] [DEBUG] [PUT]
[2026-02-15][18:10:25] [DEBUG] customer_id=5
[2026-02-15][18:10:25] [DEBUG] [Saturday.Sunday@example.com]
[2026-02-15][18:10:25] [DEBUG] [email|Saturday.Sunday@example.com]
[2026-02-15][18:10:30] [DEBUG] [GET]
[2026-02-15][18:10:30] [DEBUG] customer_id=5
[2026-02-15][18:10:30] [DEBUG] [5|Saturday Sunday]
[2026-02-15][18:10:35] [DEBUG] [GET]
[2026-02-15][18:10:35] [DEBUG] customer_id=5 | contact_type=email
[2026-02-15][18:10:35] [DEBUG] [Saturday.Sunday@example.com]
```

And of course, Docker itself gives the possibility to read log messages by using the corresponding command for that:

```
$ sudo docker logs -f api-lite-clj
[2026-02-15][18:00:15] [DEBUG] [Customers API Lite]
[2026-02-15][18:00:15] [INFO ] HikariPool-1 - Starting...
[2026-02-15][18:00:15] [INFO ] HikariPool-1 - Added connection org.sqlite.jdbc4.JDBC4Connection@50bb1c1f
[2026-02-15][18:00:15] [INFO ] HikariPool-1 - Start completed.
[2026-02-15][18:00:15] [DEBUG] [HikariProxyConnection@1540140763 wrapping org.sqlite.jdbc4.JDBC4Connection@50bb1c1f]
[2026-02-15][18:00:15] [INFO ] Server started on port 8765
[2026-02-15][18:10:20] [DEBUG] [PUT]
[2026-02-15][18:10:20] [DEBUG] [Saturday Sunday]
[2026-02-15][18:10:20] [DEBUG] [5|Saturday Sunday]
[2026-02-15][18:10:25] [DEBUG] [PUT]
[2026-02-15][18:10:25] [DEBUG] customer_id=5
[2026-02-15][18:10:25] [DEBUG] [Saturday.Sunday@example.com]
[2026-02-15][18:10:25] [DEBUG] [email|Saturday.Sunday@example.com]
[2026-02-15][18:10:30] [DEBUG] [GET]
[2026-02-15][18:10:30] [DEBUG] customer_id=5
[2026-02-15][18:10:30] [DEBUG] [5|Saturday Sunday]
[2026-02-15][18:10:35] [DEBUG] [GET]
[2026-02-15][18:10:35] [DEBUG] customer_id=5 | contact_type=email
[2026-02-15][18:10:35] [DEBUG] [Saturday.Sunday@example.com]
[2026-02-15][18:20:40] [INFO ] Server stopped
[2026-02-15][18:20:40] [INFO ] HikariPool-1 - Shutdown initiated...
[2026-02-15][18:20:40] [INFO ] HikariPool-1 - Shutdown completed.
```

### Error handling

When the URI path or request body passed in an incoming request contains inappropriate input, the microservice will respond with the **HTTP 400 Bad Request** status code, including a specific response body in JSON representation which may describe a possible cause of underlying client error, like the following:

```
$ curl http://localhost:8765/v1/customers/=qwerty4838=-i-.--089asdf..nj524987
{"error":"HTTP 400 Bad Request: Request is malformed. Please check your inputs."}
$
$ curl http://localhost:8765/v1/customers/3....7/contacts
{"error":"HTTP 400 Bad Request: Request is malformed. Please check your inputs."}
$
$ curl http://localhost:8765/v1/customers/--089asdf../contacts/email
{"error":"HTTP 400 Bad Request: Request is malformed. Please check your inputs."}
$
$ curl -XPUT http://localhost:8765/v1/customers/contacts \
       -H 'content-type: application/json' \
       -d '{"customer_id":"3","contact":"12197654320--089asdf../nj524987"}'
{"error":"HTTP 400 Bad Request: Request is malformed. Please check your inputs."}
```

---

**WIP** :dvd:
