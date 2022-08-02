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
SHELL:=/bin/bash
DISTILLERY_VERSION=2.0

default.target: help

all: init init_sdk download-dep compile-openssl compile-dep download-hicn compile-hicn

all-withqt: all init_qt download-qtdep compile-qtdep

##############################################################
# Variables
#
# Set some variables
DISTILLERY_STAMP=.distillery.stamp
REBUILD_DEPENDS=

##############################################################
# Load the configuration
#
# For more information please see config.default.mk
#
DISTILLERY_CONFIG_DIR ?= config

##DISTILLERY_DEFAULT_CONFIG ?= ${DISTILLERY_CONFIG_DIR}/config.mk
##DISTILLERY_LOCAL_CONFIG   ?= ${DISTILLERY_CONFIG_DIR}/local/config.mk
DISTILLERY_USER_CONFIG    ?= ${DISTILLERY_CONFIG_DIR}/config.mk

ifneq (,$(wildcard ${DISTILLERY_USER_CONFIG}))
    include ${DISTILLERY_USER_CONFIG}
    REBUILD_DEPENDS+=${DISTILLERY_USER_CONFIG}
else
    DISTILLERY_USER_CONFIG+="[Not Found]"
endif

ifneq (,$(wildcard ${DISTILLERY_LOCAL_CONFIG}))
    include ${DISTILLERY_LOCAL_CONFIG}
    REBUILD_DEPENDS+=${DISTILLERY_LOCAL_CONFIG}
endif

include ${DISTILLERY_DEFAULT_CONFIG}


##############################################################
# Set the paths
#
# PATH: add our install dir, build dependencies and system dependencies
# LD_RUN_PATH: add our install dir

export PATH := $(DISTILLERY_INSTALL_DIR)/bin:$(DISTILLERY_TOOLS_DIR)/bin:$(PATH)
#export LD_RUN_PATH := $(DISTILLERY_INSTALL_DIR)/lib
#export LD_LIBRARY_PATH := $(DISTILLERY_INSTALL_DIR)/lib
export CCNX_HOME
export FOUNDATION_HOME


##############################################################
# Modules
#
# Load the modules config. Please refer to that file for more information
DISTILLERY_MODULES_DIR=${DISTILLERY_CONFIG_DIR}/modules

# The modules variable is a list of modules. It will be populated by the
# modules config files.
modules=
modules_dir=
    include config/modules/000-distillery-update.mk
	include config/modules/000-gitModule.mk
	include config/modules/001-modules.mk
	include config/modules/002-cmake-modules.mk
	include config/modules/002-make-modules.mk
	include config/modules/100-distillery.mk
	include config/modules/105-libconfig.mk
	include config/modules/107-libevent.mk
	include config/modules/108-libxml2.mk
	include config/modules/109-curl.mk
	include config/modules/120-libparc.mk
	include config/modules/130-hicn.mk
	include config/modules/610-libdash.mk

ifeq (${BUILD_HPROXY},1)
modules_dir+=
	include config/modules/140-hproxy.mk
endif

