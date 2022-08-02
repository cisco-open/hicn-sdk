
default.target: help

export BASE_DIR=$(shell pwd)
export QT_HOME=`pwd`/Qt
export QT_VERSION=5.13.2
export QT_VERSION_INSTALL=5132
export QT_CI_PACKAGES=qt.qt5.${QT_VERSION_INSTALL}.ios,qt.qt5.${QT_VERSION_INSTALL}.qtcharts.ios,qt.qt5.${QT_VERSION_INSTALL}.qtcharts

init:
	@mkdir -p usr/lib && mkdir -p usr/include && mkdir -p src && mkdir -p qt

init_qt:
	@mkdir -p qt
	@if [ ! -d ${BASE_DIR}/qt/Qt ]; then \
		if [ -z ${QT_CI_LOGIN} ] || [ -z ${QT_CI_PASSWORD} ]; then \
			echo "QT_CI_LOGIN and/or QT_CI_PASSWORD not set."; \
			echo "export QT_CI_LOGIN=<qt username>"; \
			echo "export QT_CI_PASSWORD=<qt password>"; \
			echo "If you don't have a qt account, please create a new one on:"; \
			echo "https://login.qt.io/register"; \
			exit 1; \
		else \
			cd qt && if [ ! -d qtci ]; then git clone https://github.com/benlau/qtci.git; fi && export PATH=`pwd`/qtci/bin:`pwd`/qtci/recipes:"${PATH}" && install-qt ${QT_VERSION} && rm qt-opensource* && rm -rf qtci && rm -rf Qt/MaintenanceTool.app && rm -rf Qt/Examples && rm -rf Docs && rm -rf Qt/Qt\ Creator.app; \
		fi; \
	fi

