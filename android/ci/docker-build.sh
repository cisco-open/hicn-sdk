#!/bin/bash

set -e
set -x
set -o pipefail

echo "**********************************************************"
echo "******************* Compilation  *************************"
echo "***********************************************************"

ANDRODI_SDK_DEP=android-sdk-dep
ANDRODI_SDK_BUILD=android-sdk-build

docker build   --progress=plain \
  --target ${ANDRODI_SDK_DEP} \
  -t ${ANDRODI_SDK_DEP} \
  -f ci/Dockerfile \
  .

docker build 
  --progress=plain\
  --target ${ANDRODI_SDK_BUILD} \
  -t ${ANDRODI_SDK_BUILD} \
  -f ci/Dockerfile \
  .
