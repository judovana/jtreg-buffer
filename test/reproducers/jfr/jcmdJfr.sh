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
rm -rf workdir1 ; mkdir workdir1
rm -rf workdir2 ; mkdir workdir2
expectedDir=workdir1;

${JAVAC} -d . $TESTSRC/Server.java
pushd workdir1
  ${JAVA} -cp .. Server  8 &
  JPID=$!
popd
pushd workdir2
  ${JCMD} $JPID JFR.start duration=2s filename=$FLIGHTFILE
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
