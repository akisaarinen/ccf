#!/bin/bash
LAUNCHER_VERSION=0.7.3
export JAVA_OPTS="-client -XX:MaxPermSize=256m -Xmx512m"

sbt_opts=
while getopts ":p:h" option
do
  case $option in
    p) sbt_opts="project ${OPTARG}\n${sbt_opts}";;
    h) echo "Usage: ./sbt [-p PROJECT] args" && exit;;
  esac
done
shift $(($OPTIND - 1))

sbt_cmd="java ${JAVA_OPTS} -jar `dirname $0`/sbt-launch-$LAUNCHER_VERSION.jar"

if [ -z "$sbt_opts" ]; then
  $sbt_cmd $*
else
  echo -e $sbt_opts$* | $sbt_cmd
fi
