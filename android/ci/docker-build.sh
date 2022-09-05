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

ANDRODI_SDK_DEP=android-sdk-dep
ANDRODI_SDK_BUILD=android-sdk-build

docker build  --no-cache --progress=plain \
  --target ${ANDRODI_SDK_DEP} \
  -t ${ANDRODI_SDK_DEP} \
  -f android/ci/Dockerfile \
  android

docker build \
  --progress=plain \
  --target ${ANDRODI_SDK_BUILD} \
  -t ${ANDRODI_SDK_BUILD} \
  -f android/ci/Dockerfile \
  android
