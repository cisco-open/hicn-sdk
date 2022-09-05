#!/bin/bash
set -e
VERSION_CODE=1
GITHUB_USER=""
GITHUB_TOKEN=""
MVN_REPO=""
while getopts ":v:u:t:r:" opt; do
  case $opt in
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


cd aar_modules/CommonLibrary
if [ ! -f local.properties ]; then
	echo sdk.dir=${SDK} > local.properties
fi


if [ "$MVN_REPO" = "" ]; then
    ./gradlew publish -PVERSION=$VERSION_CODE -PGITHUB_USER=$GITHUB_USER -PGITHUB_TOKEN=$GITHUB_TOKEN
else
    ./gradlew publish -PVERSION=$VERSION_CODE -PGITHUB_USER=$GITHUB_USER -PGITHUB_TOKEN=$GITHUB_TOKEN -PMVN_REPO=$MVN_REPO
fi

echo "aar published"
cd ..
