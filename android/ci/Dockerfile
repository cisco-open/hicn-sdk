FROM ubuntu:20.04 as android-sdk-dep

ENV TZ=Europe/Paris
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN apt update && apt install -y build-essential openjdk-11-jdk git autoconf libtool wget unzip python3 gradle

COPY android /android-sdk

WORKDIR /android-sdk
RUN make init_sdk && ./compileAll.sh && rm -rf sdk/commandlinetools*.zip && bash compileDep.sh && rm -rf src build*

FROM android-sdk-dep as android-sdk-build

RUN echo sdk.dir=`pwd`/sdk > CommonLibrary/local.properties && cd CommonLibrary && ./gradlew
RUN echo sdk.dir=`pwd`/sdk > hicn-aar/FaceMgrLibrary/local.properties && echo ndk.dir=`pwd`/sdk/ndk/${ANDROID_NDK_VERSION} >> hicn-aar/FaceMgrLibrary/local.properties && cd hicn-aar/FaceMgrLibrary && gradlew
RUN echo sdk.dir=`pwd`/sdk > hicn-aar/ForwarderLibrary/local.properties && echo ndk.dir=`pwd`/sdk/ndk/${ANDROID_NDK_VERSION} >> hicn-aar/ForwarderLibrary/local.properties && cd hicn-aar/ForwarderLibrary && gradlew

#FROM android-sdk-build as android-sdk-publish


# RUN VERSION=`git describe --abbrev=0` && cd CommonLibrary
