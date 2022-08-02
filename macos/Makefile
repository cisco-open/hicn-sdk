default.target: help

export BASE_DIR=$(shell pwd)
export QT_HOME=`pwd`/Qt
export QT_VERSION=5.13.2
export QT_VERSION_INSTALL=5132
export QT_CI_PACKAGES=qt.qt5.${QT_VERSION_INSTALL}.clang_64,qt.qt5.${QT_VERSION_INSTALL}.qtcharts.clang_64,qt.qt5.${QT_VERSION_INSTALL}.qtcharts


BREW_INSTALLED := $(shell eval which brew | wc -l	)
TAP_INSTALLED := $(shell eval brew tap  | grep icn-team/hicn-tap | wc -l)

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
			cd qt && if [ ! -d qtci ]; then git clone https://github.com/benlau/qtci.git; fi && export PATH=`pwd`/qtci/bin:`pwd`/qtci/recipes:"${PATH}" && install-qt ${QT_VERSION} && rm -rf qtci rm -rf Qt/MaintenanceTool.app && rm -rf Qt/Examples && rm -rf Qt/Docs && rm -rf Qt\ Creator.app; rm qt-opensource*; \
		fi; \
	fi

init_hicn_tap:
	@brew info hicn; if [ $$? != 0 ]; then echo "not present!"; brew tap icn-team/hicn-tap; fi

install_hicn_tap:
	@if [ ${BREW_INSTALLED} -eq 1 ] && [ ${TAP_INSTALLED} -ne 0 ]; then \
		brew install hicn; \
	fi

openssl_download: init
	@cd src && if [ ! -d openssl ]; then if [ ! -f openssl-1.1.1f.tar.gz ]; then wget https://www.openssl.org/source/openssl-1.1.1f.tar.gz; fi;  tar xf openssl-1.1.1f.tar.gz && mv openssl-1.1.1f openssl && rm -rf openssl-1.1.1f.tar.gz; fi

openssl_src: openssl_download
	@if [ ! -d ${BASE_DIR}/usr/include/openssl ]; then echo "openssl not found"; mkdir -p build/openssl && cd build/openssl && ${BASE_DIR}/src/openssl/Configure --prefix=${BASE_DIR}/usr --openssldir=${BASE_DIR}/src/openssl/ no-ssl3 no-ssl3-method no-zlib  darwin64-x86_64-cc enable-ec_nistp_64_gcc_128 && make -j && make install_sw; fi;
	@bash ${BASE_DIR}/scripts/script.sh ${BASE_DIR}/usr/lib/libssl.1.1.dylib
	@bash ${BASE_DIR}/scripts/script.sh ${BASE_DIR}/usr/lib/libcrypto.1.1.dylib

openssl: init
	@brew install openssl@1.1
	@cp -f /usr/local/opt/openssl@1.1/lib/lib* ${BASE_DIR}/usr/lib/
	@cp -rf /usr/local/opt/openssl@1.1/include/openssl ${BASE_DIR}/usr/include/
	@chmod 755 ${BASE_DIR}/usr/lib/libcrypto*
	@chmod 755 ${BASE_DIR}/usr/lib/libssl*
	@bash ${BASE_DIR}/scripts/script.sh ${BASE_DIR}/usr/lib/libssl.1.1.dylib
	@bash ${BASE_DIR}/scripts/script.sh ${BASE_DIR}/usr/lib/libcrypto.1.1.dylib

download_libevent: init
	@cd ${BASE_DIR}/src && if [ ! -d libevent ]; then echo "libevent not found"; git clone https://github.com/libevent/libevent.git; cd libevent; git checkout tags/release-2.1.11-stable; fi;

