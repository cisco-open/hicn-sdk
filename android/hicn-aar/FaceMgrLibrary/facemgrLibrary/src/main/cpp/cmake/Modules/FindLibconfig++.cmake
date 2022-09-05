set(LIBCONFIG_SEARCH_PATH_LIST
  ${LIBCONFIG_HOME}
  $ENV{LIBCONFIG_HOME}
  /usr/local
  /opt
  /usr
)

find_path(LIBCONFIG_INCLUDE_DIR libconfig.h++
  HINTS ${LIBCONFIG_SEARCH_PATH_LIST}
  PATH_SUFFIXES include
  DOC "Find the libconfig include"
)

if (WIN32)
  if(CMAKE_SIZEOF_VOID_P EQUAL 8)
    find_library(LIBCONFIG_CPP_LIBRARIES NAMES libconfig++.lib
      HINTS ${LIBCONFIG_SEARCH_PATH_LIST}
      PATH_SUFFIXES lib/x64
      DOC "Find the libconfig libraries"
    )
  elseif(CMAKE_SIZEOF_VOID_P EQUAL 4)
    find_library(LIBCONFIG_CPP_LIBRARIES NAMES libconfig++.lib
      HINTS ${LIBCONFIG_SEARCH_PATH_LIST}
      PATH_SUFFIXES lib/x32
      DOC "Find the libconfig libraries"
    )
  endif()
else()
  find_library(LIBCONFIG_CPP_LIBRARY NAMES config++
    HINTS ${LIBCONFIG_SEARCH_PATH_LIST}
    PATH_SUFFIXES lib
    DOC "Find the libconfig++ libraries"
  )
endif()


macro(parse line returnValue)
    string(REPLACE " " ";" line ${line})
    list(LENGTH line list_len)
    math(EXPR list_last "${list_len} - 1")
    list (GET line ${list_last} returnValue)
endmacro()

set(LIBCONFIG_CPP_LIBRARIES ${LIBCONFIG_CPP_LIBRARY})
set(LIBCONFIG_INCLUDE_DIRS ${LIBCONFIG_INCLUDE_DIR})
set(Libconfig++_FOUND False)
if (NOT "${LIBCONFIG_INCLUDE_DIR}" STREQUAL "")
  set(Libconfig++_FOUND True)
  file(READ "${LIBCONFIG_INCLUDE_DIR}/libconfig.h++" libconfig)
  string(REPLACE "\n" ";" libconfig ${libconfig})
  foreach(line ${libconfig})
    if ("${line}" MATCHES "#define LIBCONFIGXX_VER_MAJOR")
      parse( ${line} returnValue)
      set(LIBCONFIG_MAJOR "${returnValue}")
    endif ()
    if ("${line}" MATCHES "#define LIBCONFIGXX_VER_MINOR")
      parse( ${line} returnValue)
      set(LIBCONFIG_MINOR "${returnValue}")
    endif ()
    if ("${line}" MATCHES "#define LIBCONFIGXX_VER_REVISION")
      parse( ${line} returnValue)
      set(LIBCONFIG_PATCH "${returnValue}")
    endif ()
  endforeach()
  set(LIBCONFIG_VERSION "${LIBCONFIG_MAJOR}.${LIBCONFIG_MINOR}.${LIBCONFIG_PATCH}")
endif ()

include(FindPackageHandleStandardArgs)
find_package_handle_standard_args(Libconfig++ REQUIRED_VARS LIBCONFIG_CPP_LIBRARIES LIBCONFIG_INCLUDE_DIRS VERSION_VAR LIBCONFIG_VERSION)

mark_as_advanced(LIBCONFIG_CPP_LIBRARIES LIBCONFIG_INCLUDE_DIRS)
