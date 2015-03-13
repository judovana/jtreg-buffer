echo $PWD

FS="/"
JAVAC=${TESTJAVA}${FS}bin${FS}javac
JAVA=${TESTJAVA}${FS}bin${FS}java

$JAVAC -d . $TESTSRC/Test.java 
R=$?
if [[ $R -ne 0 ]] ; then
  echo "On newer JDK compilationmust fail. OK - javacc returned $R"
  exit 0
fi

# older JDK, here the reproducer must verify field is unaccessible

$JAVA Test
R=$?
exit $R
