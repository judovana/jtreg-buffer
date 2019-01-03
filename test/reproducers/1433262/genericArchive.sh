##
## @test genericArchive.sh
## @bug 1433262
## @summary generic archiove of inoput >4gb fails

set -ex

FS="/"
JAVAC=${TESTJAVA}${FS}bin${FS}javac
JAVA=${TESTJAVA}${FS}bin${FS}java

$JAVAC -d . $TESTSRC/TestZip.java
RESOURCE=8192m.zip
$JAVA  TestZip  $TESTSRC/$RESOURCE

