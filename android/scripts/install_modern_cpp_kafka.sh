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
ABI=$1
OS=`echo $OS | tr '[:upper:]' '[:lower:]'`
export BASE_DIR=`pwd`
mkdir -p ${DISTILLERY_INSTALL_DIR}
mkdir -p ${DISTILLERY_INSTALL_DIR}/include


if [ ! -d ${DISTILLERY_INSTALL_DIR}/include/kafka/KafkaConsumer.h ]; then
    mkdir -p ${DISTILLERY_INSTALL_DIR}/include/kafka
    cd src
    echo "Modern cpp kafka not found"
    if [ ! -d modern-cpp-kafka ]; then
        echo "Modern directory not found"
        git clone $MODERN_CPP_KAFKA_REPO -b $MODERN_CPP_KAFKA_VERSION
    fi
    cp -r modern-cpp-kafka/include/kafka/* ${DISTILLERY_INSTALL_DIR}/include/kafka/
    MODERN_CPP_KAFKA_VERSION=$(git log -1 --format="%H")
    ${SED} -i "/${ABI}_modern_cpp_kafka/d" ${VERSIONS_FILE}
	echo ${ABI}_asio=$MODERN_CPP_KAFKA_VERSION >> ${VERSIONS_FILE}
    cd ..
fi
