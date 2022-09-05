 #############################################################################
 # Copyright (c) 2017 Cisco and/or its affiliates.
 # Licensed under the Apache License, Version 2.0 (the "License");
 # you may not use this file except in compliance with the License.
 # You may obtain a copy of the License at:
 #
 #     http://www.apache.org/licenses/LICENSE-2.0
 #
 # Unless required by applicable law or agreed to in writing, software
 # distributed under the License is distributed on an "AS IS" BASIS,
 # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 # See the License for the specific language governing permissions and
 # limitations under the License.
 ##############################################################################

#!/bin/bash

set -e

getVersionFromVersionsFile () {
    local component=$1
    local abi=$2
    local result=$3
    local  hash=$(cat ${VERSIONS_FILE} | grep "${abi}_${component}" | awk -F "=" '{print $2;}')
    if [ "${hash}" = "" ]; then
    	eval $result="'not installed'"
    else
		eval $result="'${hash}'"
    fi
}

getVersionFromSdkManager () {
	local component=$1
	local result=$2
	local version=$(cat ${TEMP_FILE} | grep "$component" | awk -F "|" '{ print $2; }' | xargs)
	if [ "${version}" = "" ]; then
    	eval $result="'not installed'"
    else
		eval $result="'$version'"
    fi
}

getVersionFromQt () {
	local abi=$1
	local result=$2
	local version=$(cat qt/Qt/components.xml | grep ApplicationName | awk '{print $2}' | awk -F "<" '{print $1}')
	if [ "${version}" = "" ]; then
    	eval $result="'not installed'"
    else
		eval $result="'$version'"
    fi
}


HICN_ANDROID_SDK_VERION=$(git log -1 --format="%H")

TEMP_FILE=.sdkmanageroutput
./sdk/tools/bin/sdkmanager --list 2>&1 | sed -e '/Available Packages/q' > ${TEMP_FILE}

if [ -f "sdk/tools/bin/sdkmanager" ]; then
	getVersionFromSdkManager "build-tools" SDK_VERSION
	getVersionFromSdkManager "ndk-bundle" NDK_VERSION
	getVersionFromSdkManager "cmake;3.6" CMAKE_3_6
	getVersionFromSdkManager "cmake;3.10" CMAKE_3_10

else
	SDK_VERSION="not installed"
	NDK_VERSION="not installed"
	CMAKE_3_6="not installed"
	CMAKE_3_10="not installed"
fi

rm ${TEMP_FILE}

if [ -f "qt/Qt_arm64/components.xml" ]; then
	getVersionFromQt arm64 QT_VERSION_ARM64
else
	QT_VERSION_ARM64="not installed"
fi

if [ -f "qt/Qt_x86_64/components.xml" ]; then
	getVersionFromQt x86_64 QT_VERSION_X86_64
else
	QT_VERSION_X86_64="not installed"
fi

if [ -f "${VERSIONS_FILE}" ]; then

	getVersionFromVersionsFile "QtAV" "arm64" QTAV_VERSION_ARM64
	getVersionFromVersionsFile "QtAV" "x86_64" QTAV_VERSION_X86_64

	getVersionFromVersionsFile "ffmpeg" "arm64" FFMPEG_VERSION_ARM64
	getVersionFromVersionsFile "ffmpeg" "x86_64" FFMPEG_VERSION_X86_64

	getVersionFromVersionsFile "openssl" "arm64" OPENSSL_VERSION_ARM64
	getVersionFromVersionsFile "openssl" "x86_64" OPENSSL_VERSION_X86_64

	getVersionFromVersionsFile "libconfig" "arm64" LIBCONFIG_VERSION_ARM64
	getVersionFromVersionsFile "libconfig" "x86_64" LIBCONFIG_VERSION_X86_64

	getVersionFromVersionsFile "libevent" "arm64" LIBEVENT_VERSION_ARM64
	getVersionFromVersionsFile "libevent" "x86_64" LIBEVENT_VERSION_X86_64

	getVersionFromVersionsFile "libxml" "arm64" LIBXML_VERSION_ARM64
	getVersionFromVersionsFile "libxml" "x86_64" LIBXML_VERSION_X86_64

	getVersionFromVersionsFile "curl" "arm64" CURL_VERSION_ARM64
	getVersionFromVersionsFile "curl" "x86_64" CURL_VERSION_X86_64

	getVersionFromVersionsFile "libparc" "arm64" LIBPARC_VERSION_ARM64
	getVersionFromVersionsFile "libparc" "x86_64" LIBPARC_VERSION_X86_64

	getVersionFromVersionsFile "hicn" "arm64" HICN_VERSION_ARM64
	getVersionFromVersionsFile "hicn" "x86_64" HICN_VERSION_X86_64

	getVersionFromVersionsFile "libdash" "arm64" LIBDASH_VERSION_ARM64
	getVersionFromVersionsFile "libdash" "x86_64" LIBDASH_VERSION_X86_64
