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

BASE_DIR=`pwd`
if [ -d src/cframwork ]; then
	cd src/cframework
	git pull
	cd ../..
fi

if [ "$1" = "" ]; then
	HICN_COMMIT_LOCAL=$HICN_COMMIT
else
	HICN_COMMIT_LOCAL=$1
fi
if [ -d src/hicn ]; then
	cd src/hicn
	git pull origin master
	git checkout $HICN_COMMIT_LOCAL
	for hash in $(git log -100 --format="%H")
	do
		if ! grep -q $hash "${BLACKLIST_FILE}"; then
			actual_hash=$(git log -1 --format="%H")
			if [ "${hash}" != "${actual_hash}" ]; then
				git checkout $hash
				if [ -f ${BASE_DIR}/${VERSIONS_FILE} ]; then
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
	cd ../..
fi

if [ -d src/viper ]; then
	cd src/viper
	git pull
	cd ../..
fi

if [ -d src/hproxy ]; then
	cd src/hproxy
	git pull
	DOWNLOADED_ARM64_HPROXY=$(cat version | grep hproxy_aarch64 | awk -F "=" '{print $2;}')
	DOWNLOADED_X86_HPROXY=$(cat version | grep hproxy_i686 | awk -F "=" '{print $2;}')
	if [ -f ./.versions ]; then
		ARM64_HPROXY=$(cat ./.versions | grep arm64_hproxy | awk -F "=" '{print $2;}')
		
		if [ "ARM64_HPROXY" != "DOWNLOADED_ARM64_HPROXY" ]; then
			mkdir -p ./usr_aarch64/include/hicn
			mkdir -p ./usr_aarch64/lib
			cp -rf usr_aarch64/include/hicn/hproxy ./usr_aarch64/include/hicn
			cp -f usr_aarch64/lib/* ./usr_aarch64/lib/
			${SED} -i '/arm64_hproxy/d' ${BASE_DIR}/.versions;
			echo arm64_hproxy=$DOWNLOADED_ARM64_HPROXY >> ${BASE_DIR}/.versions;
		fi
		X86_HPROXY=$(cat ./.versions | grep x86_hproxy | awk -F "=" '{print $2;}')
		if [ "X86_HPROXY" != "DOWNLOADED_X86_HPROXY" ]; then
			mkdir -p ${BASE_DIR}/usr_i686/include/hicn
			mkdir -p ${BASE_DIR}/usr_i686/lib
			cp -rf usr_i686/include/hicn/hproxy ${BASE_DIR}/usr_i686/include/hicn
			cp -f usr_i686/lib/* ${BASE_DIR}/usr_i686/lib/
			${SED} -i '/x86_hproxy/d' ${BASE_DIR}/.versions;
			echo x86_hproxy=$DOWNLOADED_X86_HPROXY >> ${BASE_DIR}/.versions;
		fi
	else
		touch ${BASE_DIR}/.versions;
		mkdir -p ./usr_aarch64/include/hicn
		mkdir -p ./usr_aarch64/lib
		cp -rf usr_aarch64/include/hicn/hproxy ./usr_aarch64/include/hicn
		cp -f usr_aarch64/lib/* ./usr_aarch64/lib/
		${SED} -i '/arm64_hproxy/d' ${BASE_DIR}/.versions;
		echo arm64_hproxy=$DOWNLOADED_ARM64_HPROXY >> ${BASE_DIR}/.versions;

		mkdir -p ${BASE_DIR}/usr_i686/include/hicn
		mkdir -p ${BASE_DIR}/usr_i686/lib
		cp -rf usr_i686/include/hicn/hproxy ${BASE_DIR}/usr_i686/include/hicn
		cp -f usr_i686/lib/* ${BASE_DIR}/usr_i686/lib/
		${SED} -i '/x86_hproxy/d' ${BASE_DIR}/.versions;
		echo x86_hproxy=$DOWNLOADED_X86_HPROXY >> ${BASE_DIR}/.versions;
	fi
	cd ../..
fi
