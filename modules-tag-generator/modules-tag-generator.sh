#!/bin/sh

# This tool can be used to generate @modules jtreg tag ( which is required when
# accessing private apis on JDK9. It requires results of failed run on JDK9
# (with .jtr files) as input. It also requires JDK9 forest, where it searches
# for modules.
# It automatically modifies source files of tests as required (adds @modules tag
# where necessary) and prints info about modified files.
# When this tool cannot make modifications by itself it at least prints info
# about modules, which are required by test. ( Script is carefull, it only
# modifies java jtregs (not shell ones), and only tests, where @modules tag is
# not already present. )

set -eu

if ! [ "$#" -eq 4 ] ; then
	cat 1>&2 <<- EOF
		./modules-tag-generator.sh <testsResultsDir> <testsRepoDir> <jdk9ForestDir> <runPrefix>
		
		Parameters:
		    testsRepoDir - Directory (repo) with tests
		    jdk9ForestDir - Directory with clonned jdk9 forest
		    testsResultsDir - Directory with tests results
		                      ( containing .jtr files in its directory tree )
		    runPrefixDir - Absolute location of directory (repo) with tests,
		                   when tests were ran (may be same as testsRepoDir)
	EOF
	exit 1
fi

testsRepoDir="${1}"
jdk9ForestDir="${2}"
testsResultsDir="${3}"
runPrefixDir="${4}"

if ! [ -d "${testsRepoDir}" ] ; then
	printf "ERROR: testsRepoDir does not exist: %s\\n" "${testsRepoDir}" 1>&2
	exit 1
fi

if ! [ -d "${jdk9ForestDir}" ] ; then
	printf "ERROR: jdk9forestDir does not exist: %s\\n" "${jdk9ForestDir}" 1>&2
	exit 1
fi

if ! [ -d "${testsResultsDir}" ] ; then
	printf "ERROR: testsResultsDir does not exist: %s\\n" "${testsResultsDir}" 1>&2
	exit 1
fi

if ! printf "%s" "${runPrefixDir}" | grep -q "^/" ; then
	printf "ERROR: runPrefix is not absolute path: %s\\n" "${runPrefixDir}" 1>&2
	exit 1
fi


find "${testsResultsDir}" -name "*.jtr" \
-exec "./process-jtr.sh" "${testsRepoDir}" "${jdk9ForestDir}" "${runPrefixDir}" "{}" ";"
