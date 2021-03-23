#!/bin/sh
# @test
# @bug 6666666
# @requires jdk.version.major <= 11
# @summary ssl-test-bc
# @run shell/timeout=1000 ssl-tests-bc.sh

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

# this is little strange config combining Ojdk's JSSE provider + BC provider,
# probably to be removed if this config turned up too problematic to maintain, see:
# https://github.com/zzambers/ssl-tests/blob/d724200b37774645698982e3fdbde12730878258/Makefile#L167

cd "ssl-tests"
make clean && make TEST_BC=1 SSLTESTS_ONLY_SSL_DEFAULTS=1 SSLTESTS_IGNORE_PROTOCOLS='SSLv3|TLSv1|TLSv1.1|TLSv1.2'
