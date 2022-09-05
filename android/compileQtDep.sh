#!/bin/bash

set -e
make download-qtdep
export ANDROID_ARCH="arm64"
make compile-qtdep
export ANDROID_ARCH="x86_64"
make compile-qtdep