##
## @test 
## @bug 1444666
## @summary check if jfr starts on startup, write file, and that file is readable

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

FLIGHTFILE=flight1.jfr

${JAVAC} -d . $TESTSRC/Server.java
${JAVA} -XX:+FlightRecorder  -XX:StartFlightRecording=filename=$FLIGHTFILE,dumponexit=true Server  2
${JFR} print  $FLIGHTFILE | (head; tail)
parsedLines=`cat $FLIGHTFILE | wc -l`
test $parsedLines -gt 1000
rm $FLIGHTFILE 
