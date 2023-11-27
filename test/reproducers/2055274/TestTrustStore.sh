#!/usr/bin/bash

set -e
set -x

FS="/"
JAVAC=${TESTJAVA}${FS}bin${FS}javac
JAVA=${TESTJAVA}${FS}bin${FS}java

if [ "x$TESTSRC" == x ] ; then
  TESTSRC=.
fi

OS=$(uname -s)
IS_WINDOWS=FALSE
case "$OS" in
  Windows_* | CYGWIN_NT* )
    IS_WINDOWS=TRUE
    ;;
esac

JDK8=FALSE
if $TESTJAVA/bin/java -version 2>&1 | grep 1.8.0 ; then
  JDK8=TRUE
fi

${JAVAC} -d . ${TESTSRC}/TestTrustStore.java
TEMPFILE=$(mktemp)
${JAVA} -cp . -Djavax.net.debug=trustmanager TestTrustStore 2> $TEMPFILE

# Debug output sometimes features multiple duplicities with unexpanded variables,
# the regex [^$]* should rule those out.
if [ "x$IS_WINDOWS" == "xTRUE" ]; then
  ICH="[A-Z]:\\\\"
else
  ICH="/"
fi
  CACERTS=$(     grep "javax[^$]*$" $TEMPFILE | grep -o "$ICH[-_\./\\\\0-9a-zA-Z]\+[/\\\\]cacerts$" )
  JSSECACERTS=$( grep "javax[^$]*$" $TEMPFILE | grep -o "$ICH[-_\./\\\\0-9a-zA-Z]\+[/\\\\]jssecacerts$" )

CACERTSPATH="${TESTJAVA}/lib/security/"
# with ojdk8 rpms/portables, the resultant path is a little different, featuring "jre" substring
if [ "x$JDK8" == "xTRUE" ]; then
    CACERTSPATH="${TESTJAVA}/jre/lib/security/"
fi

# with ojdk8 on el8 and el9 the default cacerts path is hardcoded to the value mentioned below

if [ $OTOOL_PROJECT_NAME == "ojdk8~rpms" ]; then
    if [ $OTOOL_OS_NAME == "el" ]; then
        if [ $OTOOL_OS_VERSION -ge 8 ]; then
            CACERTSPATH="/etc/pki/java/cacerts"
        fi
    fi
fi

if [ "$CACERTS" == "${CACERTSPATH}cacerts" ]; then
    echo "cacerts default path is CORRECT"
fi

if [ "$JSSECACERTS" == "${CACERTSPATH}jssecacerts" ]; then
    echo "jssecacerts default path is CORRECT"
fi
       
