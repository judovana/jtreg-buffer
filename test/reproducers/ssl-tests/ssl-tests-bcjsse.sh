#!/bin/sh
# @test
# @bug 6666666
# @summary ssl-test
# @run shell/timeout=1000 ssl-tests-bcjsse.sh

set -eu

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
make clean && make TEST_BCJSSE=1 SSLTESTS_ONLY_SSL_DEFAULTS=1