else
	QTAV_VERSION_ARM64="not installed"
	QTAV_VERSION_X86_64="not installed"

	FFMPEG_VERSION_ARM64="not installed"
	FFMPEG_VERSION_X86_64="not installed"

	OPENSSL_VERSION_ARM64="not installed"
	OPENSSL_VERSION_X86_64="not installed"

	LIBCONFIG_VERSION_ARM64="not installed"
	LIBCONFIG_VERSION_X86_64="not installed"

	LIBEVENT_VERSION_ARM64="not installed"
	LIBEVENT_VERSION_X86_64="not installed"

	LIBXML_VERSION_ARM64="not installed"
	LIBXML_VERSION_X86_64="not installed"

	CURL_VERSION_ARM64="not installed"
	CURL_VERSION_X86_64="not installed"

	LIBPARC_VERSION_ARM64="not installed"
	LIBPARC_VERSION_X86_64="not installed"

	HICN_VERSION_ARM64="not installed"
	HICN_VERSION_X86_64="not installed"

	LIBDASH_VERSION_ARM64="not installed"
	LIBDASH_VERSION_X86_64="not installed"
fi
echo "\n"
echo "#############################################################################"
echo "HICN ANDROID-SDK"
echo "andoroid-sdk version = ${HICN_ANDROID_SDK_VERION}"
echo "#############################################################################"
echo "ANDROID ENVIRONMENT"
echo "SDK version = ${SDK_VERSION}"
echo "NDK version = ${NDK_VERSION}"
echo "cmake 3.6 = ${CMAKE_3_6}"
echo "cmake 3.10 = ${CMAKE_3_10}"
echo "#############################################################################"
echo "QT ENVIRONMENT"
echo "arm64 version = ${QT_VERSION_ARM64}"
echo "x86_64 version = ${QT_VERSION_X86_64}"
echo "#############################################################################"
echo "LIBRARIES INSTALLED"
echo "openssl arm64 version = ${OPENSSL_VERSION_ARM64}" 
echo "openssl x86_64 version = ${OPENSSL_VERSION_X86_64}"
echo "libconfig arm64 version = ${LIBCONFIG_VERSION_ARM64}"
echo "libconfig x86_64 version = ${LIBCONFIG_VERSION_X86_64}"
echo "libevent arm64 version = ${LIBEVENT_VERSION_ARM64}"
echo "libevent x86_64 version = ${LIBEVENT_VERSION_X86_64}"
echo "libxml arm64 version = ${LIBXML_VERSION_ARM64}"
echo "libxml x86_64 version = ${LIBXML_VERSION_X86_64}"
echo "curl arm64 version = ${CURL_VERSION_ARM64}"
echo "curl x86_64 version = ${CURL_VERSION_X86_64}"
echo "libparc arm64 version = ${LIBPARC_VERSION_ARM64}"
echo "libparc x86_64 version = ${LIBPARC_VERSION_X86_64}"
echo "hicn arm64 version = ${HICN_VERSION_ARM64}"
echo "hicn x86_64 version = ${HICN_VERSION_X86_64}"
echo "libdash arm64 version = ${LIBDASH_VERSION_ARM64}"
echo "libdash x86_64 version = ${LIBDASH_VERSION_X86_64}"
echo "QtAV arm64 version = ${QTAV_VERSION_ARM64}"
echo "QtAV x86_64 version = ${QTAV_VERSION_X86_64}"
echo "ffmpeg arm64 version = ${FFMPEG_VERSION_ARM64}"
echo "ffmpeg x86_64 version = ${FFMPEG_VERSION_X86_64}"
echo "#############################################################################"
