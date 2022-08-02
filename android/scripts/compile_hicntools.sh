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
cd hICNTools
if [ ! -f local.properties ]; then
	echo sdk.dir=${SDK} > local.properties
fi

VERSION_CODE="${2:-1}"
if [ "$1" = "DEBUG" ]; then
	./gradlew assembleDebug -PVERSION_CODE=$VERSION_CODE
else
	./gradlew assembleRelease -PVERSION_CODE=$VERSION_CODE
fi

echo "Apks are inside hICNTools/app/build/outputs/apk"
cd ..