#!/bin/bash

# @test allowNonCaAnchor-javaSecurity
# @bug 1433000
# @summary Add security property (i.e. java.security file) version of jdk.security.allowNonCaAnchor
# @requires ( jdk.version.major < 17 ) | ( jdk.version.major > 17 )
# @run shell ojdk1433-nofips.sh

set -exo pipefail

if [ -e /proc/sys/crypto/fips_enabled ] && [ 1 = "$( cat /proc/sys/crypto/fips_enabled )" ] ; then
    # Ignorred in fips, see: https://issues.redhat.com/browse/OPENJDK-1442
    echo "Skipped in FIPS"
    exit 0
fi

if [ "x${TESTSRC}" = "x" ] ; then
  TESTSRC=.
fi

"${TESTSRC}"/ojdk1433.sh
