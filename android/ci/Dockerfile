 #############################################################################
 # Copyright (c) 2022 Cisco and/or its affiliates.
 # Licensed under the Apache License, Version 2.0 (the "License");
 # you may not use this file except in compliance with the License.
 # You may obtain a copy of the License at:
 #
 #     http://www.apache.org/licenses/LICENSE-2.0
 #
 # Unless required by applicable law or agreed to in writing, software
 # distributed under the License is distributed on an "AS IS" BASIS,
 # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 # See the License for the specific language governing permissions and
 # limitations under the License.
 ##############################################################################

ARG BASE_IMAGE=ubuntu:2204
FROM $BASE_IMAGE

MAINTAINER Angelo Mantellini <manangel@cisco.com>

ARG GITHUB_USERNAME
ARG GITHUB_PASSWORD
ARG GITHUB_MVN_REPO
ARG VERSION
ENV A_USERNAME=$GITHUB_USERNAME
ENV A_PASSWORD=$GITHUB_PASSWORD
ENV GITHUB_MVN_REPO=$GITHUB_MVN_REPO
ENV VERSION=$VERSION

COPY . /android-sdk

WORKDIR /android-sdk
RUN ls
RUN pwd
RUN make init_sdk && ./compileAll.sh && rm -rf sdk/commandlinetools*.zip && bash compileDep.sh && rm -rf src build*

FROM android-sdk-dep as android-sdk-build
RUN gradle --version
RUN echo sdk.dir=`pwd`/sdk > CommonLibrary/local.properties && cd CommonLibrary && gradle compileReleaseSources
RUN make android_facemgrlibraryaar
RUN make android_forwarderlibraryaar

FROM android-sdk-dep AS android-sdk-publish
RUN echo sdk.dir=`pwd`/sdk > CommonLibrary/local.properties && cd CommonLibrary \ 
    && gradle publish -PA_USER=${A_USERNAME} -PA_PASSWORD=${A_PASSWORD} -PVERSION_NUMBER=${VERSION} -PMODULE_SRC='0' -PMVN_REPO=${GITHUB_MVN_REPO}
RUN echo sdk.dir=`pwd`/sdk > hicn-aar/FaceMgrLibrary/local.properties && cd hicn-aar && cd FaceMgrLibrary \
    && gradle publish -PA_USER=${A_USERNAME} -PA_PASSWORD=${A_PASSWORD} -PVERSION_NUMBER=${VERSION} -PMODULE_SRC='0' -PMVN_REPO=${GITHUB_MVN_REPO}
RUN echo sdk.dir=`pwd`/sdk > hicn-aar/ForwarderLibrary/local.properties && cd hicn-aar && cd ForwarderLibrary \
    && gradle publish -PA_USER=${A_USERNAME} -PA_PASSWORD=${A_PASSWORD} -PVERSION_NUMBER=${VERSION} -PMODULE_SRC='0' -PMVN_REPO=${GITHUB_MVN_REPO}
