##
## @test 
## @bug 1444666
## @summary check if jfr can be started in runtime via vmattach

set -exo pipefail

if [ ! "x${TESTJAVA}" == "x" ] ; then
  FS="/"
  JAVAC=${TESTJAVA}${FS}bin${FS}javac
  JAVA=${TESTJAVA}${FS}bin${FS}java
  JFR=${TESTJAVA}${FS}bin${FS}jfr
else
  JAVAC=$(readlink -f `which javac`)
  JAVA=$(readlink -f `which java`)
  JFR=$(readlink -f `which jfr`)
fi

if [ "x${TESTSRC}" == "x" ] ; then
  TESTSRC=`pwd`
fi

#hardcoded in ClientCmdLike.java
FLIGHTFILE=cmdLikeFlight.jfr

modArg="--add-opens jdk.attach/sun.tools.attach=ALL-UNNAMED --add-exports jdk.attach/sun.tools.attach=ALL-UNNAMED "
${JAVAC} -d . $TESTSRC/Server.java
${JAVAC} $modArg -d . $TESTSRC/ClientCmdLike.java
${JAVA} Server  8 &
JPID=$!
${JAVA} $modArg ClientCmdLike  $JPID
sleep 4
${JFR} print  $FLIGHTFILE | (head; tail)
parsedLines=`cat $FLIGHTFILE | wc -l`
test $parsedLines -gt 1000
rm $FLIGHTFILE 