openssl: init
	@if [ ! -d usr/include/openssl ]; then cd scripts/OpenSSL-for-iPhone && ./build-libssl.sh --cleanup  --deprecated --targets="ios-sim-cross-x86_64 ios64-cross-arm64 ios64-cross-arm64e" && cp -r include/openssl ../../usr/include && cp -r lib/*.a ../../usr/lib; fi;

download_libevent: init
	@cd ${BASE_DIR}/src && if [ ! -d libevent ]; then echo "libevent not found"; git clone https://github.com/libevent/libevent.git; cd libevent; git checkout tags/release-2.1.11-stable; fi;

libevent: download_libevent
	@mkdir -p build/libevent/OS64 && cd build/libevent/OS64 && cmake ${BASE_DIR}/src/libevent -G Xcode -DCMAKE_TOOLCHAIN_FILE=${BASE_DIR}/cmake/ios.toolchain.cmake -DPLATFORM=OS64 -DCMAKE_FIND_ROOT_PATH=${BASE_DIR}/usr -DCMAKE_INSTALL_PREFIX=${BASE_DIR}/build/libevent/OS64 -DOPENSSL_ROOT_DIR=${BASE_DIR}/usr -DEVENT__DISABLE_TESTS=ON -DEVENT__DISABLE_SAMPLES=ON -DEVENT__LIBRARY_TYPE=STATIC -DEVENT__HAVE_EPOLL=OFF -DEVENT__HAVE_PIPE2=OFF -DEVENT__DISABLE_BENCHMARK=ON && cmake --build . --config Release --target install
	@mkdir -p build/libevent/SIMULATOR64 && cd build/libevent/SIMULATOR64 && cmake ${BASE_DIR}/src/libevent -G Xcode -DCMAKE_TOOLCHAIN_FILE=${BASE_DIR}/cmake/ios.toolchain.cmake -DPLATFORM=SIMULATOR64 -DCMAKE_FIND_ROOT_PATH=${BASE_DIR}/usr -DCMAKE_INSTALL_PREFIX=${BASE_DIR}/build/libevent/SIMULATOR64 -DOPENSSL_ROOT_DIR=${BASE_DIR}/usr -DEVENT__DISABLE_TESTS=ON -DEVENT__DISABLE_SAMPLES=ON -DEVENT__LIBRARY_TYPE=STATIC -DEVENT__HAVE_EPOLL=OFF -DEVENT__HAVE_PIPE2=OFF -DEVENT__DISABLE_BENCHMARK=ON && cmake --build . --config Release --target install
	@cp -rf ${BASE_DIR}/build/libevent/OS64/include/* ${BASE_DIR}/usr/include/
	@lipo -create ${BASE_DIR}/build/libevent/OS64/lib/libevent.a ${BASE_DIR}/build/libevent/SIMULATOR64/lib/libevent.a -output ${BASE_DIR}/usr/lib/libevent.a
	@lipo -create ${BASE_DIR}/build/libevent/OS64/lib/libevent_extra.a ${BASE_DIR}/build/libevent/SIMULATOR64/lib/libevent_extra.a -output ${BASE_DIR}/usr/lib/libevent_extra.a
	@lipo -create ${BASE_DIR}/build/libevent/OS64/lib/libevent_pthreads.a ${BASE_DIR}/build/libevent/SIMULATOR64/lib/libevent_pthreads.a -output ${BASE_DIR}/usr/lib/libevent_pthreads.a
	@lipo -create ${BASE_DIR}/build/libevent/OS64/lib/libevent_core.a ${BASE_DIR}/build/libevent/SIMULATOR64/lib/libevent_core.a -output ${BASE_DIR}/usr/lib/libevent_core.a
	@lipo -create ${BASE_DIR}/build/libevent/OS64/lib/libevent_openssl.a ${BASE_DIR}/build/libevent/SIMULATOR64/lib/libevent_openssl.a -output ${BASE_DIR}/usr/lib/libevent_openssl.a

download_libparc: init
	@cd ${BASE_DIR}/src && if [ ! -d cframework ]; then echo "cframework not found"; git clone -b cframework/master https://gerrit.fd.io/r/cicn cframework; fi;

libparc: download_libparc
	@mkdir -p build/libparc/OS64 && cd build/libparc/OS64 && cmake ${BASE_DIR}/src/cframework/libparc -G Xcode -DCMAKE_TOOLCHAIN_FILE=${BASE_DIR}/cmake/ios.toolchain.cmake -DPLATFORM=OS64 -DCMAKE_FIND_ROOT_PATH=${BASE_DIR}/usr -DCMAKE_INSTALL_PREFIX=${BASE_DIR}/build/libparc/OS64 -DOPENSSL_ROOT_DIR=${BASE_DIR}/usr -DDISABLE_EXECUTABLES=ON -DDISABLE_SHARED_LIBRARIES=ON -DDEPLOYMENT_TARGET=13.0 && cmake --build . --config Release --target install
	@mkdir -p build/libparc/SIMULATOR64 && cd build/libparc/SIMULATOR64 && cmake ${BASE_DIR}/src/cframework/libparc -G Xcode -DCMAKE_TOOLCHAIN_FILE=${BASE_DIR}/cmake/ios.toolchain.cmake -DPLATFORM=SIMULATOR64 -DCMAKE_FIND_ROOT_PATH=${BASE_DIR}/usr -DCMAKE_INSTALL_PREFIX=${BASE_DIR}/build/libparc/SIMULATOR64 -DOPENSSL_ROOT_DIR=${BASE_DIR}/usr -DDISABLE_EXECUTABLES=ON -DDISABLE_SHARED_LIBRARIES=ON -DDEPLOYMENT_TARGET=13.0 && cmake --build . --config Release --target install
	@cp -rf ${BASE_DIR}/build/libparc/OS64/include/* ${BASE_DIR}/usr/include/
	@lipo -create ${BASE_DIR}/build/libparc/OS64/lib/libparc.a ${BASE_DIR}/build/libparc/SIMULATOR64/lib/libparc.a -output ${BASE_DIR}/usr/lib/libparc.a


download_libconfig: init
	@cd ${BASE_DIR}/src && if [ ! -d libconfig ]; then echo "libconfig not found"; git clone https://github.com/hyperrealm/libconfig.git; cd libconfig; git checkout a6b370e78578f5bf594f8efe0802cdc9b9d18f1a; fi;

libconfig: download_libconfig
	@mkdir -p build/libconfig/OS64 && cd build/libconfig/OS64 && cmake ${BASE_DIR}/src/libconfig/ -G Xcode -DCMAKE_TOOLCHAIN_FILE=${BASE_DIR}/cmake/ios.toolchain.cmake -DPLATFORM=OS64 -DCMAKE_FIND_ROOT_PATH=${BASE_DIR}/usr -DCMAKE_INSTALL_PREFIX=${BASE_DIR}/build/libconfig/OS64 -DBUILD_SHARED_LIBS=OFF -DBUILD_EXAMPLES=OFF -DBUILD_TESTS=OFF && cmake --build . --config Release --target install
	@mkdir -p build/libconfig/SIMULATOR64 && cd build/libconfig/SIMULATOR64 && cmake ${BASE_DIR}/src/libconfig -G Xcode -DCMAKE_TOOLCHAIN_FILE=${BASE_DIR}/cmake/ios.toolchain.cmake -DPLATFORM=SIMULATOR64 -DCMAKE_FIND_ROOT_PATH=${BASE_DIR}/usr -DCMAKE_INSTALL_PREFIX=${BASE_DIR}/build/libconfig/SIMULATOR64 -DBUILD_SHARED_LIBS=OFF -DBUILD_EXAMPLES=OFF -DBUILD_TESTS=OFF && cmake --build . --config Release --target install
	@cp -rf ${BASE_DIR}/build/libconfig/OS64/include/* ${BASE_DIR}/usr/include/
	@lipo -create ${BASE_DIR}/build/libconfig/OS64/lib/libconfig++.a ${BASE_DIR}/build/libconfig/SIMULATOR64/lib/libconfig++.a -output ${BASE_DIR}/usr/lib/libconfig++.a
	@lipo -create ${BASE_DIR}/build/libconfig/OS64/lib/libconfig.a ${BASE_DIR}/build/libconfig/SIMULATOR64/lib/libconfig.a -output ${BASE_DIR}/usr/lib/libconfig.a

download_asio: init
	@cd ${BASE_DIR}/src && if [ ! -d asio ]; then echo "Asio directory not found"; git clone https://github.com/chriskohlhoff/asio.git; cd asio; git checkout tags/asio-1-12-2;	fi;

asio: download_asio
	@if [ ! -d ${BASE_DIR}/usr/include/asio ]; then cp -r ${BASE_DIR}/src/asio/asio/include/asio* ${BASE_DIR}/usr/include/; fi;

download_hicn: init
	@cd ${BASE_DIR}/src && if [ ! -d hicn ]; then echo "libhicn not found"; git clone https://github.com/FDio/hicn.git; fi;

hicn: download_hicn
	@mkdir -p build/hicn/OS64 && cd build/hicn/OS64 && cmake ${BASE_DIR}/src/hicn -G Xcode -DCMAKE_TOOLCHAIN_FILE=${BASE_DIR}/cmake/ios.toolchain.cmake -DPLATFORM=OS64 -DCMAKE_FIND_ROOT_PATH=${BASE_DIR}/usr  -DCMAKE_INSTALL_PREFIX=${BASE_DIR}/build/hicn/OS64 -DOPENSSL_ROOT_DIR=${BASE_DIR}/usr -DDISABLE_EXECUTABLES=ON -DDISABLE_SHARED_LIBRARIES=ON -DDEPLOYMENT_TARGET=13.0 && cmake --build . --config Release --target install
	@mkdir -p build/hicn/SIMULATOR64 && cd build/hicn/SIMULATOR64 && cmake ${BASE_DIR}/src/hicn -G Xcode -DCMAKE_TOOLCHAIN_FILE=${BASE_DIR}/cmake/ios.toolchain.cmake -DPLATFORM=SIMULATOR64 -DCMAKE_FIND_ROOT_PATH=${BASE_DIR}/usr  -DCMAKE_INSTALL_PREFIX=${BASE_DIR}/build/hicn/SIMULATOR64 -DOPENSSL_ROOT_DIR=${BASE_DIR}/usr -DDISABLE_EXECUTABLES=ON -DDISABLE_SHARED_LIBRARIES=ON -DDEPLOYMENT_TARGET=13.0 && cmake --build . --config Release --target install
	@cp -rf ${BASE_DIR}/build/hicn/OS64/include/* ${BASE_DIR}/usr/include/
	@lipo -create ${BASE_DIR}/build/hicn/OS64/lib/libfacemgr.a ${BASE_DIR}/build/hicn/SIMULATOR64/lib/libfacemgr.a -output ${BASE_DIR}/usr/lib/libfacemgr.a
	@lipo -create ${BASE_DIR}/build/hicn/OS64/lib/libhicn-light.a ${BASE_DIR}/build/hicn/SIMULATOR64/lib/libhicn-light.a -output ${BASE_DIR}/usr/lib/libhicn-light.a
	@lipo -create ${BASE_DIR}/build/hicn/OS64/lib/libhicn.a ${BASE_DIR}/build/hicn/SIMULATOR64/lib/libhicn.a -output ${BASE_DIR}/usr/lib/libhicn.a
	@lipo -create ${BASE_DIR}/build/hicn/OS64/lib/libhicnctrl.a ${BASE_DIR}/build/hicn/SIMULATOR64/lib/libhicnctrl.a -output ${BASE_DIR}/usr/lib/libhicnctrl.a
	@lipo -create ${BASE_DIR}/build/hicn/OS64/lib/libhicntransport.a ${BASE_DIR}/build/hicn/SIMULATOR64/lib/libhicntransport.a -output ${BASE_DIR}/usr/lib/libhicntransport.a

download_curl: init
	@cd ${BASE_DIR}/src && if [ ! -d curl ]; then echo "curl not found"; git clone https://github.com/curl/curl.git; cd curl; git checkout tags/curl-7_66_0; fi;

curl: download_curl openssl
	@mkdir -p build/curl/OS64 && cd build/curl/OS64 && cmake ${BASE_DIR}/src/curl -G Xcode -DCMAKE_TOOLCHAIN_FILE=${BASE_DIR}/cmake/ios.toolchain.cmake -DPLATFORM=OS64 -DCMAKE_FIND_ROOT_PATH=${BASE_DIR}/usr  -DCMAKE_INSTALL_PREFIX=${BASE_DIR}/build/curl/OS64 -DOPENSSL_ROOT_DIR=${BASE_DIR}/usr -DBUILD_CURL_EXE=OFF -DBUILD_SHARED_LIBS=OFF -DBUILD_TESTING=OFF -DDEPLOYMENT_TARGET=13.0 && cmake --build . --config Release --target install
	@mkdir -p build/curl/SIMULATOR64 && cd build/curl/SIMULATOR64 && cmake ${BASE_DIR}/src/curl -G Xcode -DCMAKE_TOOLCHAIN_FILE=${BASE_DIR}/cmake/ios.toolchain.cmake -DPLATFORM=SIMULATOR64 -DCMAKE_FIND_ROOT_PATH=${BASE_DIR}/usr  -DCMAKE_INSTALL_PREFIX=${BASE_DIR}/build/curl/SIMULATOR64 -DOPENSSL_ROOT_DIR=${BASE_DIR}/usr -DBUILD_CURL_EXE=OFF -DBUILD_SHARED_LIBS=OFF -DBUILD_TESTING=OFF -DDEPLOYMENT_TARGET=13.0 && cmake --build . --config Release --target install
	@cp -rf ${BASE_DIR}/build/curl/OS64/include/* ${BASE_DIR}/usr/include/
	@lipo -create ${BASE_DIR}/build/curl/OS64/lib/libcurl.a ${BASE_DIR}/build/curl/SIMULATOR64/lib/libcurl.a -output ${BASE_DIR}/usr/lib/libcurl.a

download_ffmpeg: init
	@cd ${BASE_DIR}/src && if [ ! -d ffmpeg ]; then if [ ! -f ffmpeg-4.2-iOS-lite.tar.gz ]; then echo "ffmpeg not found"; wget https://iweb.dl.sourceforge.net/project/avbuild/iOS/ffmpeg-4.2-iOS-lite.tar.xz; fi; tar xf ffmpeg-4.2-iOS-lite.tar.xz; rm -rf ffmpeg-4.2-iOS-lite.tar.xz; mv ffmpeg-4.2-iOS-lite ffmpeg; fi;

ffmpeg: download_ffmpeg
	@if [ ! -d ${BASE_DIR}/usr/include/libavcodec ] || [ ! -d ${BASE_DIR}/usr/include/libavfilter ] || [ ! -d ${BASE_DIR}/usr/include/libswresample ] || [ ! -d ${BASE_DIR}/usr/include/libavformat ] || [ ! -d ${BASE_DIR}/usr/include/libavutil ] || [ ! -d ${BASE_DIR}/usr/include/libswscale ]; then cp -rf ${BASE_DIR}/src/ffmpeg/include/* ${BASE_DIR}/usr/include/ ; cp -rf ${BASE_DIR}/src/ffmpeg/lib/* ${BASE_DIR}/usr/lib/; fi;

download_qtav: init
	@cd ${BASE_DIR}/src && if [ ! -d QtAV ]; then echo "qtav not found"; git clone https://github.com/wang-bin/QtAV.git; cd QtAV; git checkout 0307c174a4197fd33b1c1e7d37406d1ee5df6c82; git submodule update --init; sed -i '' 's/\/usr\/share\/doc/.\/usr\/share\/doc/' deploy.pri; echo "INCLUDEPATH = ${BASE_DIR}/usr/include/" > .qmake.conf; echo "LIBS = -L${BASE_DIR}/usr/lib/" >> .qmake.conf; fi;

qtav: download_qtav
	@mkdir -p ${BASE_DIR}/build/qtav && cd ${BASE_DIR}/build/qtav && ${BASE_DIR}/qt/Qt/${QT_VERSION}/ios/bin/qmake ${BASE_DIR}/src/QtAV "target.path = ${BASE_DIR}/qt/Qt/5.13.2/ios" "CONFIG+=no-examples release" "share.path=${BASE_DIR}/qt/doc" && make && make install && bash sdk_install.sh

update_hicn: init
	@if [ -d ${BASE_DIR}/src/hicn ]; then cd ${BASE_DIR}/src/hicn; git pull; fi;

download_viper: init
	@cd ${BASE_DIR}/src && if [ ! -d viper ]; then echo "viper not found"; git clone https://github.com/FDio/cicn.git -b viper/master viper;  fi;

libdash: download_viper
	@mkdir -p build/libdash/OS64 && cd build/libdash/OS64 && cmake ${BASE_DIR}/src/viper/libdash -G Xcode -DCMAKE_TOOLCHAIN_FILE=${BASE_DIR}/cmake/ios.toolchain.cmake -DPLATFORM=OS64 -DCMAKE_FIND_ROOT_PATH=${BASE_DIR}/usr  -DCMAKE_INSTALL_PREFIX=${BASE_DIR}/build/libdash/OS64 -DCOMPILE_FOR_IOS=ON -DDEPLOYMENT_TARGET=13.0 && cmake --build . --config Release --target install
	@mkdir -p build/libdash/SIMULATOR64 && cd build/libdash/SIMULATOR64 && cmake ${BASE_DIR}/src/viper/libdash -G Xcode -DCMAKE_TOOLCHAIN_FILE=${BASE_DIR}/cmake/ios.toolchain.cmake -DPLATFORM=SIMULATOR64 -DCMAKE_FIND_ROOT_PATH=${BASE_DIR}/usr  -DCMAKE_INSTALL_PREFIX=${BASE_DIR}/build/libdash/SIMULATOR64 -DCOMPILE_FOR_IOS=ON  -DDEPLOYMENT_TARGET=13.0 && cmake --build . --config Release --target install
	@cp -rf ${BASE_DIR}/build/libdash/OS64/include/* ${BASE_DIR}/usr/include/
	@lipo -create ${BASE_DIR}/build/libdash/OS64/lib/libdash.a ${BASE_DIR}/build/libdash/SIMULATOR64/lib/libdash.a -output ${BASE_DIR}/usr/lib/libdash.a


update_libparc: init
	@if [ -d ${BASE_DIR}/src/cframework ]; then cd ${BASE_DIR}/src/cframework; git pull; fi;

update: update_libparc update_hicn

all: openssl libevent libconfig asio libparc hicn

qt_dep: init_qt ffmpeg qtav curl libdash

all_qt: qt_dep all

help:
	@echo "---- Basic build targets ----"
	@echo "make all					- Compile hICN libraries and the dependencies"
	@echo "make all_qt					- Compile hICN libraries, install the Qt environment and build all the dependencies"
	@echo "make init_qt					- Install the Qt environment"
	@echo "make openssl					- Compile openssl"
	@echo "make download_libevent				- Download libevent"
	@echo "make libevent					- Download and compile libevent"
	@echo "make download_libparc				- Download libparc source code"
	@echo "make libparc					- Download and compile libparc"
	@echo "make download_libconfig				- Download libconfig source code"
	@echo "make libconfig					- Download and compile libconfig"
	@echo "make download_asio				- Download asio source code"
	@echo "make asio					- Download and install asio"
	@echo "make download_hicn				- Download hicn source code"
	@echo "make hicn					- Download and compile hicn"
	@echo "make download_ffmpeg				- Download ffmpeg libs"
	@echo "make ffmpeg					- Download and install ffmpeg"
	@echo "make download_curl				- Download libcurl source code"
	@echo "make hicn					- Download and compile libcurl"
	@echo "make download_qtav				- Download qtav source code"
	@echo "make qtav					- Download and compile qtav"
	@echo "make download_viper				- Download viper and libdash source code"
	@echo "make build_libdash				- Download viper and libdash and compile libdash"
	@echo "make update					- Update hicn and libparc source code"
	@echo "make update_hicn				- Update hicn source code"
	@echo "make update_libparc				- Update libparc source code"
