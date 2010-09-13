#!/bin/bash
SMTP_SERVER=smtp.reaktor.fi
SMTP_SERVER_PORT=25
MAIL_ADDR=scala-ccf@googlegroups.com

if [ $1 == '--proxy' ] 
then
    SMTP_SERVER=localhost
    SMTP_SERVER_PORT=7777
    shift
fi

git send-email --suppress-from --smtp-server=$SMTP_SERVER --smtp-server-port=$SMTP_SERVER_PORT --to=$MAIL_ADDR $1

