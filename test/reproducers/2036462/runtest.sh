#!/usr/bin/bash

set -e
set -x

FS="/"
JAVAC=${TESTJAVA}${FS}bin${FS}javac

if [ "x$TESTSRC" == x ] ; then
  TESTSRC=.
fi

# this is needed for ojdk 16 and above
FLAGS="--add-modules jdk.crypto.cryptoki --add-exports jdk.crypto.cryptoki/sun.security.pkcs11.wrapper=ALL-UNNAMED"

# removing the above for ojdk lower than 16
if [ 9 -gt $OTOOL_JDK_VERSION ]; then
    FLAGS=""
fi

# only testing whether the Test.java is buildable, dont need to run it (see bug)
${JAVAC} -d . $FLAGS ${TESTSRC}/Test.java
