#!/bin/sh
# @test
# @bug 6666666
# @summary ssl-test-bc-2nd
# @run shell/timeout=1000 ssl-tests-bc-2nd.sh

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


# EDCHE ciphers are excluded on jdk >= 11 as there is bug in current BC (1.68)
# causing these ciphers to randomly fail, see:
# https://github.com/bcgit/bc-java/issues/862
# It should be fixed in BC 1.69, then this workaround can be removed
JAVA_VERSION_MAJOR=$( "${JAVA_HOME}/bin/java" -version 2>&1 | grep version | head -n 1 | sed -E 's/^.*"(1[.])?([0-9]+).*$$/\2/g' )
ecdhe_ignore_param=''
if [ 11 -le "${JAVA_VERSION_MAJOR}" ] ; then
ecdhe_ignore_param='SSLTESTS_IGNORE_CIPHERS=TLS_ECDHE.*'
fi

cd "ssl-tests"
make clean && make TEST_BC_2ND=1 ${ecdhe_ignore_param}
