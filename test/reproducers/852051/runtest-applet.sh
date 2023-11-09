
# 852051 is real id, but jtreg needs 7 numbers bugs.... Howewer, trick with zero works....
# dont forget to include 0852051 as whole bug id
# @test
# @bug 0852051
# @summary  CVE-2012-4681-Inject-any-unsigned-code-via-flaw-in-bean-s-statement
# in reality this test needs jdk 8 and older, but with ajvaws/apple itcedtea-web. Efectively disabling
# @requires  jdk.version.major < 9
# @run shell runtest-applet.sh


FS="/"
JAVAC=${TESTJAVA}${FS}bin${FS}javac 
APPLETVIEWER=${TESTJAVA}${FS}bin${FS}appletviewer

     cp $TESTSRC/exploit.html . 
     echo "applet"
        $JAVAC -d . $TESTSRC/GondvvTestcaseApplet.java
        $APPLETVIEWER exploit.html &> applet.log &
        APID=$!
        sleep 10
        kill -9 $APID
        cat applet.log

        grep "java.awt.AWTError" applet.log        
if [[ $? -eq 0 ]] ; then
  echo "detected unrelated X error, skipping"
  exit 0
fi

        grep "OK: got expected" applet.log
if [[ $? -ne 0 ]] ; then
  exit 50
fi
        grep "FAIL:" applet.log
if [[ $? -eq 0 ]] ; then
  exit 60
fi
        grep "Final state: PASS" applet.log
if [[ $? -ne 0 ]] ; then
  exit 50
fi

