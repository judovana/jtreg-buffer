#!/bin/sh
# @test
# @bug 6666667
# @summary reproducer of kernel bug which appeared in kernels
#                 2.6.18-348.33.2.el5
#                 2.6.18-420.el5
#                 2.6.32-220.72.2.el6
#                 2.6.32-358.79.2.el6
#                 2.6.32-431.81.2.el6
#                 2.6.32-431.80.2.el6
#                 2.6.32-504.60.2.el6
#                 2.6.32-573.42.2.el6
#                 2.6.32-573.43.2.el6
#                 2.6.32-696.3.2.el6
#                 3.10.0-327.55.2 <tel:3.10.0-327.55.2>.el7
#                 3.10.0-514.21.2 <tel:3.10.0-514.21.2>.el7
#                 3.10.0-514.26.1 <tel:3.10.0-514.26.1>.el7
# @requires os.family != "windows"
# @library commons-daemon-1.1.0.jar
# @compile  HelloWorld.java
# @run shell/timeout=120 Test.sh

set -x
set -e

#for some reason the build of the native code below pops to "/". Then the jfr cannot write. Movingit to scratch (pwd) while in it
export JAVA_TOOL_OPTIONS=`echo $JAVA_TOOL_OPTIONS | sed "s;filename=myrecording.jfr;filename=$PWD/myrecording.jfr;"`

if ! [ -z "$TESTJAVA" ]; then
  JAVA=${TESTJAVA}/bin/java
  JAVAC=${COMPILEJAVA}/bin/javac
  JAR=${TESTJAVA}/bin/jar
else
  echo "TESTJAVA must be set"
  exit 1
fi

JSVC_VERSION=commons-daemon-1.1.0
JSVC_JAR=${JSVC_VERSION}.jar
JSVC_SRC=${JSVC_VERSION}-src
JSVC_SRC_TARBALL=${TESTSRC}/${JSVC_VERSION}-src.tar.gz
JSVC_NATIVE_DIR=${JSVC_SRC}/src/native/unix
JSVC_NATIVE_BIN=${JSVC_SRC}/src/native/unix/jsvc
JSVC_DAEMON_JAR=${TESTSRC}/${JSVC_JAR}
PIDFILE=$( pwd )/jsvc.pid

tar -xf ${JSVC_SRC_TARBALL}
pushd ${JSVC_NATIVE_DIR}
# fix for problem with wrong include dir on aarch64
sed -i 's;supported_os="aarch64";# supported_os="aarch64";g' ./configure

./configure --with-java=${TESTJAVA}
make
popd

${JSVC_NATIVE_BIN} -java-home ${TESTJAVA} -debug -outfile /dev/stdout -errfile /dev/stderr -pidfile ${PIDFILE} -cp ${TESTSRCPATH}/${JSVC_JAR}:${TESTCLASSES} HelloWorld

# wait for PIDFILE file to appear (necessary?)
t=0
while [ "$t" -lt 30  ] ; do
    [ -e "${PIDFILE}" ] && break
    sleep 1
    t=$(( ++t ))
done

sleep 5

kill $( cat $PIDFILE )

# wait for PIDFILE to disappear (fix for problem)
# jtreg harness sometimes failed on cleanup phase (trying to remove pid file)
# could not reproduce, but this should hopefully help
t=0
while [ "$t" -lt 30  ] ; do
    ! [ -e "${PIDFILE}" ] && break
    sleep 1
    t=$(( ++t ))
done
