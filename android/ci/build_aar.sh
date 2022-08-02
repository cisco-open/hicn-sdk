#############################################################################
# Copyright (c) 2019 Cisco and/or its affiliates.
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

#wget https://github.com/icn-team/android-sdk/releases/download/release/hICN_Tools.apk
#AAPT=$(find /sdk -name "aapt" | sort -r | head -1)
#VERSION_CODE=$($AAPT dump badging hICN_Tools.apk | grep versionCode | awk '{print $3}' | sed s/versionCode=//g | sed s/\'//g) 
#VERSION_CODE=530

#VERSION_CODE=$((VERSION_CODE+1))

GITHUB_USER=$2
GITHUB_TOKEN=$3


ln -sf /usr_aarch64 /hicn/
ln -sf /usr_x86_64 /hicn/
ln -s /.versions /hicn/
ln -s /sdk /hicn/
GITHUB_SHA=$4
VERSION_CODE=${GITHUB_SHA}_$1

cd hicn

make android_commonaar VERSION=$VERSION_CODE GITHUB_TOKEN=$GITHUB_TOKEN GITHUB_USER=$GITHUB_USER
cp aar_modules/CommonLibrary/common/build/outputs/aar/*.aar /hicn
make android_publish_commonaar VERSION=$VERSION_CODE GITHUB_TOKEN=$GITHUB_TOKEN GITHUB_USER=$GITHUB_USER

make android_forwarderlibraryaar VERSION=$VERSION_CODE GITHUB_TOKEN=$GITHUB_TOKEN GITHUB_USER=$GITHUB_USER
cp aar_modules/ForwarderLibrary/hicnforwarderLibrary/build/outputs/aar/*.aar /hicn
make android_publish_forwarderlibraryaar VERSION=$VERSION_CODE GITHUB_TOKEN=$GITHUB_TOKEN GITHUB_USER=$GITHUB_USER

make android_facemgrlibraryaar VERSION=$VERSION_CODE GITHUB_TOKEN=$GITHUB_TOKEN GITHUB_USER=$GITHUB_USER
cp aar_modules/FaceMgrLibrary/facemgrLibrary/build/outputs/aar/*.aar /hicn
make android_publish_facemgrlibraryaar VERSION=$VERSION_CODE GITHUB_TOKEN=$GITHUB_TOKEN GITHUB_USER=$GITHUB_USER

rm usr_aarch64
rm usr_x86_64
rm .versions
rm sdk
