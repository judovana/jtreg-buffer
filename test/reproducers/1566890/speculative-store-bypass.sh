#!/bin/sh
# @test
# @bug 1566890
# @summary test for 1566890
# @run shell/timeout=100 speculative-store-bypass.sh

if ! type strace &> /dev/null ; then
  if which dnf ; then
    sudo dnf install strace -y
  elif which yum ; then
    sudo yum install strace -y
  fi
fi

FS="/"
JAVA=${TESTJAVA}${FS}bin${FS}java

if ! type strace &> /dev/null ; then
	echo "ERROR: Missing strace!" 1>&2
	exit 1
fi

strace -f "${JAVA}" -help &> strace.log

# Filter PR_SET_SPECULATION_CTRL calls
# Note: 0x35 (base 16) == 53 (base 10) is the numerical value.
grep -E 'prctl\(PR_SET_SPECULATION_CTRL|prctl\(0x35' strace.log > filtered_strace.log

echo "---------------- DEBUG: START (PR_SET_SPECULATION_CTRL calls) ------------"
cat filtered_strace.log
echo "----------------- DEBUG: END (PR_SET_SPECULATION_CTRL calls) ------------"
echo

num_calls=$(wc -l filtered_strace.log | sed 's|\([0-9]\+\).*filtered_strace.log|\1|g' || echo 0)
if [ $num_calls -gt 2 ]; then
  echo "WARNING: Unexpected number of prctl(PR_SET_SPECULATION_CTRL ...) calls"
  echo "See debug info above for more info."
fi

# First PR_SPEC_DISABLE_NOEXEC is being attempted and if that
# fails, PR_SPEC_DISABLE.
# 
#  Note: PR_SPEC_DISABLE_NOEXEC  == 0x10 (base 16) == 10000 (base 2)
#        PR_SPEC_DISABLE         == 0x4  (base 16) ==   100 (base 2)
#
# Thus, we should *always* see a call with PR_SPEC_DISABLE_NOEXEC in strace
# output. We *may* see a call to PR_SPEC_DISABLE.
# Pass the reproducer iff:
#  - only PR_SPEC_DISABLE_NOEXEC is present *and* 0 was returned
#  - both PR_SPEC_DISABLE_NOEXEC *and* PR_SPEC_DISABLE calls are present
#
# Example lines in strace (Post April 2019 CPU)
#
# RHEL 7.7 java-11-openjdk:
#
# prctl(0x35 /* PR_??? */, 0, 0x10, 0, 0) = -1 ENXIO (No such device or address)
# prctl(0x35 /* PR_??? */, 0, 0x4, 0, 0)  = -1 ENXIO (No such device or address)
#
# Fedora 29 patched JDK 11:
#
# prctl(PR_SET_SPECULATION_CTRL, PR_SPEC_STORE_BYPASS, 0x10 /* PR_SPEC_??? */) = -1 ERANGE (Numerical result out of range)
# prctl(PR_SET_SPECULATION_CTRL, PR_SPEC_STORE_BYPASS, PR_SPEC_DISABLE) = 0
#

DISABLE_NOEXEC=0

if grep -q "PR_SPEC_DISABLE_NOEXEC" filtered_strace.log || grep -q "0x10" filtered_strace.log ; then
  echo "DEBUG: PR_SPEC_DISABLE_NOEXEC call present. Checking for 0 return code..."
  DISABLE_NOEXEC=$(( $DISABLE_NOEXEC + 1 ))
  if grep "PR_SPEC_DISABLE_NOEXEC" filtered_strace.log | grep -q ') = 0' ||
     grep "0x10" filtered_strace.log | grep -q ') = 0' ; then
    echo "DEBUG: PR_SPEC_DISABLE_NOEXEC returned 0"
    echo 'Result(PR_SPEC_DISABLE_NOEXEC): PASS!'
    DISABLE_NOEXEC=$(( $DISABLE_NOEXEC + 1 ))
  else
    echo "DEBUG: PR_SPEC_DISABLE_NOEXEC returned non-zero"
  fi
  set +x
fi


if [ $DISABLE_NOEXEC -eq 1 ]; then
  echo "DEBUG: Checking for PR_SPEC_DISABLE..."
  echo
  grep -v 'PR_SPEC_DISABLE_NOEXEC' filtered_strace.log | grep -v '0x10' > spec_disable_only.log
  echo "---------------- DEBUG: START (PR_SPEC_DISABLE calls) ------------"
  cat spec_disable_only.log
  echo "----------------- DEBUG: END (PR_SPEC_DISABLE calls) ------------"
  echo
  if grep -q "PR_SPEC_DISABLE" spec_disable_only.log || grep -q "0x4" spec_disable_only.log ; then
    echo "DEBUG: PR_SPEC_DISABLE call present as expected."
    echo 'Result(PR_SPEC_DISABLE): PASS!'
  else
    echo "FAILED: Missing expected PR_SPEC_DISABLE call in strace output" 1>&2
    exit 1
  fi
elif [ $DISABLE_NOEXEC -eq 0 ]; then
  echo "FAILED: Missing expected PR_SPEC_DISABLE_NOEXEC call in strace output" 1>&2
  exit 1
fi

exit 0
