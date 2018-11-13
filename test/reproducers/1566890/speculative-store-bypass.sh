#!/bin/sh
# @test
# @bug 1566890
# @summary test for 1566890
# @run shell/timeout=100 speculative-store-bypass.sh

set -eu

FS="/"
JAVA=${TESTJAVA}${FS}jre${FS}bin${FS}java

if ! type strace &> /dev/null ; then
	echo "ERROR: Missing strace!" 1>&2
	exit 1
fi

strace -f "${JAVA}" -help &> strace.log

if ! cat strace.log | grep "prctl(0x35 .*, 0, 0x4, 0, 0).*" \
&& ! cat strace.log | grep "prctl(PR_SET_SPECULATION_CTRL, PR_SPEC_STORE_BYPASS, PR_SPEC_DISABLE.*" ; then
	echo "FAILED: Missing expected call in strace output" 1>&2
	exit 1
fi

exit 0
