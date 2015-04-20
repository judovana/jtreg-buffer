FS="/"
JAVAC=${TESTJAVA}${FS}bin${FS}javac
JAVA=${TESTJAVA}${FS}bin${FS}java
LOG=./7046823.log

$JAVAC -d . $TESTSRC/bug7046823.java
appletviewer -J-Djava.security.policy=$TESTSRC/bug7046823.policy bug7046823.class &> $LOG
AR=$?

cat $LOG

exit $AR
