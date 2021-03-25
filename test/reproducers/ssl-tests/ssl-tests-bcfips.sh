#!/bin/sh
# @test
# @bug 6666666
# @requires jdk.version.major <= 11
# @summary ssl-test-bcfips
# @run shell/timeout=1000 ssl-tests-bcfips.sh

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
make clean && make TEST_BCFIPS=1
