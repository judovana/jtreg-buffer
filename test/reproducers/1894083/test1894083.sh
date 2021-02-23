#!/bin/sh

# @test
# @bug 1894083
# @summary ojdk was not depending on nss package -> failing in fips mode
# @run shell test1894083.sh

if [ "${TESTJAVA}" = "" ] ; then
  PATH_JAVA=$(readlink -f $(which java))
  TESTJAVA=$(dirname $(dirname ${PATH_JAVA}))
  echo "TESTJAVA not set, selecting " ${TESTJAVA}
  echo "If this is incorrect, try setting the variable manually."
fi


KEYTOOL=${TESTJAVA}/bin/keytool

function clean() {
  rm -f store1 store2 ca.pem
}

trap clean EXIT

${KEYTOOL} -genkeypair -alias cert -keystore store1 -storepass mypass -keypass mypass -dname cn=ca,dc=redhat,dc=com -keysize 2048 -keyalg RSA -validity 365
${KEYTOOL} -exportcert -alias cert -keypass mypass -keystore store1 -storepass mypass -rfc -file ca.pem
${KEYTOOL} -v -import -noprompt -trustcacerts -alias cacert -keypass mypass -file ca.pem -keystore store2 -storepass mypass
