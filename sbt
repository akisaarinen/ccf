#!/bin/bash
LAUNCHER_VERSION=0.5.5
export JAVA_OPTS="-client -XX:MaxPermSize=256m -Xmx512m"

java $JAVA_OPTS -jar `dirname $0`/sbt-launcher-$LAUNCHER_VERSION.jar "$@"
