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


#!/bin/bash
set -ex
BASE_DIR=`pwd`
BASIC_HOME=${QT_HOME}
export ANDROID_ARCH=arm64_v8a
export ANDROID_HOME=${SDK}
export ANDROID_NDK_HOST=${OS}-${ARCH}
export ANDROID_NDK_PLATFORM=android-29
export ANDROID_NDK_ROOT=${NDK}
export ANDROID_SDK_ROOT=${SDK}
export ANDROID_API_VERSION=android-29
export PATH=$PATH:${ANDROID_HOME}/tools:${JAVA_HOME}/bin
export DISTILLARY_INSTALLATION_PATH=${DISTILLERY_ROOT_DIR}/usr_aarch64/
export QT_VERSION=5.13.2
export QT_HOME=${BASIC_HOME}
VERSION_CODE="${2:-1}"

do_restore() {
	pwd
	${SED} -i -e "s/android:versionCode=\"$VERSION_CODE\"/android:versionCode=\"9\"/g" ${DISTILLERY_ROOT_DIR}/src/viper/android/AndroidManifest.xml
	${SED} -i -e "s/android:targetSdkVersion=\"29\"/android:targetSdkVersion=\"27\"/g" ${DISTILLERY_ROOT_DIR}/src/viper/android/AndroidManifest.xml
	${SED} -i -e "s/29/28/g" ${DISTILLERY_ROOT_DIR}/src/viper/android/gradle.properties
}

trap "do_restore" ERR

${SED} -i -e "s/android:versionCode=\"9\"/android:versionCode=\"$VERSION_CODE\"/g" ${DISTILLERY_ROOT_DIR}/src/viper/android/AndroidManifest.xml
${SED} -i -e "s/android:targetSdkVersion=\"27\"/android:targetSdkVersion=\"29\"/g" ${DISTILLERY_ROOT_DIR}/src/viper/android/AndroidManifest.xml
${SED} -i -e "s/28/29/g" ${DISTILLERY_ROOT_DIR}/src/viper/android/gradle.properties

if [ "$1" = "DEBUG" ]; then
	mkdir -p build_aarch64/viper_debug
	cd build_aarch64/viper_debug
	${QT_HOME}/${QT_VERSION}/android_${ANDROID_ARCH}/bin/qmake -r -spec android-clang ${DISTILLERY_ROOT_DIR}/src/viper/viper.pro  "TRANSPORT_LIBRARY = HICNET" CONFIG+=debug CONFIG+=qml_debug
	make
	make install INSTALL_ROOT=hicn-viper-${ANDROID_ARCH}
	${QT_HOME}/${QT_VERSION}/android_${ANDROID_ARCH}/bin/androiddeployqt --output hicn-viper-${ANDROID_ARCH} --verbose --input android-libhicn-viper.so-deployment-settings.json --gradle --android-platform ${ANDROID_NDK_PLATFORM} --stacktrace --debug --target ${ANDROID_NDK_PLATFORM} --debug --sign ${DISTILLERY_ROOT_DIR}/src/viper/android/viper.keystore viper --storepass icn_viper
else
	mkdir -p build_aarch64/viper
	cd build_aarch64/viper
	${QT_HOME}/${QT_VERSION}/android_${ANDROID_ARCH}/bin/qmake -r -spec android-clang ${DISTILLERY_ROOT_DIR}/src/viper/viper.pro  "TRANSPORT_LIBRARY = HICNET"
	make
	make install INSTALL_ROOT=hicn-viper-${ANDROID_ARCH}
	${QT_HOME}/${QT_VERSION}/android_${ANDROID_ARCH}/bin/androiddeployqt --output hicn-viper-${ANDROID_ARCH} --verbose --input android-libhicn-viper.so-deployment-settings.json \
	--gradle --android-platform ${ANDROID_NDK_PLATFORM} --stacktrace --release --target ${ANDROID_NDK_PLATFORM} --release --sign ${DISTILLERY_ROOT_DIR}/src/viper/android/viper.keystore viper --storepass icn_viper
fi
cd ..
cd ${BASE_DIR}
export ANDROID_ARCH=x86_64
export DISTILLARY_INSTALLATION_PATH=${DISTILLERY_ROOT_DIR}/usr_x86_64/
export QT_VERSION=5.13.2
export QT_HOME=${BASIC_HOME}
if [ "$1" = "DEBUG" ]; then
	mkdir -p build_x86_64/viper_debug
	cd build_x86_64/viper_debug
	${QT_HOME}/${QT_VERSION}/android_${ANDROID_ARCH}/bin/qmake -r -spec android-clang ${DISTILLERY_ROOT_DIR}/src/viper/viper.pro  "TRANSPORT_LIBRARY = HICNET" CONFIG+=debug CONFIG+=qml_debug
	make
	make install INSTALL_ROOT=hicn-viper-${ANDROID_ARCH}
	${QT_HOME}/${QT_VERSION}/android_${ANDROID_ARCH}/bin/androiddeployqt --output hicn-viper-${ANDROID_ARCH} --verbose --input android-libhicn-viper.so-deployment-settings.json --gradle --android-platform ${ANDROID_NDK_PLATFORM} --stacktrace --debug --target ${ANDROID_NDK_PLATFORM} --debug --sign ${DISTILLERY_ROOT_DIR}/src/viper/android/viper.keystore viper --storepass icn_viper
else
	mkdir -p build_x86_64/viper
	cd build_x86_64/viper
	${QT_HOME}/${QT_VERSION}/android_${ANDROID_ARCH}/bin/qmake -r -spec android-clang ${DISTILLERY_ROOT_DIR}/src/viper/viper.pro  "TRANSPORT_LIBRARY = HICNET"
	make
	make install INSTALL_ROOT=hicn-viper-${ANDROID_ARCH}
	${QT_HOME}/${QT_VERSION}/android_${ANDROID_ARCH}/bin/androiddeployqt --output hicn-viper-${ANDROID_ARCH} --verbose --input android-libhicn-viper.so-deployment-settings.json \
	--gradle --android-platform ${ANDROID_NDK_PLATFORM} --stacktrace --release --target ${ANDROID_NDK_PLATFORM} --release --sign ${DISTILLERY_ROOT_DIR}/src/viper/android/viper.keystore viper --storepass icn_viper
fi
do_restore
cd ..
