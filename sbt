#!/bin/bash
LAUNCHER_VERSION=0.5.5
java -Xmx512M -jar `dirname $0`/sbt-launcher-$LAUNCHER_VERSION.jar "$@"
