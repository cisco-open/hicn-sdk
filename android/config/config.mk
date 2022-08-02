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




#DISTILLERY_GITHUB_UPSTREAM_NAME?=ccnxs_upstream
# The name to give this upstream
DISTILLERY_GITHUB_UPSTREAM_NAME?=ccnx_upstream

# DISTILLERY_GITHUB_UPSTREAM_REPO=${DISTILLERY_GITHUB_UPSTREAM_URL}/CCNx_Distillery
# The upstream that we expect on Distillery itself.
DISTILLERY_GITHUB_UPSTREAM_REPO?=${DISTILLERY_GITHUB_UPSTREAM_URL}/CCNx_Distillery

# DISTILLERY_ROOT_DIR=/path/to/root/dir
# This is the root directory of the Distillery distribution. Many other paths depend
# on this. This file assumes that make is being run from the DISTILLERY
# directory. If this is not true, it's convenient to assign the variable at the
# shell.
export DISTILLERY_ROOT_DIR?=$(shell pwd)
DISTILLERY_BUILD_DIR_PREFIX="build"
# This is a variable that can be used to multiplex the build.
# If you set this variable the default output directories will have this
# appended to them
DISTILLERY_BUILD_NAME?=

# This is the directory where things are built.
# Note that if some modules don't support off-tree builds you may have problems
DISTILLERY_BUILD_DIR?=${DISTILLERY_ROOT_DIR}/$(shell if [ "${ANDROID_ARCH}" = "arm" ]; \
then echo ${DISTILLERY_BUILD_DIR_PREFIX}_armv7-a; \
else \
if [ "${ANDROID_ARCH}" = "x86" ]; then \
echo ${DISTILLERY_BUILD_DIR_PREFIX}_x86; \
else \
if [ "${ANDROID_ARCH}" = "x86_64" ]; then \
echo ${DISTILLERY_BUILD_DIR_PREFIX}_x86_64; \
else \
echo ${DISTILLERY_BUILD_DIR_PREFIX}_aarch64; \
fi; \
fi; \
fi;)
# This is the directory where the source is checked out.
DISTILLERY_SOURCE_DIR?=${DISTILLERY_ROOT_DIR}/src

# MAKE_BUILD_FLAGS
# Flags to pass to make when building the projects. This is mostly used for
# parallel builds. Disable by setting it to empty
MAKE_BUILD_FLAGS?=-j8

# DISTILLERY_INSTALL_DIR=/path/to/install/dir
# This is the directory where all the ccn software will be installed. This
# directory will be DELETED if you do a make clobber. Do not treat this the
# same way you would treat a system install directory.
export DISTILLERY_INSTALL_DIR_PREFIX="usr"
DISTILLERY_INSTALL_DIR?=${DISTILLERY_ROOT_DIR}/$(shell if [ "${ANDROID_ARCH}" = "arm" ]; \
then echo ${DISTILLERY_INSTALL_DIR_PREFIX}_armv7-a; \
else \
if [ "${ANDROID_ARCH}" = "x86" ]; then \
echo ${DISTILLERY_INSTALL_DIR_PREFIX}_i686; \
else \
if [ "${ANDROID_ARCH}" = "x86_64" ]; then \
echo ${DISTILLERY_INSTALL_DIR_PREFIX}_x86_64; \
else \
echo ${DISTILLERY_INSTALL_DIR_PREFIX}_aarch64; \
fi; \
fi; \
fi;)


# DISTILLERY_DEPENDENCIES_DIR=/path/to/dependencies/dir
# This is the path to the dependencies directory. It is used as the base for
# the dependencies install directories. (tools and libraries)
# You should normally not edit this variable.
DISTILLERY_DEPENDENCIES_DIR?=${DISTILLERY_INSTALL_DIR}

# DISTILLERY_EXTERN_DIR=/path/to/dependencies/external/install/dir
# This is the directory where the dependencies will be installed. This
# directory is deleted and created as needed by the dependencies system.
# It is used in gravy for includes and linking. This should be for the TARGET
# architecture.
DISTILLERY_EXTERN_DIR?=${DISTILLERY_DEPENDENCIES_DIR}
CCNX_DEPENDENCIES?=${DISTILLERY_EXTERN_DIR}
export CCNX_DEPENDENCIES

