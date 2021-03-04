#!/bin/bash

#
# @test
# @bug 1897602
# @summary Warnings when using ManagementFactory.getPlatformMBeanServer with -Xcheck:jni VM argument
# @run shell TestDiagnosticCommandImpl.sh

if [ "x${TESTSRC}" = "x" ] ; then
  TESTSRC=.
fi

if [ "${TESTJAVA}" = "" ] ; then
  PATH_JAVA=$(readlink -f $(which java))
  TESTJAVA=$(dirname $(dirname ${PATH_JAVA}))
  echo "TESTJAVA not set, selecting " ${TESTJAVA}
  echo "If this is incorrect, try setting the variable manually."
fi

if [ "${TESTSRC}" = "" ] ; then
  TESTSRC="."
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

$TESTJAVA/bin/javac -d . $TESTSRC/TestDiagnosticCommandImpl.java
$TESTJAVA/bin/java -Xcheck:jni TestDiagnosticCommandImpl 2>&1 | tee TestDiagnosticCommandImpl.out

if cat TestDiagnosticCommandImpl.out | grep -i -e WARNING ; then
  echo failed
  exit 1
else
  echo passed
  exit 0
fi


