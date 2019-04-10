# @test
# @bug 7143851
# @summary CVE-2012-1719-OpenJDK-mutable-repository-identifiers-in-generated-stub-code-CORBA-7143851
# @requires jdk.version.major <= 10
# @run shell  runtest.sh

# set platform-dependent variables
OS=`uname -s`
case "$OS" in
  SunOS | Linux )
    PS=":"
    FS="/"
    ;;
  Windows_* | CYGWIN_NT* )
    PS=";"
    FS="\\"
    ;;
  * )
    echo "Unrecognized system!"
    exit 1;
    ;;
esac

JAVA=${TESTJAVA}${FS}bin${FS}java
JAVAC=${TESTJAVA}${FS}bin${FS}javac
RMIC=${TESTJAVA}${FS}bin${FS}rmic
TOOLS=${TESTJAVA}${FS}lib${FS}tools.jar
   grep_generated() {
            for i in _RemoteInterfaceTest_Stub _RemoteInterfaceTestImpl_Tie ; do
            if grep 'return.*type_ids' "$i.java" | grep -q clone ; then
                echo "pass $i"
            else
                echo "FAIL $i"
                grep -A1 -B1 'return.*type_ids' "$i.java"
                rv=1
            fi
            done
        }



echo 'Testing rmic generated code'
$JAVAC -d .  ${TESTSRC}${FS}RemoteInterfaceTest*.java
        rv=0
        echo "- rmic -iiop"
$RMIC -iiop -always -keep RemoteInterfaceTestImpl
        grep_generated
        rm -f _RemoteInterfaceTest*
        echo '- rmic -iiop -poa'
        $RMIC -iiop -poa -always -keep RemoteInterfaceTestImpl
        grep_generated
        rm -f _RemoteInterfaceTest*

        echo "rmic generates code with clone calls $rv"


    echo "built with fixed rmic & StubGenerator"
    $JAVAC -d . -cp $TOOLS -XDignore.symbol.file ${TESTSRC}${FS}RmicIdCloneCheck.java
    $JAVA -cp $TOOLS${PS}. RmicIdCloneCheck | tee output.txt
        echo "Doing asserts on the output"
        for W in javax.management.remote.rmi._RMIServerImpl_Tie \
                 javax.management.remote.rmi._RMIConnectionImpl_Tie \
                 'POA: false' \
                 'POA: true'; do
            STR=$(grep "$W" output.txt)
            if echo $STR | grep -q FAIL; then
                echo "FAIL $W"
                rv=1
            elif echo $STR | grep -q OK; then
                echo "pass $W"
            else
                echo "Unexpected output: $STR"
                rv=1
            fi
        done

exit $rv
