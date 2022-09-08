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

FROM ubuntu:22.04 as android-sdk-dep

ENV TZ=Europe/Paris
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN apt update && apt install -y build-essential openjdk-11-jdk git autoconf libtool wget unzip python3 
RUN wget -c https://services.gradle.org/distributions/gradle-7.4.2-bin.zip -P /tmp \
    && unzip -d /opt/gradle /tmp/gradle-7.4.2-bin.zip \
    && rm /tmp/gradle-7.4.2-bin.zip \
    && echo export GRADLE_HOME=/opt/gradle/gradle-7.4.2 >> /etc/profile.d/gradle.sh \
    && echo export PATH=/opt/gradle/gradle-7.4.2/bin:${PATH} >> /etc/profile.d/gradle.sh \
    && cat /etc/profile.d/gradle.sh \
    && chmod +x /etc/profile.d/gradle.sh \
    && . /etc/profile.d/gradle.sh \
    && gradle --version
ENV PATH=/opt/gradle/gradle-7.4.2/bin:${PATH} 

COPY . /android-sdk

WORKDIR /android-sdk
RUN make init_sdk && ./compileAll.sh && rm -rf sdk/commandlinetools*.zip && bash compileDep.sh && rm -rf src build*

FROM android-sdk-dep as android-sdk-build
RUN gradle --version
RUN echo sdk.dir=`pwd`/sdk > CommonLibrary/local.properties && cd CommonLibrary && gradle compileReleaseSources
RUN make android_facemgrlibraryaar
RUN make android_forwarderlibraryaar
