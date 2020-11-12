#!/bin/sh
/usr/sbin/cupsd
exec java -cp /app:/app/lib/* -Dhibernate.types.print.banner=false org.socialhistoryservices.delivery.Application
