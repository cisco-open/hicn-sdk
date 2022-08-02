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
MVN_REPO=""
while getopts ":d:v:u:t:r:" opt; do
  case $opt in
    d) ENABLE_DEBUG="$OPTARG"
    ;;
    v) VERSION_CODE="$OPTARG"
    ;;
    u) GITHUB_USER="$OPTARG"
    ;;
    t) GITHUB_TOKEN="$OPTARG"
    ;;
    r) MVN_REPO="$OPTARG"
    ;;
    \?) echo "Invalid option -$OPTARG" >&2
    ;;
  esac
done

cd aar_modules/FaceMgrLibrary
if [ ! -f local.properties ]; then
	echo sdk.dir=${SDK} > local.properties
fi

if [ "$ENABLE_DEBUG" = "DEBUG" ]; then
    ASSEMBLE="assembleDebug"
else
    ASSEMBLE="assembleRelease"
fi

if [ "$MVN_REPO" = "" ]; then
    ./gradlew $ASSEMBLE -PVERSION=$VERSION_CODE -PGITHUB_USER=$GITHUB_USER -PGITHUB_TOKEN=$GITHUB_TOKEN
else
    ./gradlew $ASSEMBLE -PENABLE_HPROXY=$ENABLE_HPROXY -PVERSION=$VERSION_CODE -PGITHUB_USER=$GITHUB_USER -PGITHUB_TOKEN=$GITHUB_TOKEN -PMVN_REPO=$MVN_REPO
fi

cd ..