libevent_src: download_libevent
	@mkdir -p build/libevent && cd build/libevent && cmake ${BASE_DIR}/src/libevent -DCMAKE_FIND_ROOT_PATH=${BASE_DIR}/usr -DCMAKE_INSTALL_PREFIX=${BASE_DIR}/usr -DOPENSSL_ROOT_DIR=${BASE_DIR}/usr -DEVENT__DISABLE_TESTS=ON -DEVENT__DISABLE_SAMPLES=ON -DEVENT__HAVE_EPOLL=OFF -DEVENT__HAVE_PIPE2=OFF -DEVENT__DISABLE_BENCHMARK=ON && make -j && make -j && make install
	@bash ${BASE_DIR}/scripts/script.sh ${BASE_DIR}/usr/lib/libevent-2.1.7.dylib
	@bash ${BASE_DIR}/scripts/script.sh ${BASE_DIR}/usr/lib/libevent_core-2.1.7.dylib
	@bash ${BASE_DIR}/scripts/script.sh ${BASE_DIR}/usr/lib/libevent_extra-2.1.7.dylib
	@bash ${BASE_DIR}/scripts/script.sh ${BASE_DIR}/usr/lib/libevent_openssl-2.1.7.dylib
	@bash ${BASE_DIR}/scripts/script.sh ${BASE_DIR}/usr/lib/libevent_pthreads-2.1.7.dylib

libevent: init
	@brew install libevent
	@cp -f /usr/local/lib/libevent* ${BASE_DIR}/usr/lib/
	@cp -rf /usr/local/include/event* ${BASE_DIR}/usr/include/
	@chmod 755 ${BASE_DIR}/usr/lib/libevent*
	@bash ${BASE_DIR}/scripts/script.sh ${BASE_DIR}/usr/lib/libevent-2.1.7.dylib
	@bash ${BASE_DIR}/scripts/script.sh ${BASE_DIR}/usr/lib/libevent_core-2.1.7.dylib
	@bash ${BASE_DIR}/scripts/script.sh ${BASE_DIR}/usr/lib/libevent_extra-2.1.7.dylib
	@bash ${BASE_DIR}/scripts/script.sh ${BASE_DIR}/usr/lib/libevent_openssl-2.1.7.dylib
	@bash ${BASE_DIR}/scripts/script.sh ${BASE_DIR}/usr/lib/libevent_pthreads-2.1.7.dylib

download_libparc: init
	@cd ${BASE_DIR}/src && if [ ! -d cframework ]; then echo "cframework not found"; git clone -b cframework/master https://gerrit.fd.io/r/cicn cframework; fi;

libparc_src: download_libparc
	@mkdir -p build/libparc && cd build/libparc && cmake ${BASE_DIR}/src/cframework/libparc -DCMAKE_FIND_ROOT_PATH=${BASE_DIR}/usr -DCMAKE_INSTALL_PREFIX=${BASE_DIR}/usr -DOPENSSL_ROOT_DIR=${BASE_DIR}/usr -DDISABLE_EXECUTABLES=ON && make -j && make install
	@bash ${BASE_DIR}/scripts/script.sh ${BASE_DIR}/usr/lib/libparc.dylib

libparc: init init_hicn_tap
	@brew install libparc
	@cp -f /usr/local/lib/libparc* ${BASE_DIR}/usr/lib/
	@cp -rf /usr/local/include/parc ${BASE_DIR}/usr/include/
	@chmod 755 ${BASE_DIR}/usr/lib/libparc*
	@bash ${BASE_DIR}/scripts/script.sh ${BASE_DIR}/usr/lib/libparc.dylib

download_libconfig: init
	@cd ${BASE_DIR}/src && if [ ! -d libconfig ]; then echo "libconfig not found"; git clone https://github.com/hyperrealm/libconfig.git; cd libconfig; git checkout a6b370e78578f5bf594f8efe0802cdc9b9d18f1a; fi;

libconfig_src: download_libconfig
	@mkdir -p build/libconfig && cd build/libconfig && cmake ${BASE_DIR}/src/libconfig/ -DCMAKE_FIND_ROOT_PATH=${BASE_DIR}/usr -DCMAKE_INSTALL_PREFIX=${BASE_DIR}/usr -DBUILD_EXAMPLES=OFF -DBUILD_TESTS=OFF && make -j && make -j && make install
	@bash ${BASE_DIR}/scripts/script.sh ${BASE_DIR}/usr/lib/libconfig++.dylib
	@bash ${BASE_DIR}/scripts/script.sh ${BASE_DIR}/usr/lib/libconfig.dylib

libconfig:
	@brew install libconfig
	@cp -f /usr/local/lib/libconfig* ${BASE_DIR}/usr/lib/
	@cp -rf /usr/local/include/libconfig* ${BASE_DIR}/usr/include/
	@chmod 755 ${BASE_DIR}/usr/lib/libconfig*
	@bash ${BASE_DIR}/scripts/script.sh ${BASE_DIR}/usr/lib/libconfig++.dylib
	@bash ${BASE_DIR}/scripts/script.sh ${BASE_DIR}/usr/lib/libconfig.dylib

