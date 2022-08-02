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
# Find the LibTRANSPORT libraries and includes
# This module sets:
#  LIBTRANSPORT_FOUND: True if Libconsumer-producer was found
#  LIBTRANSPORTR_LIBRARY:  The Libconsumer-producer library
#  LIBTRANSPORT_LIBRARIES:  The Libconsumer-producer library and dependencies
#  LIBTRANSPORT_INCLUDE_DIR:  The Libconsumer-producer include dir
#

set(LIBTRANSPORT_SEARCH_PATH_LIST
  ${LIBTRANSPORT_HOME}
  $ENV{LIBTRANSPORTHOME}
  /usr/local
  /opt
  /usr
)

find_path(LIBTRANSPORT_INCLUDE_DIR hicn/transport/config.h
  HINTS ${LIBTRANSPORT_SEARCH_PATH_LIST}
  PATH_SUFFIXES include
  DOC "Find the libtransport includes"
)

find_library(LIBTRANSPORT_LIBRARY
  NAMES hicntransport hicntransport-memif
  HINTS ${LIBTRANSPORT_SEARCH_PATH_LIST}
  PATH_SUFFIXES lib
  DOC "Find the libtransport libraries"
)

macro(parse lineinput returnValue)
    string(REPLACE "\"" "" line ${lineinput})
    string(REPLACE " " ";" line ${line})
    list (GET line 2 returnValue)
endmacro()

set(LIBTRANSPORT_FOUND False)
if (NOT "${LIBTRANSPORT_INCLUDE_DIR}" STREQUAL "")
  set(LIBTRANSPORT_FOUND True)
  file(READ "${LIBTRANSPORT_INCLUDE_DIR}/hicn/transport/config.h" transport)
  string(REPLACE "\n" ";" transport ${transport})
  foreach(line ${transport})
    if ("${line}" MATCHES "#define HICNTRANSPORT_VERSION_MAJOR")
      parse(${line} returnValue)
      set(LIBTRANSPORT_MAJOR "${returnValue}")
    endif ()
    if ("${line}" MATCHES "#define HICNTRANSPORT_VERSION_MINOR")
      parse(${line} returnValue)
      set(LIBTRANSPORT_MINOR "${returnValue}")
    endif ()
    if ("${line}" MATCHES "#define HICNTRANSPORT_VERSION_PATCH")
      parse(${line} returnValue)
      set(LIBTRANSPORT_PATCH "${returnValue}")
    endif ()
  endforeach()
  set(LIBTRANSPORT_VERSION "${LIBTRANSPORT_MAJOR}.${LIBTRANSPORT_MINOR}.${LIBTRANSPORT_PATCH}")
endif ()
set(LIBTRANSPORT_LIBRARIES ${LIBTRANSPORT_LIBRARY})
set(LIBTRANSPORT_INCLUDE_DIRS ${LIBTRANSPORT_INCLUDE_DIR})

include(FindPackageHandleStandardArgs)
find_package_handle_standard_args(Libtransport REQUIRED_VARS LIBTRANSPORT_LIBRARIES LIBTRANSPORT_INCLUDE_DIRS VERSION_VAR LIBTRANSPORT_VERSION)
