
# 852051 is real id, but jtreg needs 7 numbers bugs.... Howewer, trick with zero works....
# dont forget to include 0852051 as whole bug id
# @test
# @bug 0852051
# @summary  CVE-2012-4681-Inject-any-unsigned-code-via-flaw-in-bean-s-statement
# @run shell runtest-java.sh


FS="/"
JAVAC=${TESTJAVA}${FS}bin${FS}javac 
JAVA=${TESTJAVA}${FS}bin${FS}java

       echo "java main class - not applet"
       $JAVAC -d . $TESTSRC/GondvvTestcase.java
R=$?
if [[ $R -ne 0 ]] ; then
  echo "Compilation1 failed"
  exit 10
fi
        $JAVA -Djava.security.manager GondvvTestcase /bin/date &> java.log
		R=$?
		cat java.log
echo "java result $R"
if [[ $R -ne 0 ]] ; then
  echo "should finish sucessfully"
  exit 20
fi
        grep "OK: got expected" java.log
if [[ $? -ne 0 ]] ; then
  exit 30
fi
        grep "FAIL:" java.log
if [[ $? -eq 0 ]] ; then
  exit 40
fi


