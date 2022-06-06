#!/usr/bin/bash

set -e
set -x

${TESTJAVA}/bin/javac TestTrustStore.java
TEMPFILE=$(mktemp)
${TESTJAVA}/bin/java -Djavax.net.debug=trustmanager TestTrustStore 2> $TEMPFILE

# Debug output sometimes features multiple duplicities with unexpanded variables,
# the regex [^$]* should rule those out.
CACERTS=$(grep javax[^$]*$ $TEMPFILE | grep -o "/"[0-9"/"a-Z_"."-]*"/cacerts"$)
JSSECACERTS=$(grep javax[^$]*$ $TEMPFILE | grep -o "/"[0-9"/"a-Z_"."-]*"/jssecacerts"$)

CACERTSPATH="${TESTJAVA}/lib/security/"
# with ojdk8 rpms/portables, the resultant path is a little different, featuring "jre" substring
if [ $OTOOL_JDK_VERSION == 8 ]; then
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
       
