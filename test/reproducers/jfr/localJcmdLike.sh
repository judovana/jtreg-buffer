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

JDK8=0
$JAVA -version 2>&1 | grep 1.8.0 || JDK8=$?
if  [ $JDK8 -eq 0 ] ; then
  modArg=""
  TRESH=500
  clazz=ClientCmdLike
  toolsJar="-cp ${TESTJAVA}/lib/tools.jar:."
else
  modArg="--add-opens jdk.attach/sun.tools.attach=ALL-UNNAMED --add-exports jdk.attach/sun.tools.attach=ALL-UNNAMED "
  TRESH=1000
  clazz=ClientCmdLike
fi

#hardcoded in ClientCmdLike.java
FLIGHTFILE=cmdLikeFlight.jfr

${JAVAC} -d . $TESTSRC/Server.java
${JAVAC} $toolsJar $modArg -d . $TESTSRC/$clazz.java
${JAVA} Server  8 &
JPID=$!
${JAVA} $toolsJar $modArg $clazz  $JPID
sleep 4
${JFR} print  $FLIGHTFILE | (head; tail)
parsedLines=`cat $FLIGHTFILE | wc -l`
test $parsedLines -gt $TRESH
rm $FLIGHTFILE 
