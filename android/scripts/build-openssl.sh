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

SCRIPT_DIR=`realpath .`/scripts
OPENSSL_VERSION=1.1.1i

echo $SCRIPT_DIR/scripts
ARCH=$1
# Set directory
export ANDROID_NDK_HOME=$2
EXTERNAL=$3

OPENSSL_DIR=$EXTERNAL/openssl-$ARCH
cd $EXTERNAL

if [ ! -d $OPENSSL_DIR ]; then
	echo "openssl not found"
	if [ ! -f openssl-$OPENSSL_VERSION.tar.gz ]; then
		wget https://www.openssl.org/source/openssl-$OPENSSL_VERSION.tar.gz
    fi
    #mkdir $OPENSSL_DIR
    tar -zxvf openssl-$OPENSSL_VERSION.tar.gz
    mv openssl-$OPENSSL_VERSION $OPENSSL_DIR
fi

# Find the toolchain for your build machine
toolchains_path=$(python $SCRIPT_DIR/toolchains_path.py --ndk ${NDK})

# Configure the OpenSSL environment, refer to NOTES.ANDROID in OPENSSL_DIR
# Set compiler clang, instead of gcc by default
CC=clang

# Add toolchains bin directory to PATH
PATH=$toolchains_path/bin:$PATH

# Set the Android API levels
ANDROID_API=29

# Set the target architecture
# Can be android-arm, android-arm64, android-x86, android-x86 etc
architecture=$ARCH

# Create the make file
cd ${OPENSSL_DIR}
export ANDROID_NDK_HOME=$toolchains_path

if [ "${ANDROID_ARCH}" = "arm" ]; then
  if [ ! -f "$toolchains_path/bin/arm-linux-android-clang" ]; then
    ln -s $toolchains_path/bin/armv7a-linux-androideabi${ANDROID_COMPILE_SDK}-clang $toolchains_path/bin/arm-linux-androideabi-clang
  fi
elif [ "${ANDROID_ARCH}" = "x86" ]; then
  if [ ! -f "$toolchains_path/bin/i686-linux-android-clang" ]; then
    ln -s $toolchains_path/bin/i686-linux-android${ANDROID_COMPILE_SDK}-clang $toolchains_path/bin/i686-linux-android-clang
  fi
elif [ "${ANDROID_ARCH}" = "x86_64" ]; then
  if [ ! -f "$toolchains_path/bin/x86_64-linux-android-clang" ]; then
    ln -s $toolchains_path/bin/x86_64-linux-android${ANDROID_COMPILE_SDK}-clang $toolchains_path/bin/x86_64-linux-android-clang
  fi
else
  if [ ! -f "$toolchains_path/bin/aarch64-linux-android-clang" ]; then
      ln -s $toolchains_path/bin/aarch64-linux-android${ANDROID_COMPILE_SDK}-clang $toolchains_path/bin/aarch64-linux-android-clang
  fi
fi

./Configure ${architecture} -D__ANDROID_API__=$ANDROID_API no-shared no-unit-test no-tests

# Build
make