# Load user defined modules
DISTILLERY_USER_MODULES_DIR=${DISTILLERY_USER_CONFIG_DIR}/modules
ifneq (,$(wildcard ${DISTILLERY_USER_MODULES_DIR}))
    include ${DISTILLERY_USER_MODULES_DIR}/*.mk
else
    DISTILLERY_USER_MODULES_DIR+="[Not Found]"
endif

ifdef ${DISTILLERY_LOCAL_MODULES_DIR}
    include ${DISTILLERY_LOCAL_MODULES_DIR}/*.mk
else
    DISTILLERY_LOCAL_MODULES_DIR="[Undefined]"
endif

build-qtdep: init_qt download-qtdep compile-qtdep

install-all: install-directories ${modules}

install-env: init_sdk init_qt

init:
	@bash scripts/init.sh ${DISTILLERY_INSTALL_DIR};

init_sdk:
	@bash scripts/init_sdk.sh

init_qt:
	@bash scripts/init_qt.sh

download-dep:
	@bash scripts/download_dep.sh;

compile-openssl: init
	@bash  scripts/compile_openssl.sh ${ABI};

install-asio: init
	@bash scripts/install_asio.sh ${ABI};

compile-dep: init compile-openssl install-asio libconfig libevent

download-hicn:
	@bash scripts/download_hicn.sh;

ifeq (${BUILD_HPROXY},1)
compile-hicn: init download-hicn cframework/libparc hicn hproxy
else
compile-hicn: init download-hicn cframework/libparc hicn
endif

download-qtdep:
	@bash  scripts/download_qtdep.sh;

compile-qtdep: init libxml2 curl viper/libdash compile-ffmpeg install-qtav

compile-ffmpeg:
	@bash scripts/compile_ffmpeg.sh ${ABI};

install-qtav:
	@bash scripts/install_qtav.sh ${ABI};

android_hicnforwarder:
	@bash scripts/compile_hicnforwarder.sh -d NODEBUG -v "$(VERSION)" -p "$(ENABLE_HPROXY)" -u "$(GITHUB_USER)" -t "$(GITHUB_TOKEN)" -r "$(MVN_REPO)" -h "$(MVN_REPO_HPROXY)"

android_hicnforwarder_debug:
	@bash scripts/compile_hicnforwarder.sh -d DEBUG -v "$(VERSION)" -p "$(ENABLE_HPROXY)" -u "$(GITHUB_USER)" -t "$(GITHUB_TOKEN)" -r "$(MVN_REPO)"  -h "$(MVN_REPO_HPROXY)"

ifeq (${BUILD_HPROXY},1)
android_hproxyaar:
	@bash scripts/compile_hproxy_aar.sh NODEBUG $(VERSION)

android_hproxyaar_debug:
	@bash scripts/compile_hproxy_aar.sh DEBUG $(VERSION)
endif

android_commonaar:
	@bash scripts/compile_common_aar.sh -d NODEBUG -v "$(VERSION)" -r "$(MVN_REPO)"

android_commonaar_debug:
	@bash scripts/compile_common_aar.sh -d DEBUG -v "$(VERSION)" -r "$(MVN_REPO)"

android_publish_commonaar:
	@bash scripts/publish_common_aar.sh -v "$(VERSION)" -r "$(MVN_REPO)" -u "$(GITHUB_USER)" -t "$(GITHUB_TOKEN)"

android_forwarderlibraryaar:
	@bash scripts/compile_hicnforwarder_aar.sh -d NODEBUG -v "$(VERSION)" -r "$(MVN_REPO)" -u "$(GITHUB_USER)" -t "$(GITHUB_TOKEN)"

android_forwarderlibraryaar_debug:
	@bash scripts/compile_hicnforwarder_aar.sh -d DEBUG -v "$(VERSION)" -r "$(MVN_REPO)" -u "$(GITHUB_USER)" -t "$(GITHUB_TOKEN)"

android_publish_forwarderlibraryaar:
	@bash scripts/publish_hicnforwarder_aar.sh -v "$(VERSION)" -r "$(MVN_REPO)" -u "$(GITHUB_USER)" -t "$(GITHUB_TOKEN)"

android_facemgrlibraryaar:
	@bash scripts/compile_facemgr_aar.sh -d NODEBUG -v "$(VERSION)" -r "$(MVN_REPO)" -u "$(GITHUB_USER)" -t "$(GITHUB_TOKEN)"

android_facemgrlibraryaar_debug:
	@bash scripts/compile_facemgr_aar.sh -d DEBUG -v "$(VERSION)" -r "$(MVN_REPO)" -u "$(GITHUB_USER)" -t "$(GITHUB_TOKEN)"

android_publish_facemgrlibraryaar:
	@bash scripts/publish_facemgr_aar.sh -v "$(VERSION)" -r "$(MVN_REPO)" -u "$(GITHUB_USER)" -t "$(GITHUB_TOKEN)"

android_hicntools:
	@bash scripts/compile_hicntools.sh NODEBUG $(VERSION)

android_hicntools_debug:
	@bash scripts/compile_hicntools.sh DEBUG $(VERSION)

android_viper:
	@bash scripts/compile_androidviper.sh NODEBUG $(VERSION)

android_viper_debug:
	@bash scripts/compile_androidviper.sh ODEBUG $(VERSION)

curl-clean:
	@rm -rf ${DISTILLERY_INSTALL_DIR_PREFIX}_*/lib/libcurl.*
	@rm -rf ${DISTILLERY_INSTALL_DIR_PREFIX}_*/include/curl
	@rm -rf ${DISTILLERY_BUILD_DIR_PREFIX}_*/curl

