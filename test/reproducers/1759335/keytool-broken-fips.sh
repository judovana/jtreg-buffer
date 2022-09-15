#!/bin/sh
# @test
# @bug 1759335
# @summary OpenJDK keytool is broken in FIPS mode
# @run shell keytool-broken-fips.sh

set -eux

if [ -n "${TESTJAVA:-}" ] ; then
    if [ -f  "${TESTJAVA}"/bin/keytool ] ; then
        KEYTOOL="${TESTJAVA}"/bin/keytool
    elif [ -f  "${TESTJAVA}"/jre/bin/keytool ] ; then
        KEYTOOL="${TESTJAVA}"/bin/keytool
    else
        echo "no keytool on setup-jdk $TESTJAVA, exiting peacefully,nothing to test"
        exit 0
    fi
else
    KEYTOOL="$(readlink -f "$(which keytool)" )"
    if ! [ -f "${KEYTOOL}" ] ; then
        echo "no system keytool, exiting with error"
        exit 1
    fi
fi

# skip on jdk without pkcs12 support in fips
if [ -n "${TESTJAVA:-}" ] \
&& [ -e "/proc/sys/crypto/fips_enabled" ] \
&& cat "/proc/sys/crypto/fips_enabled" | grep -q "^1$" ; then
    if cat "${TESTJAVA:-}/conf/security/java.security" 2>&1 | grep "^fips.keystore.type=PKCS11" \
    || cat "${TESTJAVA:-}/jre/lib/security/java.security" 2>&1 | grep "^fips.keystore.type=PKCS11" ; then
        echo "Seems like JDK without pkcs12 support in fips mode, skipping"
        exit 0
    fi
fi

rm  -f keystore.jks keystore.p12

# JKS keystore
"$KEYTOOL" -genkeypair \
-keystore keystore.jks \
-storetype jks \
-storepass Secret.123 \
-alias "key1" \
-dname "CN=CA" \
-keyalg RSA \
-keypass Secret.123

"$KEYTOOL" -list -v \
-keystore keystore.jks \
-storepass Secret.123

# PKCS12 keystore
"$KEYTOOL" -genkeypair \
-keystore keystore.p12 \
-storetype pkcs12 \
-storepass Secret.123 \
-alias "key1" \
-dname "CN=CA" \
-keyalg RSA \
-keypass Secret.123

"$KEYTOOL" -list -v \
-keystore keystore.p12 \
-storepass Secret.123

rm  -f keystore.jks keystore.p12
