#!/bin/sh
# @test
# @bug 1582253
# @summary Do not generate DSA keys by default in keytool
# @run shell/timeout=100 KeytoolNotGenerateDSA.sh

if  keytool -genkeypair -dname O=o -storepass stpass -keypass kypass -keystore test.jks 2>&1 | grep 'security risk' ; then
	echo "Error: 'security risk' found in program's output !" 1>&2
	exit 1
fi
