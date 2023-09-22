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
FLIGHTFILE=remotePortFlight.jfr
PORT=64686
rm -rf workdir1 ; mkdir workdir1
rm -rf workdir2 ; mkdir workdir2
expectedDir=workdir2;

${JAVAC} -d . $TESTSRC/Server.java
${JAVAC} -d . $TESTSRC/JmxClientPort.java
pushd workdir1
  ${JAVA} -cp .. Server  8 &
  sleep 1
popd
pushd workdir2
  JMX_ARGS="-Dcom.sun.management.jmxremote  -Dcom.sun.management.jmxremote.authenticate=false  -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.port=$PORT -Djava.rmi.server.hostname=localhost"
  ${JAVA} -cp .. $JMX_ARGS JmxClientPort  $PORT
  sleep 4
popd
${JFR} print  $expectedDir/$FLIGHTFILE | (head; tail)
parsedLines=`cat $expectedDir/$FLIGHTFILE | wc -l`
test $parsedLines -gt 1000
rm $expectedDir/$FLIGHTFILE 
rm -rf workdir1
rm -rf workdir2
