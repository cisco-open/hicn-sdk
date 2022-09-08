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
cd src

if [ ! -d hicn ]; then
	echo "libhicn not found"
	git clone --recurse-submodules $HICN_REPO -b $HICN_VERSION
	cd hicn
	for hash in $(git log -100 --format="%H")
	do
		if ! grep -q $hash "${BLACKLIST_FILE}"; then
  			actual_hash=$(git log -1 --format="%H")
  			if [ "${hash}" != "${actual_hash}" ]; then
  				git checkout $hash
  				if [ -f ${VERSIONS_FILE} ]; then
  					installed_version_arm64=$(cat ${VERSIONS_FILE} | grep "arm64_hicn" | awk -F "=" '{print $2;}')
  					if [ "$installed_version_arm64" != "$hash" ]; then
  						rm -rf ${DISTILLERY_INSTALL_DIR_PREFIX}_arm64/lib/libhicn*
						rm -rf ${DISTILLERY_INSTALL_DIR_PREFIX}_arm64/lib/libfacemgr.*
						rm -rf ${DISTILLERY_INSTALL_DIR_PREFIX}_arm64/include/hicn
  					fi
  					installed_version_x86=$(cat ${VERSIONS_FILE} | grep "x86_hicn" | awk -F "=" '{print $2;}')
  					if [ "$installed_version_armx86" != "$hash" ]; then
  						rm -rf ${DISTILLERY_INSTALL_DIR_PREFIX}_x86/lib/libhicn*
						rm -rf ${DISTILLERY_INSTALL_DIR_PREFIX}_x86/lib/libfacemgr.*
						rm -rf ${DISTILLERY_INSTALL_DIR_PREFIX}_x86/include/hicn
  					fi
  				fi
  			fi
  			break
		fi
	done
	cd ..
fi

cd ..