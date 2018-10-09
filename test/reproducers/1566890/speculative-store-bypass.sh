#!/bin/sh
# @test
# @bug 1566890
# @summary test for 1566890
# @run shell/timeout=100 speculative-store-bypass.sh

set -eu

FS="/"
JAVA=${TESTJAVA}${FS}jre${FS}bin${FS}java

strace -f "${JAVA}" -help &> strace.log

if ! cat strace.log | grep "prctl(0x35 .*, 0, 0x4, 0, 0).*"  ; then
	echo "Missing expected call in strace output"
	exit 1
fi

exit 0
