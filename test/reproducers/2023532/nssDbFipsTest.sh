#!/bin/bash

set -x

##
## @test nssDbFipsTest.sh
## @bug 2023532
## @summary testing whether nssSecmodDirectory is set correctly
## @run shell nssDbFipsTest.sh
## @requires os.family != "windows"
##

if [[ "$OTOOL_PROJECT_NAME" == *"upstream"* ]]; then
  echo "This reproducer is not supposed to run on upstream."
  exit 0
fi

if [[ "$OTOOL_OS_NAME" == "el" && $OTOOL_OS_VERSION < 8 ]]; then
  echo "This reproducer is not supposed to run on rhels lower than 8"
  exit 0
fi

nss="nss.fips.cfg"
JAVA_PATH=$(dirname $(dirname $(readlink -e $(which java))))
if ! [ -z "$TESTJAVA" ]; then
  JAVA_PATH=$TESTJAVA
fi
set -e
cfgs=$(find $JAVA_PATH | grep "/$nss$")
for file in $cfgs; do
  echo $file
  cat $file | grep "^nssSecmodDirectory\s*=\s*sql:/etc/pki/nssdb\s*$"
done
echo "Test passed."
