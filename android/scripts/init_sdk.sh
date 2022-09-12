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

set -ex

OS=`echo $OS | tr '[:upper:]' '[:lower:]'`
if [ "$OS" = "darwin" ]; then
	OS="mac"
fi

if [ -z ${SDK_PATH} ]  ; then
	mkdir -p sdk
	cd sdk
	if [ ! -d cmdline-tools ]; then
		if [ ! -f commandlinetools-${OS}-${ANDROID_SDK_TOOLS_REV}_latest.zip ]; then
				wget --quiet https://dl.google.com/android/repository/commandlinetools-${OS}-${ANDROID_SDK_TOOLS_REV}_latest.zip
		fi
		unzip -qq commandlinetools-${OS}-${ANDROID_SDK_TOOLS_REV}_latest.zip
	fi
	if [ ! -d build-tools ] || [ ! -d extras ] || [ ! -d licenses ] || [ ! -d patcher ] || [ ! -d platform-tools ] || [ ! -d platforms ]; then
		echo yes | cmdline-tools/bin/sdkmanager --licenses --sdk_root=`pwd` > /dev/null
		echo yes | cmdline-tools/bin/sdkmanager --update --sdk_root=`pwd`

		echo yes | cmdline-tools/bin/sdkmanager --sdk_root=`pwd` 'tools'
		echo yes | cmdline-tools/bin/sdkmanager --sdk_root=`pwd` 'platform-tools'
		echo yes | cmdline-tools/bin/sdkmanager --sdk_root=`pwd` 'build-tools;'$ANDROID_BUILD_TOOLS
		echo yes | cmdline-tools/bin/sdkmanager --sdk_root=`pwd` 'platforms;android-'$ANDROID_COMPILE_SDK

		echo yes | cmdline-tools/bin/sdkmanager --sdk_root=`pwd` 'extras;android;m2repository'
		echo yes | cmdline-tools/bin/sdkmanager --sdk_root=`pwd` 'extras;google;google_play_services'
		echo yes | cmdline-tools/bin/sdkmanager --sdk_root=`pwd` 'extras;google;m2repository'
    fi
	cd ..
fi

if [ -z ${NDK_PATH} ]; then
	mkdir -p sdk
	cd sdk
	if [ ! -d cmdline-tools ]; then
		if [ ! -f sdk-tools-${OS}-${ANDROID_SDK_TOOLS_REV}.zip ]; then
		    wget --quiet https://dl.google.com/android/repository/sdk-tools-${OS}-${ANDROID_SDK_TOOLS_REV}.zip
		fi
		unzip -qq sdk-tools-${OS}-${ANDROID_SDK_TOOLS_REV}.zip
	fi
	if [ ! -d build-tools ] || [ ! -d cmake ] || [ ! -d ndk ] || [ ! -d platform-tools ] || [ ! -d platforms ]; then
		echo yes | cmdline-tools/bin/sdkmanager --sdk_root=`pwd` 'tools'
		echo yes | cmdline-tools/bin/sdkmanager --sdk_root=`pwd` 'platform-tools'
		echo yes | cmdline-tools/bin/sdkmanager --sdk_root=`pwd` 'build-tools;'$ANDROID_BUILD_TOOLS
		echo yes | cmdline-tools/bin/sdkmanager --sdk_root=`pwd` 'cmake;'$ANDROID_CMAKE_REV
		echo yes | cmdline-tools/bin/sdkmanager --sdk_root=`pwd` --channel=3 --channel=1 'cmake;'$ANDROID_CMAKE_REV_3_22
		#echo yes | cmdline-tools/bin/sdkmanager --sdk_root=`pwd` 'ndk-bundle'
		echo yes | cmdline-tools/bin/sdkmanager --sdk_root=`pwd` "ndk;$ANDROID_NDK_VERSION" --channel=3
	fi
	cd ..
fi