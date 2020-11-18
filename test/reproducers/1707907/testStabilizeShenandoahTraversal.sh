#
# @ignore
# @test
# @bug 1707907
# @requires jdk.version.major >= 8
# @summary Stabilize Shenandoah Traversal mode
# @run shell testStabilizeShenandoahTraversal.sh
#

set -eu

# set platform-dependent variables
OS=`uname -s`
case "$OS" in
  SunOS | Linux )
    FS="/"
    ;;
  Windows_* | CYGWIN_NT* )
    FS="\\"
    ;;
  * )
    echo "Unrecognized system!"
    exit 1;
    ;;
esac



JAVA=${TESTJAVA}${FS}bin${FS}java
$JAVA -XX:+UseShenandoahGC -XX:ShenandoahGCMode=traversal -version
