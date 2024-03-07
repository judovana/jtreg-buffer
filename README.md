# jtreg-buffer
Repo for jtreg tests which belongs to jdk/tests, but they were in rush, or not eligible, and later forgotten.

Some tests are fips-incomaptible, but the detection is now weak. you can pretend fips mode by setting OTOOL_cryptosetup=fips. This may (and should) change in future.

Some of the tests are noot applicable to all systems/jdks. This is controled by standard, and extended @requires. The extended one is simple, environment varibales one:
 * RH_JDK=true wills set var.rh.jdk to true, which **enables** aprox 6 more tests which are known to work only on Red Hat jdks
 * MSYS2_ENABLED=true wills set var.msys2.enabled to trueand that will **disable** aprox 13 tests, which are known to fail on windows subsystem for linux

Note, that thsoe is workaround, and on long run all such exceses hshould be removed
