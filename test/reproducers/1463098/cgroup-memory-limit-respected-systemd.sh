#!/bin/sh
# @test
# @requires jdk.version.major >= 7
# @bug 1463098
# @summary cgroup memory limit not respected when run outside container
# @run shell/timeout=120 cgroup-memory-limit-respected-systemd.sh

set -eux

if ! type systemctl &> /dev/null ; then
    # OS does not have systemd, skip (rhel-6)
    exit 0
fi

if ! sudo -n true ; then
    printf '%s\n' "Sudo not configured for testing user!" 1>&2
    exit 1
fi

ojdkSliceName="ojdk-cg.slice"
ojdkSliceFile="/etc/systemd/system/${ojdkSliceName}"
memoryLimit="30"

onExit() {
    if [ -e "${ojdkSliceFile}" ] ; then
        sudo -n systemctl stop "${ojdkSliceName}" || true
        sudo -n rm -rf "${ojdkSliceFile}"
        sudo -n systemctl daemon-reload
    fi
}

checkMaxHeapSize() {
    filename="$1"
    cgroup="$2"

    if ! [ -f "${filename}" ] ; then
        printf '%s\n' "File does not exist: ${filename}" 1>&2
        return 1
    fi

    pattern="^.*MaxHeapSize[[:space:]]*:*=[[:space:]]*\\([0-9]\\+\\).*\$"
    maxHeapSizeLine="$( cat "${filename}" | grep "${pattern}" | head -n 1 )"
    if [ -z "${maxHeapSizeLine}" ] ; then
        printf '%s\n' "Failed to extract maxHeapSize from File: ${filename}" 1>&2
    fi
    maxHeapSize="$( printf '%s\n' "${maxHeapSizeLine}" | sed "s/${pattern}/\\1/g"  )"

    if [ "${cgroup}" -eq 1 ] ; then
        # heap should be limited by cgroup
        if [ "${maxHeapSize}" -gt "$(( ${memoryLimit} * 1024 * 1024 ))" ] ; then
            printf '%s\n' "maxHeapSize > ${memoryLimit}M" 1>&2
            return 1
        fi
    else
        # heap should not be limited by cgroup
        if [ "${maxHeapSize}" -le "$(( ${memoryLimit} * 1024 * 1024 ))" ] ; then
            printf '%s\n' "maxHeapSize <= ${memoryLimit}M" 1>&2
            return 1
        fi
    fi
    return 0
}

trap onExit EXIT

sudo -n tee "${ojdkSliceFile}" << EOF
[Unit]
Description=Demo cgroup
Before=slices.target

[Slice]
MemoryAccounting=true
MemoryLimit=${memoryLimit}M
EOF

sudo -n systemctl daemon-reload
sudo -n systemctl restart "${ojdkSliceName}"
# cgroup
sudo -n systemd-run --slice "${ojdkSliceName}" --scope java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:+PrintFlagsFinal -version 2>&1 | tee java-cgroup-options.log || :
sudo -n systemd-run --slice "${ojdkSliceName}" --scope java -XX:+PrintFlagsFinal -version 2>&1 | tee java-cgroup-no-options.log || :
# non-cgroup
sudo -n java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:+PrintFlagsFinal -version 2>&1 | tee java-direct-options.log || :
sudo -n java -XX:+PrintFlagsFinal -version 2>&1 | tee java-direct-no-options.log || :

checkMaxHeapSize java-cgroup-options.log 1 || exit 1
checkMaxHeapSize java-cgroup-no-options.log 1 || exit 1
checkMaxHeapSize java-direct-options.log 0 || exit 1
checkMaxHeapSize java-direct-no-options.log 0 || exit 1
