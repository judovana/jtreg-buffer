#/bin/sh

BIN=$(mktemp -d)

# set platform-dependent variables
NETSTAT_ARGS="-tln"
OS=$(uname -s)
case "$OS" in
SunOS | Linux)
  NULL=/dev/null
  FS="/"
  CODEBASE=file://$BIN
  ;;
Windows_* | CYGWIN_NT*)
  NULL=NUL
  FS="\\"
  BIN=$(cygpath -pw $BIN)
  CODEBASE=file:///$BIN
  NETSTAT_ARGS="-an"
  ;;
*)
  echo "Unrecognized system!"
  exit 1
  ;;
esac

JAVAC=${TESTJAVA}${FS}bin${FS}javac
JAVA=${TESTJAVA}${FS}bin${FS}java
RMIREGISTRY=${TESTJAVA}${FS}bin${FS}rmiregistry
JPS=${TESTJAVA}${FS}bin${FS}jps

echo "It is recommanded to have jps to have this working smoothly"

killOldJavas() {
  XIFS=$IFS
  IFS="
"
  echo killOldJavas
  allOldProcesses=$(find /proc -maxdepth 1 -type d -mmin +1 -exec basename {} \;)
  allJavaProcessesAndMainClasses=$(${JPS})
  for p in $allOldProcesses; do
    for j in $allJavaProcessesAndMainClasses; do
      # main class
      jp=$(echo $j | sed "s/ .*$//")
      # java pid
      jm=$(echo $j | sed "s/^.* //")
      if [ $jp -eq $p ] 2>${NULL}; then
        if [ "$jm" = "HelloServer" -o "$jm" = "RegistryImpl" ] 2>${NULL}; then
          t=$(ps -p $p -o etime=)
          echo "$jp/$p named $jm is running $t. Killing"
          kill -9 $p
        fi
      fi
    done
  done
  IFS=$XIFS
}

cleanup() {
  killOldJavas
  kill -9 $RMIPID $SERVERPID
  # wait for servers to die
  wait
  rm -rf $BIN
}

killOldJavas

$JAVAC -d $BIN $TESTSRC/*.java
R=$?
if [[ $R -ne 0 ]]; then
  echo "Compilation failed"
  exit $R
fi

echo "Server"
pushd $BIN
$RMIREGISTRY "-J-Djava.rmi.server.codebase=$CODEBASE" &
RMIPID=$!
popd
# wait for rmiregistry to start
t=0
while [ "$t" -lt 30  ] ; do
	netstat ${NETSTAT_ARGS} | grep -q ':1099 ' && break
    sleep 1
    t=$(( ++t ))
done

ps -o cmd= -p $RMIPID
$JAVA -cp $BIN "-Djava.rmi.server.codebase=$CODEBASE" HelloServer &>server.out &
SERVERPID=$!
# wait for Hello server
t=0
while [ "$t" -lt 30  ] ; do
    if grep -q 'Exception' server.out ; then
        echo "exception during start of rmiserver"
        cat server.out
        cleanup
        exit 35
    fi
    grep -q 'Hello Server is ready.' server.out && break
    sleep 1
    t=$(( ++t ))
done

echo "Client"
$JAVA -cp $BIN "-Djava.rmi.server.codebase=$CODEBASE" HelloClient &>client.out
R=$?
cat client.out
if [[ $R -ne 0 ]]; then
  echo "client not run"
  cleanup
  exit $R
fi
grep "Hello, I'm" client.out
R=$?
if [[ $R -ne 0 ]]; then
  echo "client run failed"
  cleanup
  exit 5
fi

echo "Final checks"
grep "client connected" server.out
R=$?
if [[ $R -ne 0 ]]; then
  echo "client was not connected"
  cleanup
  exit 25
fi
MSG=$(grep 'Sending message' server.out | sed 's/.*"\([^"]\+\)".*/\1/')
grep "$MSG" client.out
R=$?
if [[ $R -ne 0 ]]; then
  echo "connection wrong"
  cleanup
  exit 30
fi

cat server.out
cleanup
${JPS}
ps | grep java
exit 0
