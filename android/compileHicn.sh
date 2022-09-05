#!/bin/bash

set -e
make download-hicn
export ANDROID_ARCH="arm64"
make compile-hicn
export ANDROID_ARCH="x86_64"
make compile-hicn
