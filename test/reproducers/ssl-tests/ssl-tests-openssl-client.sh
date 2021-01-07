#!/bin/sh
# @test
# @bug 6666666
# @summary ssl-test with openssl client
# @run shell/timeout=1000 ssl-tests-openssl-client.sh

set -eu

osSupported() {
    if ! [ -f /etc/redhat-release ] ; then
        return 1
    fi
    rhelPattern='^Red Hat Enterprise Linux release ([0-9]+)\..*$'
    if cat /etc/redhat-release | grep -E -q "${rhelPattern}" ; then
        version="$(  cat /etc/redhat-release | grep -E "${rhelPattern}" \
        | head -n 1  | sed -E "s/${rhelPattern}/\\1/g" )"
        if [ "${version}" -ge 8 ] ; then
            return 0
        fi
    fi
    if cat /etc/redhat-release | grep -q "Fedora" ; then
        return 0
    fi
    return 1
}

if ! osSupported ; then
    echo "ssl-tests with openssl client are not supported on this os yet!" 1>&2
    exit 0
fi

fipsParam=""
if [ -e /proc/sys/crypto/fips_enabled ] && [ 1 = "$( cat /proc/sys/crypto/fips_enabled )" ] ; then
    fipsParam="TEST_PKCS11_FIPS=1 SSLTESTS_CUSTOM_JAVA_PARAMS=-Djdk.tls.ephemeralDHKeySize=2048"
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

cd "ssl-tests"
make clean && make ${krbIgnoreParam} ${fipsParam} SSLTESTS_USE_OPENSSL_CLIENT=1 SSLTESTS_IGNORE_PROTOCOLS=SSLv3
