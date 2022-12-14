# Copyright (c) 2019 Cisco and/or its affiliates.
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
project(FaceMgrLibrary)
cmake_minimum_required(VERSION 3.4.1)
set(CMAKE_CXX_STANDARD 14)

set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} "${CMAKE_SOURCE_DIR}/cmake/Modules/")
option(EVHTP_DISABLE_EVTHR "Disable find libevent_pthread" OFF)

set_property(GLOBAL PROPERTY USE_FOLDERS ON)
set(CMAKE_FIND_ROOT_PATH "${CMAKE_CURRENT_SOURCE_DIR}/../../../../../../usr_${CMAKE_SYSTEM_PROCESSOR};${CMAKE_FIND_ROOT_PATH}")
find_package(OpenSSL REQUIRED)
find_package(LibEvent REQUIRED)
find_package(Libhicn REQUIRED)
find_package(Hicnlight REQUIRED)
find_package(Libconfig REQUIRED)
find_package(Libhicnctrl REQUIRED)
find_package(Libfacemgr REQUIRED)
find_package(Threads REQUIRED)
find_package(Libtransport REQUIRED)

include_directories(${CISCOSAFEC_INCLUDE_DIR})
include_directories(${LIBHICNLIGHT_INCLUDE_DIRS})

include_directories(${LIBTRANSPORT_INCLUDE_DIRS})

add_library(facemgr-wrapper SHARED facemgr-wrapper.c)

target_include_directories(facemgr-wrapper PUBLIC ${LIBHICNCTRL_INCLUDE_DIRS})

find_library(log-lib log)

target_link_libraries(facemgr-wrapper
        ${log-lib}
        ${LIBFACEMGR_LIBRARIES}
        ${LIBHICNCTRL_LIBRARIES}
        ${LIBHICN_LIBRARY}
        ${LIBEVENT_LIBRARIES}
        ${LIBCONFIG_LIBRARY}
        ${OPENSSL_LIBRARIES}
        )

