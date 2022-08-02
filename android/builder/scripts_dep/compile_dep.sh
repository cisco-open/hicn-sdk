#!/bin/bash

set -ex

if [ ! -d android-sdk ]; then
	git clone https://github.com/icn-team/android-sdk.git
fi

cd android-sdk
ln -s ../sdk .
ln -s ../qt .
./compileDep.sh
./compileQtDep.sh
cd ..
mv android-sdk/usr_aarch64 .
mv android-sdk/usr_x86_64 .
mv android-sdk/.versions .
rm -rf android-sdk
