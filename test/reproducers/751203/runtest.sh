#/bin/sh

BIN=`mktemp -d `

# set platform-dependent variables
OS=`uname -s`
case "$OS" in
  SunOS | Linux )
    NULL=/dev/null
    FS="/"
    ;;
  Windows_* | CYGWIN_NT* )
    NULL=NUL
    FS="\\"
    BIN=$( cygpath -pw $BIN )
    ;;
  * )
    echo "Unrecognized system!"
    exit 1;
    ;;
esac

JAVAC=${TESTJAVA}${FS}bin${FS}javac
JAVA=${TESTJAVA}${FS}bin${FS}java
RMIREGISTRY=${TESTJAVA}${FS}bin${FS}rmiregistry
JPS=${TESTJAVA}${FS}bin${FS}jps

echo "It is recommanded to have jps to have this working smoothly"

killOldJavas(){
XIFS=$IFS
IFS="
"
echo killOldJavas
allOldProcesses=`find /proc -maxdepth 1  -type d -mmin +1 -exec basename {} \; `
allJavaProcessesAndMainClasses=`${JPS}`
for p in $allOldProcesses ; do
  for j in $allJavaProcessesAndMainClasses ; do
# main class
   jp=`echo $j | sed "s/ .*$//"`
# java pid
   jm=`echo $j | sed "s/^.* //"`
     if [ $jp -eq $p  ] 2>${NULL}; then
      if [ "$jm" = "HelloServer" -o "$jm" =  "RegistryImpl" ] 2>${NULL}; then
        t=`ps -p $p -o etime=`
        echo "$jp/$p named $jm is running $t. Killing"
		kill -9 $p
      fi
    fi
  done
done
IFS=$XIFS
}



cleanup(){
    killOldJavas
    kill -9 $RMIPID $SERVERPID
    rm -rf $BIN
}


killOldJavas
CODEBASE=file://$BIN
echo $BIN

$JAVAC -d $BIN $TESTSRC/*.java
R=$?
if [[ $R -ne 0 ]] ; then
  echo "Compilation failed"
  exit $R
fi


   echo "Server"
   pushd $BIN
   $RMIREGISTRY "-J-Djava.rmi.server.codebase=$CODEBASE" &
   RMIPID=$!
   popd
   sleep 10
   ps -o cmd= -p $RMIPID
   $JAVA -cp $BIN "-Djava.rmi.server.codebase=$CODEBASE" HelloServer &> server.out &
   SERVERPID=$!
   sleep 10
   grep  'Exception' server.out
   R=$?
   cat client.out
   if [[ $R -eq 0 ]] ; then
    echo "exception during start of rmiserver"
    cat server.out
    cleanup
    exit 35
   fi
   while ! grep -q 'Hello Server is ready.' server.out; do sleep 1; done
   cat server.out

   echo "Client"
   $JAVA -cp $BIN "-Djava.rmi.server.codebase=$CODEBASE" HelloClient &> client.out
   R=$?
   cat client.out
   if [[ $R -ne 0 ]] ; then
    echo "client not run"
    cleanup
    exit $R
   fi
   grep "Hello, I'm" client.out
   R=$?
   if [[ $R -ne 0 ]] ; then
    echo "client run failed"
    cleanup
    exit 5
  fi

    echo "Final checks"
    grep "client connected" server.out
    R=$?
    if [[ $R -ne 0 ]] ; then
      echo "client was not connected"
      cleanup
      exit 25
    fi
    MSG=$(grep 'Sending message' server.out | sed 's/.*"\([^"]\+\)".*/\1/')
     grep  "$MSG" client.out
     R=$?
    if [[ $R -ne 0 ]] ; then
      echo "connection wrong"
      cleanup
      exit 30
    fi

    cat server.out
    cleanup
    ${JPS}
    ps | grep java
exit 0
