#!/bin/bash
FS="/"
JAVAC=${TESTJAVA}${FS}bin${FS}javac
JAVA=${TESTJAVA}${FS}bin${FS}java


$JAVAC  -d . $TESTSRC/GenOOMCrashClass.java
R=$?
if [[ $R -ne 0 ]] ; then
  echo "Compilation1 failed"
  exit $R
fi
$JAVA GenOOMCrashClass 1 4000
R=$?
if [[ $R -ne 0 ]] ; then
  echo "generation failed"
  exit $R
fi
$JAVA OOMCrashClass4000_1 > output 2>&1
R=$?
echo "***out*put***"
cat output
echo "****e*n*d****"
echo "OOMCrashClass4000_1 run returned $?"
if [[ $R -eq 0 ]] ; then
  echo "run passed, had to fail!"
  exit 10
fi

grep  "SIGSEGV|An unexpected error has been detected" output -E  
R=$?
if [[ $R -eq 0 ]] ; then
  echo "log had SIGSEV"
  exit 20
fi

#jvm specific error. More important is failed run OOMCrashClass4000_1
#grep "java.lang.LinkageError" output
#R=$?
#if [[ $R -ne 0 ]] ; then
#  echo "log did not had LinkageError"
#  exit 30
#fi

grep "Error" output
R1=$?
grep "Exception" output
R2=$?
if [ $R1 -ne 0 -a $R2 -ne 0  ] ; then
  echo "no exception/error found"
  exit 30
fi
