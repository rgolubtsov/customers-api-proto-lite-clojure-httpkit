#
# Dockerfile
# =============================================================================
# Customers API Lite microservice prototype (Clojure port). Version 0.3.0
# =============================================================================
# A daemon written in Clojure, designed and intended to be run
# as a microservice, implementing a special Customers API prototype
# with a smart yet simplified data scheme.
# =============================================================================
# (See the LICENSE file at the top of the source tree.)
#

# Note: Since it is supposed that all-in-one JAR bundle of the microservice
#       was already built previously and can be run in a container as is,
#       there is no need to use official Clojure Docker images
#       (https://hub.docker.com/_/clojure).
#       Instead, it is recommended to use any of JRE-only flavors of slim
#       (e.g. Alpine-based) Docker images.

FROM       azul/zulu-openjdk-alpine:21-jre-headless-latest
USER       daemon
WORKDIR    var/tmp
COPY       target/uberjar/customers-api-lite-0.3.0.jar api-lite/api-lite.jar
COPY       data/db                                     api-lite/data/db/
WORKDIR    api-lite
USER       root
RUN        ["chown", "-R", "daemon:daemon", "."]
USER       daemon
ENTRYPOINT ["java", "-jar", "api-lite.jar"]

# vim:set nu ts=4 sw=4:
