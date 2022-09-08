#!/bin/bash
set -e

export ANDROID_ARCH="arm64"
make all VERBOSE=1
export ANDROID_ARCH="x86_64"
make all VERBOSE=1
