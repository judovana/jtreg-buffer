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
# @run shell/timeout=30 Test.sh

set -x
set -e

if [ "$TESTJAVA" ]; then
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

OS_TYPE_ARG=""
# workarounded broken os detection on fedora
if cat /etc/redhat-release | grep -iq fedora ; then
  OS_TYPE_ARG="--with-os-type=include/linux"
fi

tar -xf ${JSVC_SRC_TARBALL}
pushd ${JSVC_NATIVE_DIR}
./configure --with-java=${TESTJAVA} ${OS_TYPE_ARG}
make
popd

${JSVC_NATIVE_BIN} -java-home ${TESTJAVA} -debug -outfile /dev/stdout -errfile /dev/stderr -pidfile ${PIDFILE} -cp ${TESTSRCPATH}/${JSVC_JAR}:${TESTCLASSES} HelloWorld

sleep 5

kill $( cat $PIDFILE )