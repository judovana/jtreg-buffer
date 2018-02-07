#!/bin/sh

set -eu

testsRepoDir="${1}"
jdk="${2}"
runPrefix="${3}"
jtrFile="${4}"

# only process files of failed tests
if cat "$jtrFile" | grep -q "test result: Failed" ; then
	testFile="$( cat "${jtrFile}" | grep '^\$file=' | sed 's/^\$file=//g' )"
	sourceFile="$( cat "${jtrFile}" | grep '^source=' )"

	testFileRelative="${testFile#${runPrefix}}"
	testFileRelative="${testFileRelative#/}"
	testFileLocal="${testsRepoDir%/}/${testFileRelative}"

	# make sure we were able to locate test's source file
	if [ -e "${testFileLocal}" ] ; then
		# find modules which need to be made accessible
		requiredModules="$(
			# extract package name (and possible class name) from errors
			cat "${jtrFile}" | sed -n -e "
				/^.*error: package \\([^[:space:]]*\\) is not visible\$/ {
					s/^.*error: package \\([^[:space:]]*\\) is not visible\$/\\1/g
					P
				}

				/^.*error: package \\([^[:space:]]*\\) does not exist\$/ {
					s/^.*error: package \\([^[:space:]]*\\) does not exist\$/\\1/g
					P
				}

				/^.*error: cannot find symbol\$/ {
					n
					/^.*\$/ {
						n
						/^[[:space:]]*\\^[[:space:]]*\$/ {
							n
							/^[[:space:]]*symbol:[[:space:]]*class[[:space:]]\\([^[:space:]]*\\)[[:space:]]*\$/ {
								N
								/^[[:space:]]*symbol:[[:space:]]*class[[:space:]]*\\([^[:space:]]*\\)[[:space:]]*\\n[[:space:]]*location:[[:space:]]*package[[:space:]]*\\([^[:space:]]*\\)\$/ {
									s/^[[:space:]]*symbol:[[:space:]]*class[[:space:]]*\\([^[:space:]]*\\)[[:space:]]*\\n[[:space:]]*location:[[:space:]]*package[[:space:]]*\\([^[:space:]]*\\)\$/\\2 \\1/g
									P
								}
							}
						}
					}
				}

				/^.*java.lang.ClassNotFoundException: \\([^[:space:]]*\\)\\.\\([^[:space:].]*\\)\$/ {
					s/^.*java.lang.ClassNotFoundException: \\([^[:space:]]*\\)\\.\\([^[:space:].]*\\)\$/\\1 \\2/g
					P
				}" \
			| while read -r line ; do
				if printf "%s" "${line}" | grep -q " " ; then
					# we also have class name
					package="${line%% *}"
					className="${line#* }"
					# we do not wont inner class
					className="${className%%\$*}"
				else
					# we do not have classname
					package="${line}"
					className=""
				fi
				# find package's module in jdk sources :)
				packagePath="$( printf "%s" "${package}" | sed 's;\.;/;g' )"
				for jdkprojectDir in "${jdk}/"* ; do
					for moduleDir in "${jdkprojectDir}/src/"* ; do
						for osDir in "${moduleDir}/"* ; do
							fullPackagePath="${osDir}/classes/${packagePath}"
							if [ -d "${fullPackagePath}" ] ; then
								if [ -n "${className}" ] ; then
									if [ -e "${fullPackagePath}/${className}.java" ] ; then
										printf '%s/%s\n' "${moduleDir##*/}" "${package}"
									fi
								else
									# consider module/package only if it contains some sources
									# ( because when we search for a.b package for example,
									#   some module may contain a.b.c package (but not a.b
									#   package), so we don't want that module to be useded )
									if ls "${fullPackagePath}" | grep -q '.*\.java' ; then
										printf '%s/%s\n' "${moduleDir##*/}" "${package}"
									fi
								fi
							fi
						done
					done
				done
			# sort and remove duplicities
			done | sort -u
		)"
		# if some modules were found
		if [ -n "${requiredModules}" ] ; then
			# only process files, which do not have @modules tag yet
			if ! cat "${testFileLocal}" | grep -q "^[[:space:]]*[/]*\\**[[:space:]]*@modules" ; then
				# generate module tag (with modules)
				moduleJtregTag="$(
					tag="@modules"
					printf '%s\n' "${requiredModules}" \
					| while read -r moduleLine ; do
						printf '%s' "\\n * ${tag} ${moduleLine}"
						tag="        "
					done
				)"
				# add tage after @test tag
				testTagPattern="^[[:space:]]*[/]*\\*\\+[[:space:]]*@test[[:space:]]*.*\$"
				if cat "${testFileLocal}" | grep -q "${testTagPattern}" ; then
					sed -i -e "s|${testTagPattern}|&${moduleJtregTag}|" "${testFileLocal}"
					printf "INFO: automatically modified file: %s\\n" "${testFileLocal}"
				else
					# test was not able to find expected patern with @test tag
					# manual fix is needed
					# so at least print test file and required modules
					printf "INFO: manual fix needed:\\nfile: %s\\n%s\\n\\n" "${testFileLocal}" "${requiredModules}"
				fi
			else
				# test already contains @modules tag
				# manual fix is needed
				# so at least print test file and required modules
				printf "INFO: manual fix needed:\\nfile: %s\\n%s\\n\\n" "${testFileLocal}" "${requiredModules}"
			fi
		fi
	else
		printf "WARN: file not found at expected location (test source): %s\\n" "${testFileLocal}" 1>&2
	fi
fi
