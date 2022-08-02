# Copyright (c) 2020 Cisco and/or its affiliates.
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

##################################
# Download Modern CPP Kafka

include(FetchContent)

FetchContent_Declare(
  modernCppKafka
  GIT_REPOSITORY https://github.com/morganstanley/modern-cpp-kafka.git
  GIT_TAG v2021.10.15
  CONFIGURE_COMMAND  ""
  BUILD_COMMAND ""
  INSTALL_COMMAND ""
)

FetchContent_Populate(modernCppKafka)

FetchContent_GetProperties(modernCppKafka)
message (STATUS "Modern Cpp Kafka include dir: ${moderncppkafka_SOURCE_DIR}/include")

set(MODERN_CPP_KAFKA_INCLUDE_DIRS ${moderncppkafka_SOURCE_DIR}/include)

list(APPEND THIRD_PARTY_INCLUDE_DIRS
  ${MODERN_CPP_KAFKA_INCLUDE_DIRS}
)
