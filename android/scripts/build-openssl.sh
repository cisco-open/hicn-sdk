#!/bin/bash
set -e
set -x

SCRIPT_DIR=`realpath .`/scripts
SRC_DIR=`realpath .`/src
ARCH=$1
# Set directory
export ANDROID_NDK_HOME=$2

OPENSSL_DIR=$SRC_DIR/openssl
OPENSSL_BUILD_DIR=$DISTILLERY_BUILD_DIR/openssl


if [ ! -d $OPENSSL_DIR ]; then
	echo "openssl not found"
  cd $SRC_DIR
 
  git clone $OPENSSL_REPO -b $OPENSSL_VERSION

  cd ..
fi

# Find the toolchain for your build machine
toolchains_path=$(python3 $SCRIPT_DIR/toolchains_path.py --ndk ${NDK})

# Configure the OpenSSL environment, refer to NOTES.ANDROID in OPENSSL_DIR
# Set compiler clang, instead of gcc by default
CC=clang

# Add toolchains bin directory to PATH
PATH=$toolchains_path/bin:$PATH

AR=llvm-ar

# Set the target architecture
# Can be android-arm, android-arm64, android-x86, android-x86 etc
architecture=$ARCH

# Create the make file
mkdir -p ${OPENSSL_BUILD_DIR}
cd ${OPENSSL_BUILD_DIR}
export ANDROID_NDK_HOME=$toolchains_path

if [ "${ANDROID_ARCH}" = "arm" ]; then
  if [ ! -f "$toolchains_path/bin/arm-linux-android-clang" ]; then
    ln -sf $toolchains_path/bin/armv7a-linux-androideabi${ANDROID_COMPILE_SDK}-clang $toolchains_path/bin/arm-linux-androideabi-clang
  fi
  RANLIB=$toolchains_path/bin/llvm-ranlib
elif [ "${ANDROID_ARCH}" = "x86" ]; then
  if [ ! -f "$toolchains_path/bin/i686-linux-android-clang" ]; then
    ln -sf $toolchains_path/bin/i686-linux-android${ANDROID_COMPILE_SDK}-clang $toolchains_path/bin/i686-linux-android-clang
  fi
  RANLIB=$toolchains_path/bin/llvm-ranlib
elif [ "${ANDROID_ARCH}" = "x86_64" ]; then
  if [ ! -f "$toolchains_path/bin/x86_64-linux-android-clang" ]; then
    ln -sf $toolchains_path/bin/x86_64-linux-android${ANDROID_COMPILE_SDK}-clang $toolchains_path/bin/x86_64-linux-android-clang
  fi
  RANLIB=$toolchains_path/bin/llvm-ranlib
else
  if [ ! -f "$toolchains_path/bin/aarch64-linux-android-clang" ]; then
      ln -sf $toolchains_path/bin/aarch64-linux-android${ANDROID_COMPILE_SDK}-clang $toolchains_path/bin/aarch64-linux-android-clang
  fi
  if [ ! -f "$toolchains_path/bin/aarch64-linux-android-clang" ]; then
      ln -sf $toolchains_path/bin/aarch64-linux-android${ANDROID_COMPILE_SDK}-clang $toolchains_path/bin/aarch64-linux-android-clang
  fi
  RANLIB=$toolchains_path/bin/llvm-ranlib
fi

$OPENSSL_DIR/Configure ${architecture} -D__ANDROID_API__=$ANDROID_COMPILE_SDK no-shared no-unit-test no-tests --openssldir=$OPENSSL_DIR --prefix=$DISTILLERY_INSTALL_DIR

AR=$toolchains_path/bin/llvm-ar
# Build
make depend; make install_dev -j RANLIB=$RANLIB AR=$AR
echo "OK"
#RANLIB=$toolchains_path/bin/$ANDROID_ARCH-linux-androideabi-ranlib #AR=$toolchains_path/bin/arm-linux-androideabi-ar

