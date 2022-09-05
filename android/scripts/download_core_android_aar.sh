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
export BASE_DIR=`pwd`

mkdir -p src

if [ ! -z "${CORE_AAR_REPO}" ]; then
	if [ ! -d core-android-aar ]; then
			echo "core-android-aar not found"
			git clone $CORE_AAR_REPO -b $CORE_AAR_VERSION
	fi
else
	echo "core-android-aar repo url is empty, define CORE_AAR_REPO environment variable"
	exit 1
fi

cd core-android-aar
ln -sf ../hproxy-aar/semtun-manager .
ln -sf ../hproxy-aar/hproxyLibrarySrc .
mkdir -p aar
cd aar
ln -sf ../../CommonLibrary/common .
ln -sf ../../hicn-aar/FaceMgrLibrary/facemgrLibrary .
ln -sf ../../hicn-aar/ForwarderLibrary/hicnforwarderLibrary .
