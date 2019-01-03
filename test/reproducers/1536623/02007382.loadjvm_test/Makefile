#ORACLEJDK_JAVA_HOME_I386=/opt/java/jdk1.8.0_x86
#ORACLEJDK_JAVA_HOME_AMD64=/opt/java/jdk1.8.0

ORACLEJDK_JAVA_HOME_I386=/gns/mw/java/32bit/oracle/hotspot/jdk-1.8.0_60

JAVA_HOME=

prepare:
	echo "Installing OpenJDK 8"
	sudo apt-get install -y openjdk-8-jdk
	sudo apt-get install -y openjdk-8-jdk:i386
	echo ""
	echo "Install Oracle JDK 8 (amd64) manually into /opt/java/jdk1.8.0"
	echo "Install Oracle JDK 8 (i386 ) manually into /opt/java/jdk1.8.0_x86"

compile_java:
	javac foo.java

compile_oraclejdk: compile_java
	# ORACLE JDK
	gcc -m32 -o test_oraclejdk_i386_case1 test_case1.c -I$(ORACLEJDK_JAVA_HOME_I386)/include -I$(ORACLEJDK_JAVA_HOME_I386)/include/linux -L$(ORACLEJDK_JAVA_HOME_I386)/jre/lib/i386/server -ljvm
	gcc -m32 -o test_oraclejdk_i386_case2 test_case2.c -I$(ORACLEJDK_JAVA_HOME_I386)/include -I$(ORACLEJDK_JAVA_HOME_I386)/include/linux -L$(ORACLEJDK_JAVA_HOME_I386)/jre/lib/i386/server -ljvm

compile: compile_oraclejdk


run_oraclejdk_i386_case1:
	bash -c "{ LD_PRELOAD=$(ORACLEJDK_JAVA_HOME_I386)/jre/lib/i386/server/libjvm.so ./test_oraclejdk_i386_case1; } >&test_oraclejdk_i386_case1.log"

run_oraclejdk_i386_case2:
	bash -c "{ LD_PRELOAD=$(ORACLEJDK_JAVA_HOME_I386)/jre/lib/i386/server/libjvm.so ./test_oraclejdk_i386_case2; } >&test_oraclejdk_i386_case2.log"

run_oraclejdk_i386: run_oraclejdk_i386_case1 run_oraclejdk_i386_case2
run_oraclejdk: run_oraclejdk_i386 

runall: run_oraclejdk

all: compile runall

clean:
	rm -f foo.class
	rm -f test_*case[12]
	rm -f test_*case*.log
