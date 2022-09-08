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

set -e
QT_VERSION=5.13.2
QT_VERSION_INSTALL=5132

mkdir -p qt
cd qt
export QT_HOME=`pwd`/Qt
echo ${QT_HOME}

if [ ! -d ${QT_HOME} ]; then
	if [ -z ${QT_CI_LOGIN} ] || [ -z ${QT_CI_PASSWORD} ]; then
		echo "QT_CI_LOGIN and/or QT_CI_PASSWORD not set."
		echo "export QT_CI_LOGIN=<qt username>"
		echo "export QT_CI_PASSWORD=<qt password>"
		echo "If you don't have a qt account, please create a new one on:"
		echo "https://login.qt.io/register"
		exit 1;
	fi
	if [ ! -d qtci ]; then
		git clone https://github.com/benlau/qtci.git
	fi

	export PATH=$PATH:`pwd`/qtci/bin:`pwd`/qtci/recipes
	export QT_CI_PACKAGES=qt.qt5.${QT_VERSION_INSTALL}.android_arm64_v8a,qt.qt5.${QT_VERSION_INSTALL}.qtcharts.android_arm64_v8a,qt.qt5.${QT_VERSION_INSTALL}.qtcharts,qt.qt5.${QT_VERSION_INSTALL}.android_x86_64,qt.qt5.${QT_VERSION_INSTALL}.qtcharts.android_x86_64
	install-qt ${QT_VERSION}
	rm -rf ${QT_HOME}/MaintenanceTool*
	rm -rf ${QT_HOME}/Qt\ Creator*
	rm -rf ${QT_HOME}/Tools
	rm -rf ${QT_HOME}/Examples
	rm -rf ${QT_HOME}/Docs
fi
