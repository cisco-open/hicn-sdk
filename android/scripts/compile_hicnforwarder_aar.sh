#!/bin/bash
set -e
ENABLE_DEBUG="NODEBUG"
VERSION_CODE=1
GITHUB_USER=""
GITHUB_TOKEN=""
MVN_REPO=""
while getopts ":d:v:u:t:r:" opt; do
  case $opt in
    d) ENABLE_DEBUG="$OPTARG"
    ;;
    v) VERSION_CODE="$OPTARG"
    ;;
    u) GITHUB_USER="$OPTARG"
    ;;
    t) GITHUB_TOKEN="$OPTARG"
    ;;
    r) MVN_REPO="$OPTARG"
    ;;
    \?) echo "Invalid option -$OPTARG" >&2
    ;;
  esac
done

cd hicn-aar/ForwarderLibrary
echo "${SDK}"
if [ ! -f local.properties ]; then
	  echo sdk.dir=${SDK} > local.properties
    echo ndk.dir=${NDK} >> local.properties
fi

if [ "$ENABLE_DEBUG" = "DEBUG" ]; then
    ASSEMBLE="compileDebugSources"
else
    ASSEMBLE="compileReleaseSources"
fi

if [ "$MVN_REPO" = "" ]; then
    gradle $ASSEMBLE -PVERSION=$VERSION_CODE -PMODULE_SRC=1
else
    gradle $ASSEMBLE -PMODULE_SRC=1
fi


cd ..
