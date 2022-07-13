#!/bin/bash

# @test ability_to_avoid_system_crypto
# @bug 1915071
# @summary can not prevent openjdk from adhering to the crypto policy when RHEL 8 FIPS is enabled
# @run shell ability_to_avoid_system_crypto.sh

if [ "${TESTSRC}" = "" ]
then TESTSRC=.
fi

if [ "${TESTJAVA}" = "" ]
then
  PARENT=`dirname \`which java\``
  TESTJAVA=`dirname ${PARENT}`
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

SHARED_SETUP="-genkeypair -v -alias myproject -keyalg RSA -keysize 4096"
DNAME="cn=myproject, ou=Devices, ou=Random Company, ou=Random Company, o=Random Company, c=US"
KEYS="-keypass 123456$ -storepass 123456$"
k1=my3.keystore
k2=my.keystore 

echo "this should always pass"
rm -rf $k1 $k2
if [ "x$OTOOL_JDK_VERSION" = "x8" ] ; then
  $TESTJAVA/bin/keytool -J-Djava.security.disableSystemPropertiesFile=true -J-Dcom.redhat.fips=false $SHARED_SETUP -storetype PKCS12 -dname "$DNAME"  $KEYS -keystore my3.keystore
else
  $TESTJAVA/bin/keytool -J-Djava.security.disableSystemPropertiesFile=true -J-Dcom.redhat.fips=false $SHARED_SETUP -storetype jks    -dname "$DNAME"  $KEYS -keystore my.keystore
fi

echo "result of below is not known under fips, so removeing -e and if we passed down here, it should be ok"
set +e
rm -rf $k1 $k2
if [ "x$OTOOL_JDK_VERSION" = "x8" ] ; then
  $TESTJAVA/bin/keytool $SHARED_SETUP -storetype PKCS12 -dname "$DNAME"	  $KEYS -keystore my3.keystore
else
  $TESTJAVA/bin/keytool $SHARED_SETUP -storetype jks    -dname "$DNAME"  $KEYS -keystore my.keystore 
fi

rm -rf $k1 $k2
echo "test finished"

