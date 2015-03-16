
FS="/"
JAVAC=${TESTJAVA}${FS}bin${FS}javac
JAVA=${TESTJAVA}${FS}bin${FS}java



$JAVAC -d . $TESTSRC/TestScript.java
R=$?
if [[ $R -ne 0 ]] ; then
  echo "Compilation1 failed"
  exit $R
fi

$JAVA -cp . TestScript &> log1
#$TESTSRC/rhino-1.5r5.jar ?
R=$?
cat log1
if [[ $R -ne 0 ]] ; then
  echo "Run1 failed"
  exit $R
fi

grep "Scripting components FOUND successfully." log1
R=$?
if [[ $R -ne 0 ]] ; then
  echo "grep1 failed"
  exit $R
fi

$JAVAC -cp $TESTSRC/rhino-1.5r5.jar $TESTSRC/UpstreamRhinoClassPathTest.java &> log2
R=$?
cat log2
if [[ $R -ne 0 ]] ; then
  echo "Compilation2 failed"
  exit $R
fi

cat log2
ls -l

if [[ !  -z `cat log2` ]] ; then
  echo "Empty output"
  exit 100
fi

exit 0



