#!/bin/bash

# @test allowNonCaAnchor-javaSecurity
# @bug 1433000
# @summary Add security property (i.e. java.security file) version of jdk.security.allowNonCaAnchor
# @requires jdk.version.major == 17
# @run shell ojdk1433.sh

set -exo pipefail

if [ "x${TESTSRC}" = "x" ] ; then
  TESTSRC=.
fi

if [ "x${TESTJAVA}" = "x" ] ; then
  PATH_JAVA=$(readlink -f $(which javac))
  TESTJAVA=$(dirname $(dirname ${PATH_JAVA}))
  echo "TESTJAVA not set, selecting " ${TESTJAVA}
  echo "If this is incorrect, try setting the variable manually."
fi

if [ "${TESTSRC}" = "" ] ; then
  TESTSRC="."
fi

cat > R.java <<'EOF' && $TESTJAVA/bin/javac R.java && $TESTJAVA/bin/java -Djava.security.manager -cp . R; rm R.*

import javax.net.ssl.SSLContext;

public final class R {
    public static void main(String[] args) throws Throwable {
        SSLContext.getDefault();
        System.out.println("TEST PASS - OK");
    }
}
EOF
