#!/bin/bash
SMTP_SERVER=smtp.core.reaktor.fi
SMTP_SERVER_PORT=25
MAIL_ADDR=backlog-git@reaktor.fi
git send-email --suppress-from --smtp-server=$SMTP_SERVER --smtp-server-port=$SMTP_SERVER_PORT --to=$MAIL_ADDR $1

