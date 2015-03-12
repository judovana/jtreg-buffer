
FS="/"
JAVAC=${TESTJAVA}${FS}bin${FS}javac
JAVA=${TESTJAVA}${FS}bin${FS}java

LOG=./7013969.log

$JAVAC -d . $TESTSRC/Test7013969.java

if [ $? -ne 0 ] ; then
 echo "compilation failed"
 exit 10
fi

$JAVA Test7013969 > $LOG 2>&1

if [ $? -ne 0 ] ; then
 echo "run failed"
 exit 100
fi

cat  $LOG


if [ ! -s $LOG ] ; then
	echo "zero size logfile"
	exit 110
fi

grep  "addresses" $LOG

if [ $? -eq 0 ] ; then
 echo "found addresses, should not"
 exit 1
fi
grep  -E "/[0-9]+\.[0-9]+\.[0-9]+\.[0-9]|/[0-9a-f]+:[0-9a-f]+" $LOG
if [ $? -eq 0 ] ; then
 echo "found address, should not"
 exit 2
fi
