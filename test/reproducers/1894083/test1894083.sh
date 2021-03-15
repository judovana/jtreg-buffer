#!/bin/sh

# @test
# @bug 1894083
# @summary ojdk was not depending on nss package -> failing in fips mode
# @run shell test1894083.sh

if [ "x$OTOOL_cryptosetup" == "xfips" -o "x`update-crypto-policies --show`" == "xFIPS"  ] ; then
  echo "fips detected"
  LOCAL_FIPS="true"
else
  echo "probably non-fips environment"
  LOCAL_FIPS="false"
fi

if [ "${TESTJAVA}" = "" ] ; then
  PATH_JAVA=$(readlink -f $(which java))
  TESTJAVA=$(dirname $(dirname ${PATH_JAVA}))
  echo "TESTJAVA not set, selecting " ${TESTJAVA}
  echo "If this is incorrect, try setting the variable manually."
fi


KEYTOOL=${TESTJAVA}/bin/keytool

function clean() {
  rm -f store1 store2 ca.pem logfile*
}

trap clean EXIT

set -x;

failures=0
rm -fv logfile*
${KEYTOOL} -genkeypair -alias cert -keystore store1 -storepass mypass -keypass mypass -dname cn=ca,dc=redhat,dc=com -keysize 2048 -keyalg RSA -validity 365  >> logfile1 2>&1 || let failures=$failures+1
date | tee -a logfile
cat logfile1 | tee -a logfile
${KEYTOOL} -exportcert -alias cert -keypass mypass -keystore store1 -storepass mypass -rfc -file ca.pem >> logfile2 2>&1 || let failures=$failures+1
date | tee -a logfile
cat logfile2 | tee -a logfile
${KEYTOOL} -v -import -noprompt -trustcacerts -alias cacert -keypass mypass -file ca.pem -keystore store2 -storepass mypass >> logfile3 2>&1 || let failures=$failures+1
date | tee -a logfile
cat logfile3 | tee -a logfile


if [ "x$LOCAL_FIPS" == "xtrue" ]; then
  echo "Fips mode, commands should fail, but some kinds of exceptions are prohibited"
  grep -i -e ".*Exception.*not.*initialize.*NSS.*"  logfile 
  if [ $? -eq 0 ] ; then
    echo "Exception of 'Could not initialize NSS' appeared. Failed" 
    exit 1
  else
    echo "Exception of 'Could not initialize NSS' NOT appeared. Passed" 
    exit 0
  fi
else
  echo "Non fips mode, all commands must pass"
  exit $failures
fi
