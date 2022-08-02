#############################################################################
# Copyright (c) 2022 Cisco and/or its affiliates.
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
ENABLE_DEBUG="NODEBUG"
VERSION_CODE=1
GITHUB_USER=""
GITHUB_TOKEN=""
ENABLE_HPROXY="0"
MVN_REPO_HPROXY="https://maven.pkg.github.com/Cisco-Hybrid-ICN/hproxy"
MVN_REPO="https://maven.pkg.github.com/icn-team/android-sdk"
while getopts ":d:v:p:u:t:r:h:" opt; do
  case $opt in
    d) ENABLE_DEBUG="$OPTARG"
    ;;
    v) VERSION_CODE="$OPTARG"
    ;;
    p) if [ "$OPTARG" != "" ]; then
        ENABLE_HPROXY="$OPTARG"
       fi
    ;;
    u) GITHUB_USER="$OPTARG"
    ;;
    t) GITHUB_TOKEN="$OPTARG"
    ;;
    r) if [ "$OPTARG" != "" ]; then
        MVN_REPO="$OPTARG"
       fi
    ;;
    h) if [ "$OPTARG" != "" ]; then
        MVN_REPO_HPROXY="$OPTARG"
       fi
    ;;
    \?) echo "Invalid option -$OPTARG" >&2
    ;;
  esac
done

if [ "$ENABLE_HPROXY" = "1" ]; then
    if [ "$GITHUB_USER" = "" ] || [ "$GITUB_TOKEN" = "" ]; then
        echo "set github user and token"
        exit 1
    fi
fi

cd HicnForwarderAndroid
if [ ! -f local.properties ]; then
	echo sdk.dir=${SDK} > local.properties
fi

if [ "$ENABLE_DEBUG" = "DEBUG" ]; then
    ASSEMBLE="assembleDebug"
else
    ASSEMBLE="assembleRelease"
fi

./gradlew $ASSEMBLE -PENABLE_HPROXY=$ENABLE_HPROXY -PVERSION_CODE=$VERSION_CODE -PGITHUB_USER=$GITHUB_USER -PGITHUB_TOKEN=$GITHUB_TOKEN -PMVN_REPO=$MVN_REPO -PMVN_REPO_HPROXY=$MVN_REPO_HPROXY

echo "Apks are inside HicnForwarderAndroid/app/build/outputs/apk"
cd ..
