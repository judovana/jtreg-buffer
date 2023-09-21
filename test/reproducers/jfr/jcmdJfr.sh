##
## @test 
## @bug 1444666
## @summary check if jfr can be started in runtime via jcmd, write file, and that file is readable

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

JDK8=0
$JAVA -version 2>&1 | grep 1.8.0 || JDK8=$?
if  [ $JDK8 -eq 0 ] ; then
  TRESH=500
else
  TRESH=1000
fi

FLIGHTFILE=flight2.jfr

${JAVAC} -d . $TESTSRC/Server.java
${JAVA} Server  8 &
JPID=$!
${JCMD} $JPID JFR.start duration=2s filename=$FLIGHTFILE
sleep 4
${JFR} print  $FLIGHTFILE | (head; tail)
parsedLines=`cat $FLIGHTFILE | wc -l`
test $parsedLines -gt $TRESH
rm $FLIGHTFILE 
