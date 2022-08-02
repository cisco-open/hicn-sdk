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

set -ex
ABI=$1
OS=`echo $OS | tr '[:upper:]' '[:lower:]'`
export BASE_DIR=`pwd`
mkdir -p ${DISTILLERY_INSTALL_DIR}
mkdir -p ${DISTILLERY_INSTALL_DIR}/include

if [ ! -d ${DISTILLERY_INSTALL_DIR}/include/openssl ]; then
	echo "OpenSSL Libs not found!"
	echo "Compile OpenSSL"
	export ANDROID_NDK_ROOT=${BASE_DIR}/sdk/ndk-bundle
    bash ${BASE_DIR}/scripts/build-openssl.sh android-$ABI $ANDROID_NDK_ROOT $BASE_DIR/external
	cp $BASE_DIR/external/openssl-android-$ABI/*.a ${DISTILLERY_INSTALL_DIR}/lib/
	cp -r $BASE_DIR/external/openssl-android-$ABI/include/openssl ${DISTILLERY_INSTALL_DIR}/include/
	rm -rf $BASE_DIR/external/openssl-android-$ABI
    touch ${VERSIONS_FILE}
	${SED} -i "/${ABI}_openssl/d" ${VERSIONS_FILE}
	echo ${ABI}_openssl=1.1.1i >> ${VERSIONS_FILE}
fi