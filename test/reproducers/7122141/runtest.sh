FS="/"
JAVAC=${TESTJAVA}${FS}bin${FS}javac
JAVA=${TESTJAVA}${FS}bin${FS}java

$JAVAC -d .  $(find $TESTSRC/annotationinitialisationrace -name \*.java)
R=$?
if [[ $R -ne 0 ]] ; then
  echo "Compilation1 failed"
  exit $R
fi
$JAVA annotationinitialisationrace/AnnotationInitialisationRace &
java_pid=$!
sleep 5;
ps | grep $java_pid
R=$?
        if [[ $R -eq 0 ]]; then
            echo "java not finished, see kill -3 bellow"
            kill -3 $java_pid # write info about process
            kill -9 $java_pid # kill it with fire
        fi

$JAVA annotationinitialisationrace/AnnotationInitialisationRace
R=$?
exit $R

