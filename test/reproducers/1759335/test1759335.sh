#!/bin/sh
# @test
# @bug 1759335
# @summary OpenJDK keytool is broken in FIPS mode
# @run shell test1759335.sh

set -ex

pwd
ls -l

if [ "$TESTJAVA" ]; then
    if [ -f  ${TESTJAVA}/bin/keytool ] ; then
	  keytool=${TESTJAVA}/bin/keytool
   elif [ -f  ${TESTJAVA}/jre/bin/keytool ] ; then
	  keytool=${TESTJAVA}/bin/keytool
   else
     echo "no keytool on setup-jdk $TESTJAVA, exiting peacefully,nothing to test" 
     exit 0
   fi
else
	keytool=$(readlink -f $(which keytool))
    if [ ! -f $keytool ] ; then
      echo "no system keytool, exiting with error"
      exit 1
    fi
fi

if  [ "x$HOSTNAME" == "x" ] ; then
  HOSTNAME=`hsotname`
fi

if  [ "x$HOSTNAME" == "x" ] ; then
  HOSTNAME=`no.host.found`
fi

KNAME=keystore
rm  -rvf $KNAME*

echo "case 1"
 $keytool -genkeypair \
   -keystore $KNAME.jks \
   -storepass Secret.123 \
   -alias "sslserver" \
   -dname "CN=$HOSTNAME" \
   -keyalg RSA \
   -keypass Secret.123

echo "case 2"
 $keytool -genkeypair \
   -keystore $KNAME.p12 \
   -storetype pkcs12 \
   -storepass Secret.123 \
   -alias "sslserver" \
   -dname "CN=$HOSTNAME" \
   -keyalg RSA \
   -keypass Secret.123

ls -l  keystore*

if [ ! `ls $KNAME*` -eq 2 ] ; then
  echo "should be two keystores, are not"
  exit 2
fi 

rm  -rvf $KNAME*