openssl-clean:
	@rm -rf ${DISTILLERY_INSTALL_DIR_PREFIX}_*/lib/libssl.*
	@rm -rf ${DISTILLERY_INSTALL_DIR_PREFIX}_*/lib/libcrypto.*
	@rm -rf ${DISTILLERY_INSTALL_DIR_PREFIX}_*/include/openssl
	@rm -rf external/openssl*

event-clean:
	@rm -rf ${DISTILLERY_BUILD_DIR_PREFIX}_*/libevent
	@rm -rf ${DISTILLERY_INSTALL_DIR_PREFIX}_*/lib/libevent*
	@rm -rf ${DISTILLERY_INSTALL_DIR_PREFIX}_*/include/event2
	@rm -rf ${DISTILLERY_INSTALL_DIR_PREFIX}_*/include/ev*.h

xml2-clean:
	@rm -rf ${DISTILLERY_BUILD_DIR_PREFIX}_*/libxml2
	@rm -rf ${DISTILLERY_INSTALL_DIR_PREFIX}_*/lib/libxml2*
	@rm -rf ${DISTILLERY_INSTALL_DIR_PREFIX}_*/include/libxml

libconfig-clean:
	@rm -rf ${DISTILLERY_BUILD_DIR_PREFIX}_*/libconfig
	@rm -rf ${DISTILLERY_INSTALL_DIR_PREFIX}_*/lib/libconfig*
	@rm -rf ${DISTILLERY_INSTALL_DIR_PREFIX}_*/include/libconfig*

asio-clean:
	@rm -rf ${DISTILLERY_INSTALL_DIR_PREFIX}_*/include/asio*

libparc-clean:
	@rm -rf ${DISTILLERY_BUILD_DIR_PREFIX}_*/cframework
	@rm -rf ${DISTILLERY_INSTALL_DIR_PREFIX}_*/lib/libparc.*
	@rm -rf ${DISTILLERY_INSTALL_DIR_PREFIX}_*/include/parc

ffmpeg-clean:
	@rm -rf ${DISTILLERY_INSTALL_DIR_PREFIX}_*/lib/libav.*
	@rm -rf ${DISTILLERY_INSTALL_DIR_PREFIX}_*/include/libav*
	@rm -rf ${DISTILLERY_INSTALL_DIR_PREFIX}_*/include/libsw*
	@rm -rf ${DISTILLERY_BUILD_DIR_PREFIX}_*/ffmpeg
	@rm -rf src/ffmpeg

libdash-clean:
	@rm -rf ${DISTILLERY_BUILD_DIR_PREFIX}_*/viper/libdash
	@rm -rf ${DISTILLERY_INSTALL_DIR_PREFIX}_*/lib/libdash.*
	@rm -rf ${DISTILLERY_INSTALL_DIR_PREFIX}_*/include/libdash

dependencies-clean: event-clean openssl-clean curl-clean xml2-clean libconfig-clean libparc-clean asio-clean ffmpeg-clean libdash-clean

sdk-clean:
	@rm -rf sdk

hicn-clean:
	@rm -rf ${DISTILLERY_BUILD_DIR_PREFIX}_*/hicn
	@rm -rf ${DISTILLERY_INSTALL_DIR_PREFIX}_*/lib/libhicn*
	@rm -rf ${DISTILLERY_INSTALL_DIR_PREFIX}_*/lib/libfacemgr.*
	@rm -rf ${DISTILLERY_INSTALL_DIR_PREFIX}_*/include/hicn

viper-clean:
	@rm -rf ${DISTILLERY_BUILD_DIR_PREFIX}_*/viper

qt-clean:
	@rm -rf qt

env-clean: sdk-clean qt-clean

qtav-clean:
	@echo ${DISTILLERY_BUILD_DIR_PREFIX}
	@for each in ${DISTILLERY_BUILD_DIR_PREFIX}_*/qtav/sdk_uninstall.sh ; do bash $$each ; done
	@rm -rf ${DISTILLERY_BUILD_DIR_PREFIX}_*/qtav

version:
	@bash scripts/print_env_version.sh

all-clean: dependencies-clean hicn-clean qt-clean qtav-clean viper-clean

update:
	./scripts/update.sh ${COMMIT}

