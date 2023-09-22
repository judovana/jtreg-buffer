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
  toolsJar="-cp ${TESTJAVA}/lib/tools.jar:.."
else
  modArg="--add-opens jdk.attach/sun.tools.attach=ALL-UNNAMED --add-exports jdk.attach/sun.tools.attach=ALL-UNNAMED "
  TRESH=1000
  clazz=ClientCmdLike
  toolsJar="-cp .."
fi

#hardcoded in ClientCmdLike.java
FLIGHTFILE=cmdLikeFlight.jfr
rm -rf workdir1 ; mkdir workdir1
rm -rf workdir2 ; mkdir workdir2
expectedDir=workdir1;

${JAVAC} -d . $TESTSRC/Server.java
${JAVAC} $toolsJar $modArg -d . $TESTSRC/$clazz.java
pushd workdir1
  ${JAVA} -cp .. Server  8 &
  JPID=$!
popd
pushd workdir2
  ${JAVA} $toolsJar $modArg $clazz  $JPID
  sleep 4
popd
${JFR} print  $expectedDir/$FLIGHTFILE | (head; tail)
parsedLines=`cat $expectedDir/$FLIGHTFILE | wc -l`
test $parsedLines -gt $TRESH

sleep=`${JFR} summary  $expectedDir/$FLIGHTFILE | grep ThreadSleep`
count=`echo "$sleep"   | sed "s/ \+/ /g"  |  cut -d ' ' -f 3`
#??test $count -gt 100

rm $expectedDir/$FLIGHTFILE
rm -rf workdir1
rm -rf workdir2
