#!/bin/bash
JAVA=$1
TIME=`date +%s`
BUGID=$2

if [ "x$BUGID" != "x" ] ; then 
  BUGID="-bug:$BUGID"
fi;

echo Running with $JAVA...

mkdir -p test.${TIME}/jdk/JTwork test.${TIME}/jdk/JTreport
java -jar jtreg.jar -v1 -a -ignore:quiet \
		-w:test.${TIME}/jdk/JTwork -r:test.${TIME}/jdk/JTreport \
		-jdk:$JAVA \
		$BUGID \
		test \
	    | tee test.${TIME}/tests.log