help:
	@echo "---- Basic build targets ----"
	@echo "make help					- This help message"
	@echo "make init					- Create the installation directories"
	@echo "make install-env				- Install android sdk and qt"
	@echo "make download-dep			- Download libevent, libconfig and asio"
	@echo "make compile-openssl			- Download and compile openssl"
	@echo "make install-asio			- Install Asio"
	@echo "make compile-dep				- Compile and install openssl, libconfig and libevent"
	@echo "make download-hicn			- Download libparc and hicn stack"
	@echo "make compile-hicn			- Compile and install libparc and hicn stack"
	@echo "make download-qtdep			- Download viper files and the qt depends"
	@echo "make compile-qtdep			- Compile and install ffmpeg, libdash, libcurl, qtAv and libxml2"
	@echo "make compile-ffmpeg			- compile ffmpeg"
	@echo "make install-qtav			- Compile and install qtAV"
	@echo "make update					- update hicn to the right commit"
	@echo "make all						- Download sdk, ndk and dependencies, configure, compile and install all hicn software in DISTILLERY_INSTALL_DIR"
	@echo "make all-withqt				- Download sdk, ndk, qt and dependencies, configure, compile and install all hicn and qt software in DISTILLERY_INSTALL_DIR"
	@echo "make build-qtdep				- Downlaod the qt environment and install the qt/viper dependencies"
	@echo "make install-all 			- Configure, compile and install all software in DISTILLERY_INSTALL_DIR"
	@echo "make curl-clean				- Clean curl files and libs"
	@echo "make openssl-clean			- Clean opennssl files and libs"
	@echo "make asio-clean				- Clean asio files"
	@echo "make event-clean				- Clean libevent files and libs"
	@echo "make ffmpeg-clean			- Clean ffmpeg files and libs"
	@echo "make libconfig-clean			- Clean libconfig files and libs"
	@echo "make xml2-clean				- Clean libxml2 files and libs"
	@echo "make libdash-clean			- Clean libdash files and libs"
	@echo "make viper-clean				- Clean viper files"
	@echo "make dependencies-clean 		- Clean all dependencies files and libs"
	@echo "make sdk-clean				- Clean sdk files"
	@echo "make qt-clean				- Clean qt files"
	@echo "make env-clean				- Clean android sdk and qt files"
	@echo "make libparc-clean			- Clean libparc files and libs"
	@echo "make hicn-clean				- Clean hicn files and libs"
	@echo "make all-clean				- Clean	all files and libs"
	@echo "make android_hicnforwarder GITHUB_USER=<github user> GITHUB_TOKEN=<github token>	- Build HicnForwader apk for android"
	@echo "make android_hicnforwarder_debug	GITHUB_USER=<github user> GITHUB_TOKEN=<github token> - Build HicnForwader apk for android in debug mode"
	@echo "make android_hicntools		- Build HicnTools apk for android"
	@echo "make android_hicntools_debug	- Build HicnTools apk for android in debug mode"
	@echo "make android_viper			- Build Viper apk for android apk in debug mode (only arm64)"
	@echo "make android_viper_debug		- Build Viper apk for android apk (only arm64)"
	@echo "make android_commonaar		- Build common aar module"
	@echo "make android_commonaar_debug	- Build common aar module for android in debug mode"
	@echo "make android_forwarderlibraryaar GITHUB_USER=<github user> GITHUB_TOKEN=<github token>	- Build hicn-light forwarder aar module"
	@echo "make android_forwarderlibraryaar_debug	GITHUB_USER=<github user> GITHUB_TOKEN=<github token> - Build hicn-light forwarder aar module in debug mode"
	@echo "make android_facemgrlibraryaar GITHUB_USER=<github user> GITHUB_TOKEN=<github token>	- Build face manager aar module"
	@echo "make android_facemgrlibraryaar_debug	GITHUB_USER=<github user> GITHUB_TOKEN=<github token> - Build face manager aar module in debug mode"
	@echo "make version				- Print the version of installed modules"

${DISTILLERY_STAMP}: ${REBUILD_DEPENDS}
	touch $@

install-directories:
	@mkdir -p ${DISTILLERY_INSTALL_DIR}/include
	@mkdir -p ${DISTILLERY_INSTALL_DIR}/lib
	@mkdir -p ${DISTILLERY_INSTALL_DIR}/bin


.PHONY: dependencies
