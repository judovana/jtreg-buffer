#!/bin/sh
# @test
# @requires ( os.arch == "x86" ) | ( os.arch == "i386" ) | ( os.arch == "i486" ) | ( os.arch == "i586" ) | ( os.arch == "i686" )
# @bug 1536623
# @summary 32 bit java app started via JNI crashes with larger stack sizes
# @run shell/timeout=100 32-bit-JNI-crash-larger-stack.sh

set -eu

if [ "x$OTOOL_BUILD_ARCH" == "xi686" ] ; then
  echo "skipped on i686"
  exit 0
fi

FS="/"
testDir="02007382.loadjvm_test"
cp -a "${TESTSRC}${FS}${testDir}" .
pushd "${testDir}"
sed -i "s#^ORACLEJDK_JAVA_HOME_I386=.*\$#ORACLEJDK_JAVA_HOME_I386=${TESTJAVA}#g" "Makefile"
make all
popd
