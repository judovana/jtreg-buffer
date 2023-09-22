##
## @test 
## @requires jdk.version.major > 10
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
FLIGHTFILE=remotePidFlight.jfr
rm -rf workdir1 ; mkdir workdir1
rm -rf workdir2 ; mkdir workdir2
expectedDir=workdir2;

${JAVAC} -d . $TESTSRC/Server.java
${JAVAC} -d . $TESTSRC/JmxClientPid.java
pushd workdir1
  ${JAVA} -cp .. Server  8 &
  PID=$!
popd
pushd workdir2
  ${JAVA} -cp .. JmxClientPid  $PID
  sleep 4
popd
${JFR} print  $expectedDir/$FLIGHTFILE | (head; tail)
parsedLines=`cat $expectedDir/$FLIGHTFILE | wc -l`
test $parsedLines -gt 1000

sleep=`${JFR} summary  $expectedDir/$FLIGHTFILE | grep ThreadSleep`
count=`echo "$sleep"   | sed "s/ \+/ /g"  |  cut -d ' ' -f 3`
test $count -gt 100

rm $expectedDir/$FLIGHTFILE
rm -rf workdir1
rm -rf workdir2
