echo $PWD

FS="/"
JAVAC=${TESTJAVA}${FS}bin${FS}javac
JAVA=${TESTJAVA}${FS}bin${FS}java

$JAVAC -d . $TESTSRC/Test.java 
R=$?
if [ "0$R" -ne "0" ] ; then
  echo "Compilation failed"
  exit $R
fi
cp -v $TESTSRC/test.gif . 
R=$?
if [ "0$R" -ne "0" ] ; then
  echo "Copy of resource failed"
  exit $R
fi

$JAVA  Test 
R=$?
if [ "0$R" -ne "0" ] ; then
  echo "run  failed"
  exit $R
fi


ls test.gif
R=$?
if [ "0$R" -ne "0" ] ; then
  echo "no image here"
  exit $R
fi

exit 0