download_asio: init
	@cd ${BASE_DIR}/src && if [ ! -d asio ]; then echo "Asio directory not found"; git clone https://github.com/chriskohlhoff/asio.git; cd asio; git checkout tags/asio-1-12-2;	fi;

asio_src: download_asio
	@if [ ! -d ${BASE_DIR}/usr/include/asio ]; then cp -r ${BASE_DIR}/src/asio/asio/include/asio* ${BASE_DIR}/usr/include/; fi;

asio: init
	@brew install asio
	@cp -rf /usr/local/include/asio* ${BASE_DIR}/usr/include/

download_hicn: init
	@cd ${BASE_DIR}/src && if [ ! -d hicn ]; then echo "libhicn not found"; git clone https://github.com/FDio/hicn.git; fi;

hicn_src: download_hicn
	@mkdir -p build/hicn && cd build/hicn && cmake ${BASE_DIR}/src/hicn -DCMAKE_FIND_ROOT_PATH=${BASE_DIR}/usr  -DCMAKE_INSTALL_PREFIX=${BASE_DIR}/usr -DBUILD_APPS=ON -DOPENSSL_ROOT_DIR=${BASE_DIR}/usr -DBUILD_UTILS=ON && make -j && make install
	@bash ${BASE_DIR}/scripts/script.sh ${BASE_DIR}/usr/lib/libhicn.dylib
	@bash ${BASE_DIR}/scripts/script.sh ${BASE_DIR}/usr/lib/libhicntransport.dylib

hicn_package: hicn_src
	@mkdir -p build/hicn && cd build/hicn && make package

hicn: init init_hicn_tap
	@brew install hicn
	@cp -f /usr/local/lib/libhicn* ${BASE_DIR}/usr/lib/
	@cp -rf /usr/local/include/hicn* ${BASE_DIR}/usr/include/
	@chmod 755 ${BASE_DIR}/usr/lib/libhicn*
	@bash ${BASE_DIR}/scripts/script.sh ${BASE_DIR}/usr/lib/libhicn.dylib
	@bash ${BASE_DIR}/scripts/script.sh ${BASE_DIR}/usr/lib/libhicntransport.dylib

download_curl: init
	@cd ${BASE_DIR}/src && if [ ! -d curl ]; then echo "curl not found"; git clone https://github.com/curl/curl.git; cd curl; git checkout tags/curl-7_66_0; fi;

curl_src: download_curl openssl_src
	@mkdir -p build/curl && cd build/curl && cmake ${BASE_DIR}/src/curl  -DCMAKE_FIND_ROOT_PATH=${BASE_DIR}/usr  -DCMAKE_INSTALL_PREFIX=${BASE_DIR}/usr -DOPENSSL_ROOT_DIR=${BASE_DIR}/usr -DBUILD_CURL_EXE=OFF -DBUILD_TESTING=OFF && make -j && make install
	@mv -f ${BASE_DIR}/usr/lib/libcurl.dylib ${BASE_DIR}/usr/lib/libcurl.4.dylib
	@ln -s ${BASE_DIR}/usr/lib/libcurl.4.dylib ${BASE_DIR}/usr/lib/libcurl.dylib
	@bash ${BASE_DIR}/scripts/script.sh ${BASE_DIR}/usr/lib/libcurl.4.dylib

curl: init openssl
	brew install curl
	@cp -f /usr/local/opt/curl/lib/lib* ${BASE_DIR}/usr/lib/
	@cp -rf /usr/local/opt/curl/include/curl* ${BASE_DIR}/usr/include/
	@chmod 755 ${BASE_DIR}/usr/lib/libcurl*
	@bash ${BASE_DIR}/scripts/script.sh ${BASE_DIR}/usr/lib/libcurl.4.dylib

download_ffmpeg: init
	@cd ${BASE_DIR}/src && if [ ! -d ffmpeg ]; then if [ ! -f ffmpeg-4.2-macOS-lite.tar.gz ]; then echo "ffmpeg not found"; wget https://sourceforge.net/projects/avbuild/files/macOS/ffmpeg-4.2-macOS-lite.tar.xz; fi; tar xf ffmpeg-4.2-macOS-lite.tar.xz; rm -rf ffmpeg-4.2-macOS-lite.tar.xz; mv ffmpeg-4.2-macOS-lite ffmpeg; fi;

