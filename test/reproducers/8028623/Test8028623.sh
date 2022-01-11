#!/bin/sh
#
# Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
#
# This code is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License version 2 only, as
# published by the Free Software Foundation.
#
# This code is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
# FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
# version 2 for more details (a copy is included in the LICENSE file that
# accompanied this code).
#
# You should have received a copy of the GNU General Public License version
# 2 along with this work; if not, write to the Free Software Foundation,
# Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
#
# Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
# or visit www.oracle.com if you need additional information or have any
# questions.
#
#

# @test
# @bug 8028623
# @summary Test hashing of extended characters in Serviceability Agent.
# @run shell Test8028623.sh

set -x

OS=$(uname -s)
IS_WINDOWS=FALSE
case "$OS" in
CYGWIN*)
  IS_WINDOWS=TRUE
  ;;
esac

if [ "${TESTJAVA}" = "" ]; then
  PATH_JAVA=$(readlink -f "$(which javac)")

  if [ $IS_WINDOWS = 'TRUE' ]; then
    TESTJAVA=$(dirname "$(dirname "$(cygpath -m "$PATH_JAVA")")")
  else
    TESTJAVA=$(dirname $(dirname ${PATH_JAVA}))
  fi

  echo "TESTJAVA not set, selecting " ${TESTJAVA}
  echo "If this is incorrect, try setting the variable manually."
fi

FS="/"
JAVAC=${TESTJAVA}${FS}bin${FS}javac
JAVA=${TESTJAVA}${FS}bin${FS}java
JMAP=${TESTJAVA}${FS}bin${FS}jmap

if [ $IS_WINDOWS = 'TRUE' ]; then
  JAVAC=$(cygpath -m "$JAVAC")
  JAVA=$(cygpath -m "$JAVA")
fi

if [ "${TESTSRC}" = "" ]; then
  TESTSRC=.
  if [ $IS_WINDOWS = 'TRUE' ]; then
    TESTSRC=$(cygpath -m $(readlink -f "."))
  fi
fi

"$JAVAC" -encoding UTF-8 ${TESTJAVACOPTS} -d . ${TESTSRC}/Test8028623.java

"$JAVA" ${TESTVMOPTS} Test8028623 &
PID=$!
sleep 4

if [ $IS_WINDOWS = 'TRUE' ]; then
  "$JMAP" -dump:live,format=b,file=heap.out $(</proc/$PID/winpid)
else
  "$JMAP" -dump:live,format=b,file=heap.out $PID
fi

RESULT=$?
kill $PID

if [ $RESULT != 0 ]; then
  echo "Test FAILED: jmap exits with error."
  exit 1
fi
if [ ! -f heap.out ]; then
  echo "Test FAILED: heap.out not created."
  exit 1
fi
