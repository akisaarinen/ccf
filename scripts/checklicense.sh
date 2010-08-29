#!/bin/bash
./scripts/prependlicense.sh scripts/license.scala.template ccf "*.scala"
./scripts/prependlicense.sh scripts/license.scala.template app "*.scala"
./scripts/prependlicense.sh scripts/license.scala.template perftest "*.scala"
