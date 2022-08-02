########################################
#
# Find the LibHicnFwd libraries and includes
# This module sets:
# LIBHICNFWD_FOUND: True if Libhicnfwd was found
# LIBHICNFWD_LIBRARY: The Libhicnfwd library
# LIBHICNFWD_LIBRARIES: The Libhicnfwd library and dependencies
# LIBHICNFWD_INCLUDE_DIR: The Libhicnfwd include dir
#

set(LIBHICNFWD_SEARCH_PATH_LIST
${LIBHICNFWD_HOME}
$ENV{LIBHICNFWDHOME}
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

find_path(LIBHICNFWD_INCLUDE_DIR hicn-forwarder/hicnFwd_About.h
HINTS ${LLIBHICNFWD_SEARCH_PATH_LIST}
PATH_SUFFIXES include
DOC "Find the libhicnfwd includes")

find_library(LIBHICNFWD_LIBRARY NAMES hicfwd
HINTS ${LIBHICNFWD_SEARCH_PATH_LIST}
PATH_SUFFIXES lib
DOC "Find the libhicnfwd libraries")

set(LIBHICNFWD_LIBRARIES ${LIBHICNFWD_LIBRARY})
set(LIBHICNFWD_INCLUDE_DIRS ${LIBHICNFWD_INCLUDE_DIR})

include(FindPackageHandleStandardArgs)
find_package_handle_standard_args(Libhicnfwd DEFAULT_MSG LIBHICNFWD_LIBRARY LIBHICNFWD_INCLUDE_DIR)
