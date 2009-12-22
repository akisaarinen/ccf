#!/bin/bash
MAIL_ADDR=backlog-git@reaktor.fi
git send-email --suppress-from --smtp-server mx.reaktor.fi --to $MAIL_ADDR $1

