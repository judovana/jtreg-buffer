##
## @test 
## @bug 1444666
## @summary check if jfr can be stopped in runtime via jcmd, write file, and that file is readable

if [ "x$OTOOL_jfr" == "xjfron" ] ; then
  echo "ignored in OTOOL_jfr=jfron"
  exit 0
fi

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
rm -rf workdir1 ; mkdir workdir1
rm -rf workdir2 ; mkdir workdir2
expectedDir=workdir1;

${JAVAC} -d . $TESTSRC/Server.java
pushd workdir1
  ${JAVA} -cp .. Server  8 &
  JPID=$!
popd
pushd workdir2
  ${JCMD} $JPID JFR.start filename=$FLIGHTFILE
  sleep 2
  ${JCMD} $JPID JFR.dump name=1
  ${JCMD} $JPID JFR.stop name=1
popd
${JFR} print  $expectedDir/$FLIGHTFILE | (head; tail)
parsedLines=`cat $expectedDir/$FLIGHTFILE | wc -l`
test $parsedLines -gt 1000

sleep=`${JFR} summary  $expectedDir/$FLIGHTFILE | grep ThreadSleep`
count=`echo "$sleep"   | sed "s/ \+/ /g"  |  cut -d ' ' -f 3`
#??test $count -gt 100

rm $expectedDir/$FLIGHTFILE 
rm -rf workdir1
rm -rf workdir2
