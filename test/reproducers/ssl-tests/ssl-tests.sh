#!/bin/sh
# @test
# @bug 6666666
# @summary ssl-test
# @run shell/timeout=500 ssl-tests.sh

set -eu

# skip if running in fips mode
if [ -e /proc/sys/crypto/fips_enabled ] && [ 1 = "$( cat /proc/sys/crypto/fips_enabled )" ] ; then
    printf '%s\n' "FIPS mode not supported yet, skipping!" 1>&2
    exit 0
fi

if [ -n "${TESTJAVA:-}" ]; then
    export JAVA_HOME="${TESTJAVA}"
fi

if ! [ -d "ssl-tests" ] ; then
    if [ -n "${TESTSRC:-}" ] && [ -d "${TESTSRC}/ssl-tests" ] ; then
        cp -a "${TESTSRC}/ssl-tests" .
    else
        git clone "https://github.com/zzambers/ssl-tests.git"
    fi
fi

cd "ssl-tests"
make clean && make
