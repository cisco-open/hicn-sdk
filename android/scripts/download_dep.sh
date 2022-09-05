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

if [ ! -d openssl ]; then
	echo "openssl  not found"
	wget $OPENSSL_REPO/${CISCOSSL_VERSION//./\/}/ciscossl-$OPENSSL_VERSION.$CISCOSSL_VERSION.tar.gz
    tar -zxvf ciscossl-$OPENSSL_VERSION.$CISCOSSL_VERSION.tar.gz
    mv ciscossl-$OPENSSL_VERSION.$CISCOSSL_VERSION openssl
	rm ciscossl-$OPENSSL_VERSION.$CISCOSSL_VERSION.tar.gz
fi

if [ ! -d libevent ]; then
    echo "libevent not found"
	git clone $LIBEVENT_REPO -b $LIBEVENT_VERSION
fi

if [ ! -d asio ]; then
	echo "Asio directory not found"
	git clone $ASIO_REPO -b $ASIO_VERSION
fi

if [ ! -d libconfig ]; then
	echo "libconfig not found"
	git clone $LIBCONFIG_REPO -b $LIBCONFIG_VERSION
	${SED} -i -- '2s/$/include(CheckSymbolExists)/' libconfig/CMakeLists.txt 
fi

cd ..
