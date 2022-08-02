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
VERSION_CODE=$2
GITHUB_USER=$3
GITHUB_TOKEN=$4

ln -sf /usr_aarch64 /hicn/
ln -sf /usr_x86_64 /hicn/
ln -sf /.versions /hicn/
ln -sf /sdk /hicn/
ln -sf /qt /hicn/

cd /hicn
make version

make android_hicnforwarder VERSION=$VERSION_CODE GITHUB_USER=$GITHUB_USER GITHUB_TOKEN=$GITHUB_TOKEN
cp HicnForwarderAndroid/app/build/outputs/apk/release/*.apk /hicn

make android_hicntools VERSION=$VERSION_CODE
cp hICNTools/app/build/outputs/apk/release/*.apk /hicn

pwd
cd /hicn
mkdir -p /hicn/src
if [ ! -d /hicn/src/viper ]; then
	git clone -b viper/master https://gerrit.fd.io/r/cicn /hicn/src/viper
fi
make android_viper VERSION=$VERSION_CODE
cp /hicn/build_aarch64/viper/hicn-viper-arm64_v8a//build/outputs/apk/hicn-viper-arm64_v8a-release-signed.apk /hicn/viper-arm64.apk
cp /hicn/build_x86_64/viper/hicn-viper-x86_64//build/outputs/apk/hicn-viper-x86_64-release-signed.apk /hicn/viper-x86_64.apk


if [ "$1" = "1" ]; then
  APK_PATH=/hicn/HicnForwarderAndroid.apk
  bash /hicn/ci/push_playstore.sh /hicn/playstore_key.json $APK_PATH $VERSION_CODE /sdk
  APK_PATH=/hicn/hICN_Tools.apk
  bash /hicn/ci/push_playstore.sh /hicn/playstore_key.json $APK_PATH $VERSION_CODE /sdk
  APK_PATH=/hicn/viper-arm64.apk
  bash /hicn/ci/push_playstore.sh /hicn/playstore_key.json $APK_PATH $VERSION_CODE /sdk
fi
