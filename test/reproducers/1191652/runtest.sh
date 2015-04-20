FS="/"
JAVAC=${TESTJAVA}${FS}bin${FS}javac
JAVA=${TESTJAVA}${FS}bin${FS}java


        $JAVAC -d . $TESTSRC/RH1187252.java
        $JAVA RH1187252 &> output.log"

# example output:
# java.library.path = /usr/lib64/mpich/lib:/usr/java/packages/lib/ppc64le:/usr/lib64:/lib64:/lib:/usr/lib
# sun.boot.library.path = /usr/lib/jvm/java-1.7.0.75-2.5.4.5.ael7b.ppc64le/jre/lib/ppc64le
# os.arch = ppc64le

# bug was about ppc64le reported as ppc64
        ARCH=$(rlGetPrimaryArch)
        # in java nothing is as expected
        case $ARCH in 
            x86_64)
                ARCH=amd64
                ;;
        esac
        rlAssertGrep "java.library.path = .*java/packages/lib/$ARCH:" output.log
        rlAssertGrep "sun.boot.library.path = .*jre/lib/$ARCH" output.log
        rlAssertGrep "os.arch = $ARCH" output.log
        rlGetTestState || cat output.log
    rlPhaseEnd

    rlPhaseStartCleanup
        rlBundleLogs LOGS *.log
        rlRun "popd"
        rlRun "rm -r $TmpDir" 0 "Removing tmp directory"
    rlPhaseEnd
rlJournalPrintText
rlJournalEnd
