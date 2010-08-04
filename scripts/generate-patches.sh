#!/bin/bash

EXTRA_FLAGS=""

if [ $1 == '-1' ]
then
    EXTRA_FLAGS+="-1"
    shift
fi

if [ $# -lt 1 ] || [ $# -gt 2 ]
then
  echo "Usage: $0 <compare-to> [<version>]"
  echo "Example: $0 master v3"
  exit
fi

SUBJECT_PREFIX="CCF-PATCH"
rm -r patches
if [ "$2" != "" ]; then
  SUBJECT_PREFIX="CCF-PATCH $2"
fi

git format-patch -n -M $EXTRA_FLAGS $1 --subject-prefix="$SUBJECT_PREFIX" -o patches/
