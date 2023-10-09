##
## @test 
## @requires jdk.version.major > 10
## @bug 1444666
## @summary check if java class can flight record itself

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
else
  JAVAC=$(readlink -f `which javac`)
  JAVA=$(readlink -f `which java`)
  JFR=$(readlink -f `which jfr`)
fi

if [ "x${TESTSRC}" == "x" ] ; then
  TESTSRC=`pwd`
fi

#hardcoded in Self.java
FLIGHTFILE=flightSelf.jfr

${JAVAC} -d . $TESTSRC/Self.java
${JAVA} Self  2
${JFR} print  $FLIGHTFILE | (head; tail)
parsedLines=`cat $FLIGHTFILE | wc -l`
test $parsedLines -gt 1000

sleep=`${JFR} summary  $FLIGHTFILE | grep ThreadSleep`
count=`echo "$sleep"   | sed "s/ \+/ /g"  |  cut -d ' ' -f 3`
test $count -gt 100

rm $FLIGHTFILE 
