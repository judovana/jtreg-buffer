FS="/"
JAVAC=${TESTJAVA}${FS}bin${FS}javac
JAVA=${TESTJAVA}${FS}bin${FS}java



$JAVAC -d . $TESTSRC/Test.java
R=$?
if [[ $R -ne 0 ]] ; then
  echo "Compilation1 failed"
  exit $R
fi

#just info
rpm -qR fontconfig

$JAVA Test
R=$?
echo "$JAVA Test ; returned $R"

for x in {1..20} ; do 
JDK="java-1.$x.0-openjdk"
a=`rpm -qR $JDK`
TR2=$?
if [[ $TR2 -eq 0 ]] ; then
  echo "$JDK  installed; checking fontconfig"
else
  echo "$JDK not installed; trying next"
  continue
fi
echo "$a" grep fontconfig
TR1=$?
if [[ $TR1 -eq 0 ]] ; then
 echo "$JDK requiring fontconfig. Thats ok. Continuing"
else
 echo "$JDK NOT requiring fontconfig. Thats fatal"
 exit 10
fi
done

exit $R



