#!/bin/bash
# if you are porting tests to jdk9 there is automatic @modules tag generator in reproducers regular:
# hg clone ssh://to-openjdk1.usersys.redhat.com//mirrored/internal/regular
# ( in modules-tag-generator directory )

JAVA=$1
TIME=`date +%s`
BUGID=$2

if [ "x$BUGID" != "x" ] ; then 
  BUGID="-bug:$BUGID"
fi;

echo Running with $JAVA...

envVarArg="-e:CUSTOM_DUMMY_VARIABLE=true"
keys=$(env | grep OTOOL_ | sed "s/=.*//")
for key in $keys; do
  envVarArg="$envVarArg,$key"
done

mkdir -p test.${TIME}/jdk/JTwork test.${TIME}/jdk/JTreport
java -jar jtreg/lib/jtreg.jar -v1 -a -ignore:quiet \
		-w:test.${TIME}/jdk/JTwork -r:test.${TIME}/jdk/JTreport \
		-jdk:$JAVA \
		$BUGID \
		$envVarArg \
		test \
	    | tee test.${TIME}/tests.log
