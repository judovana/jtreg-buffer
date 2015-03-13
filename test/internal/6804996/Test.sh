echo $PWD

FS="/"
JAVAC=${TESTJAVA}${FS}bin${FS}javac
JAVA=${TESTJAVA}${FS}bin${FS}java

$JAVAC -d . $TESTSRC/Test.java 
R=$?
if [[ $R -ne 0 ]] ; then
  echo "Compilation failed"
  exit $R
fi
cp -v $TESTSRC/splash.png . 
R=$?
if [[ $R -ne 0 ]] ; then
  echo "Copy of resource failed"
  exit $R
fi

$JAVA -splash:splash.png -showversion Test > LOG
R=$?
if [[ $R -ne 0 ]] ; then
  echo "run  failed"
  exit $R
fi

cat LOG
R=$?
if [[ $R -ne 0 ]] ; then
  echo "no log"
  exit $R
fi

grep splash.png LOG
if [[ $R -ne 0 ]] ; then
  echo "no spalsh in log"
  exit $R
fi

grep done.png LOG
if [[ $R -ne 0 ]] ; then
  echo "testnot done"
  exit $R
fi

exit 0
