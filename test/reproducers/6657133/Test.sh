echo $PWD

FS="/"
JAVAC=${TESTJAVA}${FS}bin${FS}javac
JAVA=${TESTJAVA}${FS}bin${FS}java
if [ "x$TESTSRC" == "x" ] ; then
  TESTSRC="."
fi

$JAVAC -d . $TESTSRC/Test.java 
R=$?
if [ "0$R" -ne "0" ] ; then
  echo "On newer JDK default compilation must fail. OK - javac returned $R"
  # ok, how new jdk?
  if ! ${JAVA}  -Djava.security.manager=allow -version | grep "Could not create SecurityManager" ; then
   echo "jdk with no security manager, passing"
   exit 0
  else 
   echo "jdk with security manager, but no com.sun.imageio.plugins.jpeg.JPEG class"
   exit 0
  fi
fi

# older JDK, here the reproducer must verify field is unaccessible
$JAVA -Djava.security.manager com.sun.imageio.plugins.jpeg.Test
R=$?
exit $R
