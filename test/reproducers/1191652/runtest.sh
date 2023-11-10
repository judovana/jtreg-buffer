#!bin/bash

set -exo pipefail

FS="/"
JAVAC=${TESTJAVA}${FS}bin${FS}javac
JAVA=${TESTJAVA}${FS}bin${FS}java

$JAVAC -d . $TESTSRC/RH1187252.java
OUT=output.log
$JAVA RH1187252 | tee $OUT

# example output:
# java.library.path = /usr/lib64/mpich/lib:/usr/java/packages/lib/ppc64le:/usr/lib64:/lib64:/lib:/usr/lib
# sun.boot.library.path = /usr/lib/jvm/java-1.7.0.75-2.5.4.5.ael7b.ppc64le/jre/lib/ppc64le
# os.arch = ppc64le
#/usr/lib/jvm/java-1.8.0-openjdk/bin/java  RH1187252
#java.library.path = /usr/java/packages/lib/amd64:/usr/lib64:/lib64:/lib:/usr/lib
#sun.boot.library.path = /usr/lib/jvm/java-1.8.0-openjdk-1.8.0.382.b05-2.fc38.x86_64/jre/lib/amd64
#os.arch = amd64
#/usr/lib/jvm/java-11-openjdk/bin/java  RH1187252
#java.library.path = /usr/java/packages/lib:/usr/lib64:/lib64:/lib:/usr/lib
#sun.boot.library.path = /usr/lib/jvm/java-11-openjdk-11.0.20.0.8-1.fc38.x86_64/lib
#os.arch = amd64
#/usr/lib/jvm/java-17-openjdk/bin/java  RH1187252
#java.library.path = /usr/java/packages/lib:/usr/lib64:/lib64:/lib:/usr/lib
#sun.boot.library.path = /usr/lib/jvm/java-17-openjdk-17.0.8.0.7-1.fc38.x86_64/lib
#os.arch = amd64
#/usr/lib/jvm/java-21-openjdk/bin/java  RH1187252
#java.library.path = /usr/java/packages/lib:/usr/lib64:/lib64:/lib:/usr/lib
#sun.boot.library.path = /usr/lib/jvm/java-21-openjdk-21.0.0.0.35-1.rolling.fc38.x86_64/lib
#os.arch = amd64


ARCH_JAVA=$(uname -m)
ARCH_OS=$(uname -m)
# java is using amd64 instead of x86_64
case $ARCH_JAVA in 
            x86_64)
                ARCH_JAVA=amd64
                ;;
esac
cat  $OUT | grep "java.library.path = .*java/packages/lib" #this changed to arch-les somwher eon the fly
cat  $OUT | grep "java.library.path = .*/usr/lib"
cat  $OUT | grep "java.library.path = .*/usr/lib64"
cat  $OUT | grep -e "sun.boot.library.path = .*java.*1.8.0.*/lib/$ARCH_JAVA" -e "sun.boot.library.path = .*java.*/lib"
cat  $OUT | grep "os.arch = $ARCH_JAVA"

