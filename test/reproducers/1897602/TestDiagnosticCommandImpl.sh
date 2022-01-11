#!/bin/bash

#
# @test
# @bug 1897602
# @summary Warnings when using ManagementFactory.getPlatformMBeanServer with -Xcheck:jni VM argument
# @requires jdk.version.major > 8
# @run shell TestDiagnosticCommandImpl.sh

if [ "x${TESTSRC}" = "x" ] ; then
  TESTSRC=.
fi

if [ "${TESTJAVA}" = "" ] ; then
  PATH_JAVA=$(readlink -f $(which javac))
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

if [ "0$OTOOL_JDK_VERSION" -gt "8" ]; then
  EXTFLAGS='--add-modules jdk.crypto.cryptoki,java.base --add-exports jdk.crypto.cryptoki/sun.security.pkcs11=ALL-UNNAMED --add-opens jdk.crypto.cryptoki/sun.security.pkcs11=ALL-UNNAMED --add-exports java.base/javax.crypto=ALL-UNNAMED --add-opens java.base/javax.crypto=ALL-UNNAMED'
fi

set -exo pipefail

$TESTJAVA/bin/javac $EXTFLAGS -d . $TESTSRC/TestDiagnosticCommandImpl.java
$TESTJAVA/bin/java $EXTFLAGS -Xcheck:jni TestDiagnosticCommandImpl 2>&1 | tee TestDiagnosticCommandImpl.out

if cat TestDiagnosticCommandImpl.out | grep -i -e WARNING ; then
  echo failed
  exit 1
else
  echo passed
  exit 0
fi


