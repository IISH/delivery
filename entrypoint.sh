#!/bin/sh

set -e

echo "Running CUPS /usr/sbin/cupsd"
/usr/sbin/cupsd
sleep 1s
lpstat -W completed -o

CMD="/opt/java/openjdk/bin/java ${JAVA_OPTS} -jar /app/delivery.jar"
echo "Running ${CMD}"
eval "$CMD"
