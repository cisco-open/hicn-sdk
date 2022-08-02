########################################
#
# Find the LibHicnFwd libraries and includes
# This module sets:
# LIBHICNFWD_FOUND: True if Libhicnfwd was found
# LIBHICNFWD_LIBRARY: The Libhicnfwd library
# LIBHICNFWD_LIBRARIES: The Libhicnfwd library and dependencies
# LIBHICNFWD_INCLUDE_DIR: The Libhicnfwd include dir
#

set(LIBHICNLIGHT_SEARCH_PATH_LIST
${LIBHICNLIGHT_HOME}
$ENV{LIBHICNLIGHTHOME}
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

find_path(LIBHICNLIGHT_INCLUDE_DIR hicn/core/forwarder.h
HINTS ${LIBHICNLIGHT_SEARCH_PATH_LIST}
PATH_SUFFIXES include
DOC "Find the libhicnlight includes")

find_library(LIBHICNLIGHT_LIBRARY NAMES hicn-light
HINTS ${LIBHICNLIGHT_SEARCH_PATH_LIST}
PATH_SUFFIXES lib
DOC "Find the libhicnlight libraries")

set(LIBHICNLIGHT_LIBRARIES ${LIBHICNLIGHT_LIBRARY})
set(LIBHICNLIGHT_INCLUDE_DIRS ${LIBHICNLIGHT_INCLUDE_DIR})

include(FindPackageHandleStandardArgs)
find_package_handle_standard_args(Libhicnfwd DEFAULT_MSG LIBHICNLIGHT_LIBRARY LIBHICNLIGHT_INCLUDE_DIR)
