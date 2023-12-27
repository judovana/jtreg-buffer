##
## @test genericArchive.sh
## @bug 1433262
## @requires (jdk.version.major > 8)
## @summary generic archive of input >4gb fails

set -ex

if [ 8 = "${OTOOL_JDK_VERSION:-}" ] ; then
    if [ "el" = "${OTOOL_BUILD_OS_NAME:-}" ] && ! [ "7" = "${OTOOL_BUILD_OS_VERSION:-}" ] \
    || ! printf '%s\n' "${OTOOL_PROJECT_NAME:-}" | grep -E -q 'ojdk8~(rpms|portable)' ; then
        # Only affects JDK8, fix is only in rhel-7 builds (rpms + portables)
        # in form of rpm patch. See:
        # https://bugzilla.redhat.com/show_bug.cgi?id=1433262
        # Failure on other OSes is known, reported (multiple times). Plan is
        # to upstream the fix (backport) rather than bring it to other
        # OSes (check e-mails). Skipping builds, where it is expected to fail.
        # Can be reenabled once patch is upstramed.
        echo "Fix for this is only in rhel-7 builds, skipping..."
        exit 0
    fi
fi

FS="/"
JAVAC=${TESTJAVA}${FS}bin${FS}javac
JAVA=${TESTJAVA}${FS}bin${FS}java

$JAVAC -d . $TESTSRC/TestZip.java
RESOURCE=8192m.zip
$JAVA  TestZip  $TESTSRC/$RESOURCE

