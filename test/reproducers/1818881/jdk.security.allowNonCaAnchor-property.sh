#!/bin/bash

# @test jdk.security.allowNonCaAnchor-property
# @bug 1818881
# @summary Add security property (i.e. java.security file) version of jdk.security.allowNonCaAnchor
# @run shell jdk.security.allowNonCaAnchor-property.sh

if [ "${TESTSRC}" = "" ] ; then
  TESTSRC=.
fi

if [ "${TESTJAVA}" = "" ] ; then
  PATH_JAVA=$(readlink -f $(which java))
  TESTJAVA=$(dirname $(dirname ${PATH_JAVA}))
  echo "TESTJAVA not set, selecting " ${TESTJAVA}
  echo "If this is incorrect, try setting the variable manually."
fi

# set platform-dependent variables
OS=`uname -s`
case "$OS" in
  Linux )
    PS=":"
    FS="/"
    ;;
  Windows_* | CYGWIN_NT* )
    PS=";"
    FS="\\"
    ;;
  * )
    echo "Unrecognized system!"
    exit 1;
    ;;
esac

set -exo pipefail

garbage="ca.cer
ca.jks
clientLog1
clientLog2
HTTPSClient\$1.class
HTTPSClient.class
HTTPSServer\$ServerThread.class
HTTPSServer.class
jboss.cer
jboss.csr
jboss.keystore.jks
serverLog"
function clean() {
  if [ "x$CLEAN" == "xtrue" ] ; then
    rm -fv $garbage
  fi
}
trap clean EXIT # will be overwritten later if server starts

$TESTJAVA/bin/javac HTTPSClient.java
$TESTJAVA/bin/javac HTTPSServer.java

if [ "x$CLEAN" == "x" ] ; then
  CLEAN="true"
fi
if [ "x$GEN_KEYS" == "x" ] ; then
  GEN_KEYS="true"
fi

if [ "x$GEN_KEYS" == "xtrue" ] ; then
  $TESTJAVA/bin/keytool -genkeypair -alias ca    -keystore ca.jks             -storepass secret -keypass secret -dname cn=ca,dc=redhat,dc=com -keysize 2048 -keyalg RSA -validity 365 -ext "BC=ca:false"
  $TESTJAVA/bin/keytool -exportcert -alias ca    -keystore ca.jks             -storepass secret -file ca.cer
  $TESTJAVA/bin/keytool -genkeypair -alias jboss -keystore jboss.keystore.jks -storepass secret -keypass secret -dname "cn=jboss.usersys.redhat.com, ou=GSS,dc=redhat,dc=com" -keysize 2048 -keyalg RSA -ext "BC=ca:false"
  $TESTJAVA/bin/keytool -certreq    -alias jboss -keystore jboss.keystore.jks -storepass secret -file jboss.csr
  $TESTJAVA/bin/keytool -gencert    -alias ca    -keystore ca.jks             -storepass secret -keypass secret -infile jboss.csr -outfile jboss.cer -validity 365 -ext "BC=ca:false"
  $TESTJAVA/bin/keytool -importcert -alias ca    -keystore jboss.keystore.jks -storepass secret -trustcacerts -file ca.cer -noprompt
  $TESTJAVA/bin/keytool -importcert -alias jboss -keystore jboss.keystore.jks -storepass secret -file jboss.cer -noprompt
fi

$TESTJAVA/bin/keytool  -list  -keystore jboss.keystore.jks  -storepass secret  -v | grep -e "CA:"
$TESTJAVA/bin/keytool  -list  -keystore jboss.keystore.jks  -storepass secret  -v | grep -e "CA:true"

OPTS="-Djavax.net.ssl.keyStore=jboss.keystore.jks 
      -Djavax.net.ssl.keyStorePassword=secret 
      -Djavax.net.ssl.trustStore=jboss.keystore.jks
      -Djavax.net.ssl.trustStorePassword=secret
      -Djavax.net.ssl.trustStoreType=jks
      -Dtest.port=9999"
ANCHOR="-Djdk.security.allowNonCaAnchor"

serverLog=serverLog
rm -f pid
(set -eo pipefail ; $TESTJAVA/bin/java $OPTS  HTTPSServer 2>&1 & echo $! > pid ) | tee $serverLog &
SERVER_PID=$(cat pid)
rm -f pid

function killServer() {
  $TESTJAVA/bin/jps
  kill $SERVER_PID
  clean
}
trap killServer EXIT

i=0
while ! cat $serverLog | grep "SSL server started"; do
  if [ $i -gt 5 ] ; then
    echo "server not started"
    exit 1
  fi
  let i=i+1
  sleep 1
done

$TESTJAVA/bin/java $OPTS "$ANCHOR=false" HTTPSClient 2>&1 | tee clientLog1
$TESTJAVA/bin/java $OPTS "$ANCHOR=true"  HTTPSClient 2>&1 | tee clientLog2

