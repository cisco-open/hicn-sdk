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
#  HICN_PROXY_FOUND: True if hicn was found
#  HICN_PROXY_LIBRARY:  The hicn library
#  HICN_PROXY_LIBRARIES:  The hicn library and dependencies
#  HICN_PROXY_INCLUDE_DIR:  The hicn include dir
#

set(HICN_HPROXY_SEARCH_PATH_LIST
    ${HICN_HPROXY_HOME}
    $ENV{HICN_HPROXY_HOME}
    /usr/local
    /opt
    /usr
    /include
)

find_path(HICN_HPROXY_INCLUDE_DIR hicn/hproxy/hproxy_about.h
        HINTS ${HICN_HPROXY_SEARCH_PATH_LIST}
        PATH_SUFFIXES include
        DOC "Find the hproxy includes"
        )

find_library(HICN_HPROXY_LIBRARY NAMES hproxy
        HINTS ${HICN_HPROXY_SEARCH_PATH_LIST}
        PATH_SUFFIXES lib
        DOC "Find the hproxy libraries"
        )

set(HICN_HPROXY_LIBRARIES ${HICN_HPROXY_LIBRARY})
set(HICN_HPROXY_INCLUDE_DIRS ${HICN_HPROXY_INCLUDE_DIR})

include(FindPackageHandleStandardArgs)
find_package_handle_standard_args(Hproxy DEFAULT_MSG HICN_HPROXY_LIBRARIES HICN_HPROXY_INCLUDE_DIRS)
