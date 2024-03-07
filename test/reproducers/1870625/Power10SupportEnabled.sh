#!/bin/bash

set -x

##
## @test Power10SupportEnabled.sh
## @bug 1870625
## @summary test whether power 10 support is enabled
## @run shell Power10SupportEnabled.sh
## @requires jdk.version.major >= 11 & var.msys2.enabled == "false"
##

${TESTJAVA}/bin/java -version -XX:+UseByteReverseInstructions
