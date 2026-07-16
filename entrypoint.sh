#!/bin/sh

set -e

CMD="/opt/java/openjdk/bin/java ${JAVA_OPTS} -jar /app/delivery.jar"
echo "Running ${CMD}"
eval "$CMD"
