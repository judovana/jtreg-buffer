##
## @test 
## @bug 1444666
## @summary check if jfr can be stopped in runtime via jcmd, write file, and that file is readable

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

FLIGHTFILE=flight3.jfr

${JAVAC} -d . $TESTSRC/Server.java
${JAVA} Server  8 &
JPID=$!
${JCMD} $JPID JFR.start filename=$FLIGHTFILE
sleep 2
${JCMD} $JPID JFR.dump name=1
${JCMD} $JPID JFR.stop name=1
${JFR} print  $FLIGHTFILE 
rm $FLIGHTFILE 