ffmpeg_src: download_ffmpeg
	@if [ ! -d ${BASE_DIR}/usr/include/libavcodec ] || [ ! -d ${BASE_DIR}/usr/include/libavfilter ] || [ ! -d ${BASE_DIR}/usr/include/libswresample ] || [ ! -d ${BASE_DIR}/usr/include/libavformat ] || [ ! -d ${BASE_DIR}/usr/include/libavutil ] || [ ! -d ${BASE_DIR}/usr/include/libswscale ]; then cp -rf ${BASE_DIR}/src/ffmpeg/include/* ${BASE_DIR}/usr/include/ ; cp -f ${BASE_DIR}/src/ffmpeg/lib/lib* ${BASE_DIR}/usr/lib/; fi;

ffmpeg: init
	@brew install ffmpeg
	@cp -f /usr/local/lib/libav* ${BASE_DIR}/usr/lib/
	@cp -f /usr/local/lib/libsw* ${BASE_DIR}/usr/lib/
	@cp -rf /usr/local/include/libav* ${BASE_DIR}/usr/include/
	@cp -rf /usr/local/include/libsw* ${BASE_DIR}/usr/include/
	@chmod 755 ${BASE_DIR}/usr/lib/libav*
	@chmod 755 ${BASE_DIR}/usr/lib/libsw*
	@for f in $(shell ls -1 ${BASE_DIR}/usr/lib/libav*.dylib); do bash ${BASE_DIR}/scripts/script.sh $${f}; done
	@for f in $(shell ls -1 ${BASE_DIR}/usr/lib/libsw*.dylib); do bash ${BASE_DIR}/scripts/script.sh $${f}; done


download_qtav: init
	@cd ${BASE_DIR}/src && if [ ! -d QtAV ]; then echo "qtav not found"; git clone https://github.com/wang-bin/QtAV.git; cd QtAV; git checkout 768dbd6ff2c9994cc10f2dc9b7764a8cca417e9e; git submodule update --init; echo "INCLUDEPATH = ${BASE_DIR}/usr/include/" >> .qmake.conf; echo "LIBS = -L${BASE_DIR}/usr/lib/" >> .qmake.conf; fi;

qtav_src: download_qtav
	@mkdir -p ${BASE_DIR}/build/qtav && cd ${BASE_DIR}/build/qtav && ${BASE_DIR}/qt/Qt/${QT_VERSION}/clang_64/bin/qmake ${BASE_DIR}/src/QtAV "target.path = ${BASE_DIR}/qt/Qt/5.13.2/clang_64" "CONFIG+=no-examples release" "share.path=${BASE_DIR}/qt/doc" && make -j && make install && bash sdk_install.sh

update_hicn_src: init
	@if [ -d ${BASE_DIR}/src/hicn ]; then cd ${BASE_DIR}/src/hicn; git pull; fi;

download_viper: init
	@cd ${BASE_DIR}/src && if [ ! -d viper ]; then echo "viper not found"; git clone https://github.com/FDio/cicn.git -b viper/master viper;  fi;

libdash_src: download_viper
	@mkdir -p build/libdash && cd build/libdash && cmake ${BASE_DIR}/src/viper/libdash -DCMAKE_FIND_ROOT_PATH=${BASE_DIR}/usr -DCURL_NO_CURL_CMAKE=ON -DCMAKE_INSTALL_PREFIX=${BASE_DIR}/usr && make -j && make install
	@bash ${BASE_DIR}/scripts/script.sh ${BASE_DIR}/usr/lib/libdash.dylib

update_libparc_src: init
	@if [ -d ${BASE_DIR}/src/cframework ]; then cd ${BASE_DIR}/src/cframework; git pull; fi;

update: update_libparc_src update_hicn_src

dependencies:  openssl libevent libconfig asio

all: openssl libevent libconfig asio libparc hicn

all_src: openssl_src libevent_src libconfig_src asio_src libparc_src hicn_src

qt_dep_src: init_qt ffmpeg_src qtav_src curl_src libdash_src

qt_dep: init_qt ffmpeg qtav_src curl libdash_src

all_qt_src: qt_dep_src all_src

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