# DISTILLERY_TOOLS_DIR=/path/to/dependency/tools/dir
# This directory holds some of the tools needed to build libccnx. It should be
# built for the HOST. The directory might be deleted and rebuilt by the
# dependency system. The directory will be included in the execution PATH as
# Distillery builds all the modules.
DISTILLERY_TOOLS_DIR?=${DISTILLERY_DEPENDENCIES_DIR}/build-tools

# DISTILLERY_XCODE_DIR?=${DISTILLERY_ROOT_DIR}/xcode
# Directory where distillery will create the xcode project files. This is done
# via cmake's build system. Modules that don't use cmake won't have a way to
# create this unless the Makefile provides a way.
DISTILLERY_XCODE_DIR?=${DISTILLERY_ROOT_DIR}/xcode

# CMAKE_MAKE_TEST_ARGS="ARGS=-j16"
# Tell CTest (via CMake) to run parallel tests (16)
# To run only 1 test at a time run with -j1 or set it empty
CMAKE_MAKE_TEST_ARGS?="ARGS=-j16"


# CCNX_HOME
# These variables are used by scripts to know where to find the installed
# CCNX software and libaries.  They are also used by various packaging scripts.
CCNX_HOME=${DISTILLERY_INSTALL_DIR}
export DISTILLERY_ROOT_DIR
export SDK=$(shell if [ -z ${SDK_PATH} ]; then echo ${DISTILLERY_ROOT_DIR}/sdk/; else echo ${SDK_PATH}; fi;)

export CMAKE=$(shell if [ -z ${CMAKE_PATH} ]; then echo ${DISTILLERY_ROOT_DIR}/sdk/cmake/bin/; else echo ${CMAKE_PATH}; fi;)
export OS=$(shell uname | tr '[:upper:]' '[:lower:]')
export ARCH=$(shell uname -m)

CCNX_COMPILE_ENVIRONMENT=-DCMAKE_TOOLCHAIN_FILE=${DISTILLERY_ROOT_DIR}/config/config.android
OPEN_SSL_DIR=-DOPENSSL_ROOT_DIR=${DISTILLERY_INSTALL_DIR}
LIBEVENT_ROOT=${DISTILLERY_INSTALL_DIR}
export ABI=$(shell if [ "${ANDROID_ARCH}" = "arm" ]; \
then echo arm; \
else \
if [ "${ANDROID_ARCH}" = "x86" ]; then \
echo x86; \
else \
if [ "${ANDROID_ARCH}" = "x86_64" ]; then \
echo x86_64; \
else \
echo arm64; \
fi; \
fi; \
fi;)
export ANDROID_ABI=$(shell if [ "${ABI}" = "arm" ]; then \
echo armeabi-v7a; \
else \
if [ "${ANDROID_ARCH}" = "x86" ]; then \
echo x86; \
else \
if [ "${ANDROID_ARCH}" = "x86_64" ]; then \
echo x86_64; \
else \
echo arm64-v8a; \
fi; \
fi; \
fi;)
export DISTILLERY_BUILD_DIR_PREFIX
export QT_HOME=${DISTILLERY_ROOT_DIR}/qt/Qt
export DISTILLERY_BUILD_DIR
export DISTILLERY_INSTALL_DIR
export ANDROID_PLATFORM=android-26
#export ANDROID_COMPILE_SDK=27
#export ANDROID_BUILD_TOOLS=28.0.3
#export ANDROID_SDK_TOOLS_REV=4333796
export ANDROID_NDK_VERSION=21.4.7075529
export ANDROID_COMPILE_SDK=29
export ANDROID_BUILD_TOOLS=29.0.3
export ANDROID_SDK_TOOLS_REV=6858069
export ANDROID_CMAKE_REV=3.6.4111459
export ANDROID_CMAKE_REV_3_18=3.18.1
export VERSIONS_FILE=${DISTILLERY_ROOT_DIR}/.versions
export HICN_COMMIT=master
export BLACKLIST_FILE=$(shell pwd)/config/blacklistfile
export NDK=$(shell if [ -z ${NDK_PATH} ]; then echo ${DISTILLERY_ROOT_DIR}/sdk/ndk/${ANDROID_NDK_VERSION}; else echo ${NDK_PATH}; fi;)
export SED=$(shell if [ "${OS}" = "darwin" ]; then \
echo gsed; \
else \
echo sed; \
fi)
