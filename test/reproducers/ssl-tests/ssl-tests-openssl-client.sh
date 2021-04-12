#!/bin/sh
# @test
# @requires os.family != "windows"
# @bug 6666666
# @summary ssl-test with openssl client
# @run shell/timeout=1000 ssl-tests-openssl-client.sh

set -eu

fipsParam=""
ignoredProtoParam="SSLTESTS_IGNORE_PROTOCOLS=SSLv3"
if [ -e /proc/sys/crypto/fips_enabled ] && [ 1 = "$( cat /proc/sys/crypto/fips_enabled )" ] ; then
    fipsParam="TEST_PKCS11_FIPS=1"
    # ignore protocols not supported in fips mode
    ignoredProtoParam="${ignoredProtoParam}|TLSv1|TLSv1.1"
    if printf '%s' "${TESTJAVA:-}" | grep -q 'upstream' ; then
        # upstream misses the patch to disable TLSv1.3 in fips mode
        # (not supported by pkcs11 provider)
        ignoredProtoParam="${ignoredProtoParam}|TLSv1.3"
    fi
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

# kerberos seems to need more complicated setup, not yet implemented (ignore for now):
# https://hg.openjdk.java.net/jdk8u/jdk8u/jdk/file/d5c320d784e5/test/sun/security/krb5/auto/SSL.java
krbIgnoreParam="SSLTESTS_IGNORE_CIPHERS=TLS_KRB5.*"

# 2048 bit keys use also for non-fips as RHEL-9 no longer accepts smaller keys
dhkeyParam="SSLTESTS_CUSTOM_JAVA_PARAMS=-Djdk.tls.ephemeralDHKeySize=2048"

cd "ssl-tests"
make clean && make ${krbIgnoreParam} ${fipsParam} ${ignoredProtoParam} ${dhkeyParam} SSLTESTS_USE_OPENSSL_CLIENT=1
