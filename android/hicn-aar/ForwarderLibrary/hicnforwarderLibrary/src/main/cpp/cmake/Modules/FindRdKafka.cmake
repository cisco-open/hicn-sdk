# This find module helps find the RdKafka module. It exports the following variables:
# - RdKafka_INCLUDE_DIR : The directory where rdkafka.h is located.
# - RdKafka_LIBNAME : The name of the library, i.e. librdkafka.a, librdkafka.so, etc.
# - RdKafka_LIBRARY_PATH : The full library path i.e. <path_to_binaries>/${RdKafka_LIBNAME}
# - RdKafka_LIBNAME : The name of the cpplibrary, i.e. librdkafka++.a, librdkafka++.so, etc.
# - RdKafka_LIBRARY_PATH : The full library path i.e. <path_to_binaries>/${RdKafka_CPP_LIBNAME}
# - RdKafka::rdkafka : Imported library containing all above properties set.

if (CPPKAFKA_RDKAFKA_STATIC_LIB)
    set(RDKAFKA_PREFIX ${CMAKE_STATIC_LIBRARY_PREFIX})
    set(RDKAFKA_SUFFIX ${CMAKE_STATIC_LIBRARY_SUFFIX})
    set(RDKAFKA_LIBRARY_TYPE STATIC)
else()
    set(RDKAFKA_PREFIX ${CMAKE_SHARED_LIBRARY_PREFIX})
    set(RDKAFKA_SUFFIX ${CMAKE_SHARED_LIBRARY_SUFFIX})
    set(RDKAFKA_LIBRARY_TYPE SHARED)
endif()

set(RdKafka_LIBNAME ${RDKAFKA_PREFIX}rdkafka${RDKAFKA_SUFFIX})

find_path(RdKafka_INCLUDE_DIR
    NAMES librdkafka/rdkafka.h
    HINTS ${RdKafka_ROOT}/include
)

find_library(RdKafka_LIBRARY_PATH
    NAMES ${RdKafka_LIBNAME} rdkafka
    HINTS ${RdKafka_ROOT}/lib ${RdKafka_ROOT}/lib64
)

find_library(RdKafka_LIBRARY_CPP_PATH
    NAMES ${RdKafka_CPP_LIBNAME} rdkafka++
    HINTS ${RdKafka_ROOT}/lib ${RdKafka_ROOT}/lib64
)

# Check lib paths
if (CPPKAFKA_CMAKE_VERBOSE)
    get_property(FIND_LIBRARY_32 GLOBAL PROPERTY FIND_LIBRARY_USE_LIB32_PATHS)
    get_property(FIND_LIBRARY_64 GLOBAL PROPERTY FIND_LIBRARY_USE_LIB64_PATHS)
    message(STATUS "RDKAFKA search 32-bit library paths: ${FIND_LIBRARY_32}")
    message(STATUS "RDKAFKA search 64-bit library paths: ${FIND_LIBRARY_64}")
    message(STATUS "RdKafka_ROOT = ${RdKafka_ROOT}")
    message(STATUS "RdKafka_INCLUDE_DIR = ${RdKafka_INCLUDE_DIR}")
    message(STATUS "RdKafka_LIBNAME = ${RdKafka_LIBNAME}")
    message(STATUS "RdKafka_LIBRARY_PATH = ${RdKafka_LIBRARY_PATH}")
    message(STATUS "RdKafka_CPP_LIBNAME = ${RdKafka_CPP_LIBNAME}")
    message(STATUS "RdKafka_LIBRARY_CPP_PATH = ${RdKafka_LIBRARY_CPP_PATH}")
endif()


macro(parse lineinput major minor patch)
    string(REPLACE " " ";" line ${lineinput})
    list (GET line 3 version)
    string(SUBSTRING "${version}" 2 2 major)
    string(SUBSTRING "${version}" 4 2 minor)
    string(SUBSTRING "${version}" 6 2 patch)
endmacro()

set(RdKafka_FOUND False)
if (NOT "${RdKafka_INCLUDE_DIR}" STREQUAL "")
  set(RdKafka_FOUND True)
  file(READ "${RdKafka_INCLUDE_DIR}/librdkafka/rdkafka.h" rdkafka)
  string(REPLACE "\n" ";" rdkafka ${rdkafka})
  foreach(line ${rdkafka})
     if ("${line}" MATCHES "#define RD_KAFKA_VERSION")
       parse(${line} major minor patch)
       set(RdKafka_MAJOR "${major}")
       math(EXPR RdKafka_MAJOR "${RdKafka_MAJOR}")
       set(RdKafka_MINOR "${minor}")
       math(EXPR RdKafka_MINOR "${RdKafka_MINOR}")
       set(RdKafka_PATCH "${patch}")
       math(EXPR RdKafka_PATCH "${RdKafka_PATCH}")
     endif ()
  endforeach()
  set(RdKafka_VERSION "${RdKafka_MAJOR}.${RdKafka_MINOR}.${RdKafka_PATCH}")
endif ()

include(FindPackageHandleStandardArgs)
find_package_handle_standard_args(RdKafka
    REQUIRED_VARS
    RdKafka_LIBNAME
    RdKafka_LIBRARY_PATH
    RdKafka_LIBRARY_CPP_PATH
    RdKafka_INCLUDE_DIR
    VERSION_VAR
    RdKafka_VERSION
)

set(CONTENTS "#include <librdkafka/rdkafka.h>\n #if RD_KAFKA_VERSION >= ${RDKAFKA_MIN_VERSION_HEX}\n int main() { }\n #endif")
set(FILE_NAME ${CMAKE_CURRENT_BINARY_DIR}/rdkafka_version_test.cpp)
file(WRITE ${FILE_NAME} ${CONTENTS})

add_library(RdKafka::rdkafka ${RDKAFKA_LIBRARY_TYPE} IMPORTED GLOBAL)
if (UNIX AND NOT APPLE)
    set(RDKAFKA_DEPENDENCIES pthread rt ssl crypto dl z)
else()
    set(RDKAFKA_DEPENDENCIES pthread ssl crypto dl z)
endif()
set_target_properties(RdKafka::rdkafka PROPERTIES
        IMPORTED_NAME RdKafka
        IMPORTED_LOCATION "${RdKafka_LIBRARY_PATH};${RdKafka_LIBRARY_CPP_PATH}"
        INTERFACE_INCLUDE_DIRECTORIES "${RdKafka_INCLUDE_DIR}"
        INTERFACE_LINK_LIBRARIES "${RDKAFKA_DEPENDENCIES}")
message(STATUS "Found valid rdkafka version")
mark_as_advanced(
    RDKAFKA_LIBRARY
    RdKafka_INCLUDE_DIR
    RdKafka_LIBRARY_PATH
    RdKafka_LIBRARY_CPP_PATH
)

list(APPEND LIBRARIES
  ${RdKafka_LIBRARY_CPP_PATH}
  ${RdKafka_LIBRARY_PATH}
)