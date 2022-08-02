########################################
#
# Find the LibHicnHttpproxy libraries and includes
# This module sets:
# LIBHICNHTTPPROXY_FOUND: True if LHicnHttpproxy was found
# LIBHICNHTTPPROXY_LIBRARY: The LHicnHttpproxy library
# LIBHICNHTTPPROXY_LIBRARIES: The LHicnHttpproxy library and dependencies
# LIBHICNHTTPPROXY_INCLUDE_DIR: The LHicnHttpproxy include dir
#

set(LIBHICNHTTPPROXY_SEARCH_PATH_LIST
${LIBHICNHTTPPROXY_HOME}
$ENV{LIBHICNHTTPPROXYHOME}
$ENV{CCNX_HOME}
$ENV{PARC_HOME}
$ENV{FOUNDATION_HOME}
/usr/local/
/usr/local/ccnx
/usr/local/ccn
/usr/local
/opt
/usr
)

find_path(LIBHICNHTTPPROXY_INCLUDE_DIR hicn/http-proxy/http_proxy.h
HINTS ${LIBHICNHTTPPROXY_SEARCH_PATH_LIST}
PATH_SUFFIXES include
DOC "Find the lhicnhttpproxy includes")

find_library(LIBHICNHTTPPROXY_LIBRARY NAMES hicnhttpproxy
HINTS ${LIBHICNHTTPPROXY_SEARCH_PATH_LIST}
PATH_SUFFIXES lib
DOC "Find the lhicnhttpproxy libraries")

set(LIBHICNHTTPPROXY_LIBRARIES ${LIBHICNHTTPPROXY_LIBRARY})
set(LIBHICNHTTPPROXY_INCLUDE_DIRS ${LIBHICNHTTPPROXY_INCLUDE_DIR})

include(FindPackageHandleStandardArgs)
find_package_handle_standard_args(HicnHttpproxy DEFAULT_MSG LIBHICNHTTPPROXY_LIBRARY LIBHICNHTTPPROXY_INCLUDE_DIR)
