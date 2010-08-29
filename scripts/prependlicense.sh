#!/bin/bash
LIC_FILE=$1
FILES=`find "$2" -name "$3"`
FILE_COUNT=`find "$2" -name "$3" | wc -l | sed -e "s/ //g"`
TMP=`mktemp /tmp/license.XXXXXX`
echo "Checking licenses from $3 in '$2' ($FILE_COUNT files)"
for f in $FILES
do
    LICENSE_GREP=`grep "Licensed under the Apache License" $f`
    if [ -z "$LICENSE_GREP" ]; then
        echo "License not found from $f, prepending license template"
        cat $LIC_FILE >> $TMP
        cat $f >> $TMP
        mv $TMP $f
    fi
done

