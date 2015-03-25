#!/bin/sh
# @test
# @bug 7023639
# @summary another exploit of thoger's collection
# @run shell/timeout=100 runtest.sh

if [ "$TESTJAVA" ]; then
	java=${TESTJAVA}/bin/java
	javac=${TESTJAVA}/bin/javac
else
	java=java
	javac=javac
fi

$javac -d . $TESTSRC/*.java
$javac -d . $TESTSRC/data/*.java

$java BlackBox
