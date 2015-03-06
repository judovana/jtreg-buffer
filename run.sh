#!/bin/bash
JAVA=$1
TIME=`date +%s`

echo Running with $JAVA...

mkdir -p test.${TIME}/jdk/JTwork test.${TIME}/jdk/JTreport
java -jar jtreg.jar -v1 -a -ignore:quiet \
		-w:test.${TIME}/jdk/JTwork -r:test.${TIME}/jdk/JTreport \
		-jdk:$JAVA \
		test \
	    | tee test.${TIME}/tests.log
