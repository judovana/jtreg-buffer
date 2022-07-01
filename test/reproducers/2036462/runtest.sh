#!/usr/bin/bash

set -e
set -x

FS="/"
JAVAC=${TESTJAVA}${FS}bin${FS}javac

if [ "x$TESTSRC" == x ] ; then
  TESTSRC=.
fi

# only testing whether the Test.java is buildable, dont need to run it (see bug)
${JAVAC} -d . ${TESTSRC}/Test.java
