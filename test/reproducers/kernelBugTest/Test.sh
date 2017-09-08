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
# @library commons-daemon-1.0.15.jar
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

JSVC_VERSION=commons-daemon-1.0.15
JSVC_SRC=${JSVC_VERSION}-src
JSVC_SRC_TARBALL=${TESTSRC}/${JSVC_VERSION}-src.tar.gz
JSVC_NATIVE_DIR=${JSVC_SRC}/src/native/unix
JSVC_NATIVE_BIN=${JSVC_SRC}/src/native/unix/jsvc
JSVC_DAEMON_JAR=${TESTSRC}/commons-daemon-1.0.15.jar
PIDFILE=$( pwd )/jsvc.pid

tar -xf ${JSVC_SRC_TARBALL}
pushd ${JSVC_NATIVE_DIR}
./configure --with-java=${TESTJAVA}
make
popd

${JSVC_NATIVE_BIN} -java-home ${TESTJAVA} -debug -outfile /dev/stdout -errfile /dev/stderr -pidfile ${PIDFILE} -cp ${TESTSRCPATH}:${TESTCLASSES} HelloWorld

sleep 5

kill $( cat $PIDFILE )