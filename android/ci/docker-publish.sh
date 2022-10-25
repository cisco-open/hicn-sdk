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
set -x
set -o pipefail

echo "**********************************************************"
echo "******************* Compilation  *************************"
echo "***********************************************************"

ANDROID_SDK_DEP=android-sdk-dep
ANDROID_SDK_PUBLISH=android-sdk-build
HICN_AAR_PUBLISH=android-sdk-publish

docker build \
  -t ${HICN_AAR_PUBLISH} \
  --target ${HICN_AAR_PUBLISH} \
  --build-arg GITHUB_USERNAME \
  --build-arg GITHUB_PASSWORD \
  --build-arg GITHUB_MVN_REPO \
  -f android/ci/Dockerfile \
  android
