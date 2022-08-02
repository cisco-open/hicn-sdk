# Copyright (c) 2017-2019 Cisco and/or its affiliates.
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

########################################
#
# Find the hcin libraries and includes
# This module sets:
#  MODERN_CPP_KAFKA_FOUND: True if modern-cpp-kafka was found
#  MODERN_CPP_KAFKA_INCLUDE_DIR: The modern-cpp-kafka include dir
#

set(MODERN_CPP_KAFKA_SEARCH_PATH_LIST
  ${MODERN_CPP_KAFKA_HOME}
  $ENV{MODERN_CPP_KAFKA_HOME}
  /usr/local
  /opt
  /usr
)

find_path(MODERN_CPP_KAFKA_INCLUDE_DIR kafka/KafkaClient.h
  HINTS ${MODERN_CPP_KAFKA_SEARCH_PATH_LIST}
  PATH_SUFFIXES include
  DOC "Find the modern-cpp-kafka includes"
)

set(MODERN_CPP_KAFKA_INCLUDE_DIRS ${MODERN_CPP_KAFKA_INCLUDE_DIR})

include(FindPackageHandleStandardArgs)
find_package_handle_standard_args(ModernCppKafka
  REQUIRED_VARS MODERN_CPP_KAFKA_INCLUDE_DIRS
)