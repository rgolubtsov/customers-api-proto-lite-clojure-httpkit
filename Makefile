#
# Makefile
# =============================================================================
# Customers API Lite microservice prototype (Clojure port). Version 0.2.1
# =============================================================================
# A daemon written in Clojure, designed and intended to be run
# as a microservice, implementing a special Customers API prototype
# with a smart yet simplified data scheme.
# =============================================================================
# (See the LICENSE file at the top of the source tree.)
#

SRV     = target
UBERJAR = uberjar
JAR     = $(SRV)/$(UBERJAR)

# Specify flags and other vars here.
LEIN   = lein
LFLAGS = compile :all

MV   = mv
UNXZ = unxz

# Making the first target (JVM classes).
$(SRV):
	$(LEIN) $(LFLAGS)

# Making the second target (executable JAR bundle).
$(JAR):
	$(LEIN) $(UBERJAR) && \
	DAEMON_NAME="customers-api-lite"; \
	DMN_VERSION="0.2.1"; \
	SIMPLE_JAR="$(JAR)/$${DAEMON_NAME}-$${DMN_VERSION}.jar"; \
	BUNDLE_JAR="$(JAR)/$${DAEMON_NAME}-$${DMN_VERSION}-standalone.jar"; \
	$(RM) $${SIMPLE_JAR} && $(MV) $${BUNDLE_JAR} $${SIMPLE_JAR} && \
	DB_DIR="data/db"; \
	if [ -f $${DB_DIR}/$${DAEMON_NAME}.db.xz ]; then \
	   $(UNXZ) $${DB_DIR}/$${DAEMON_NAME}.db.xz; \
	fi

.PHONY: all clean

all: $(JAR)

clean:
	$(LEIN) clean

# vim:set nu et ts=4 sw=4:
