##
## @test 
## @bug 1444666
## @summary check if jfr starts on startup can be dumped and stopped externaly

set -exo pipefail

if [ ! "x${TESTJAVA}" == "x" ] ; then
  FS="/"
  JAVAC=${TESTJAVA}${FS}bin${FS}javac
  JAVA=${TESTJAVA}${FS}bin${FS}java
  JFR=${TESTJAVA}${FS}bin${FS}jfr
  JCMD=${TESTJAVA}${FS}bin${FS}jcmd
else
  JAVAC=$(readlink -f `which javac`)
  JAVA=$(readlink -f `which java`)
  JFR=$(readlink -f `which jfr`)
  JCMD=$(readlink -f `which jcmd`)  
fi

if [ "x${TESTSRC}" == "x" ] ; then
  TESTSRC=`pwd`
fi

FLIGHTFILE=flight4.jfr

${JAVAC} -d . $TESTSRC/Server.java
${JAVA} -XX:+FlightRecorder  -XX:StartFlightRecording=filename=$FLIGHTFILE,dumponexit=true Server  8 &
JPID=$!
sleep 2
${JCMD} $JPID JFR.dump name=1
${JCMD} $JPID JFR.stop name=1
${JFR} print  $FLIGHTFILE 
rm $FLIGHTFILE 
