
default.target: help

init:
	@powershell New-Item -ItemType Directory -Force -Path src; New-Item -ItemType Directory -Force -Path build; New-Item -ItemType Directory -Force -Path usr;

init_vcpkg:
	@powershell if (-not (Test-Path 'vcpkg' )) { git clone https://github.com/microsoft/vcpkg.git ; cd vcpkg ; .\bootstrap-vcpkg.bat; .\vcpkg integrate install} else {echo "vcpkg installed"}

openssl:
	@powershell .\vcpkg\vcpkg install openssl:x64-windows

libevent:
	@powershell .\vcpkg\vcpkg install libevent:x64-windows

asio:
	@powershell .\vcpkg\vcpkg install asio:x64-windows

libconfig:
	@powershell .\vcpkg\vcpkg install libconfig:x64-windows

pthreads:
	@powershell .\vcpkg\vcpkg install pthreads:x64-windows

download_libparc: init
	@powershell -noninteractive -nologo cd src; if (-not (Test-Path 'cframework' )) { echo "cframework not found"; git clone -b cframework/master https://gerrit.fd.io/r/cicn cframework; }

libparc: download_libparc
	@powershell -noninteractive -nologo try {Set-Variable -Name "BASE_DIR" -Value (pwd).Path;  New-Item -ItemType Directory -Force -Path "build\libparc"; cd build\libparc; cmake $${BASE_DIR}\src\cframework\libparc -G \"NMake Makefiles\" -DCMAKE_TOOLCHAIN_FILE=\"$${BASE_DIR}\vcpkg\scripts\buildsystems\vcpkg.cmake\" -DCMAKE_INSTALL_PREFIX=\"$${BASE_DIR}\usr\" -DCMAKE_BUILD_TYPE=\"Release\"; nmake install; } catch {};

download_hicn: init
	@powershell -noninteractive -nologo cd src; if (-not (Test-Path 'hicn' )) { echo "hicn not found"; git clone https://github.com/fdio/hicn.git; }

hicn: download_hicn
	@powershell -noninteractive -nologo try { Set-Variable -Name "BASE_DIR" -Value (pwd).Path; New-Item -ItemType Directory -Force -Path "build\hicn"; cd build\hicn; cmake $${BASE_DIR}\src\hicn -G \"NMake Makefiles\"  -DBUILD_CTRL=OFF -DBUILD_APPS=ON -DCMAKE_TOOLCHAIN_FILE=\"$${BASE_DIR}\vcpkg\scripts\buildsystems\vcpkg.cmake\" -DCMAKE_INSTALL_PREFIX=\"$${BASE_DIR}\usr\" -DDISABLE_SHARED_LIBRARIES=ON -DCMAKE_BUILD_TYPE="Release" -DLIBPARC_HOME=\"$${BASE_DIR}\usr\" -DCMAKE_INSTALL_PREFIX=\"$${BASE_DIR}\usr\"; nmake install; } catch {};

all: openssl libevent libconfig asio pthreads libparc hicn

help:
	@echo "---- Basic build targets ----"
	@echo "make all					- Compile hICN libraries and the dependencies"
	@echo "make init_vcpkg				- Init vcpkg"
	@echo "make openssl					- Compile openssl"
	@echo "make libevent					- Download and compile libevent"
	@echo "make libparc					- Download and compile libparc"
	@echo "make download_libconfig				- Download libconfig source code"
	@echo "make libconfig					- Download and compile libconfig"
	@echo "make download_asio				- Download asio source code"
	@echo "make asio					- Download and install asio"
	@echo "make download_hicn				- Download hicn source code"
	@echo "make hicn					- Download and compile hicn"
	@echo "make ptheads					- Download and compile ptheads"
