########################################
#
# Find the Zf Log libraries and includes
# This module sets:
#  LIBZF_LOG_FOUND: True if libzf_log was found
#  LIBZF_LOG_LIBRARY:  The libzf_log library
#  LIBZF_LOG_LIBRARIES:  The libzf_log library and dependencies
#  LIBZF_LOG_INCLUDE_DIR:  The libzf_log include dir
#

set(LIBZF_LOG_SEARCH_PATH_LIST
        ${LIBZF_LOG_HOME}
        $ENV{LIBZF_LOG_HOME}
        $ENV{CCNX_HOME}
        $ENV{PARC_HOME}
        $ENV{FOUNDATION_HOME}
        /usr/local/http-server
        /usr/local/ccnx
        /usr/local/ccn
        /usr/local
        /opt
        /usr
        )

find_path(LIBZF_LOG_INCLUDE_DIR zf_log.h
        HINTS ${LIBZF_LOG_SEARCH_PATH_LIST}
        PATH_SUFFIXES include
        DOC "Find the zf_log includes")

find_library(LIBZF_LOG_LIBRARY NAMES zf_log
        HINTS ${LIBZF_LOG_SEARCH_PATH_LIST}
        PATH_SUFFIXES lib
        DOC "Find the zf_log libraries")

set(LIBZF_LOG_LIBRARIES ${LIBZF_LOG_LIBRARY})
set(LIBZF_LOG_INCLUDE_DIRS ${LIBZF_LOGR_INCLUDE_DIR})

include(FindPackageHandleStandardArgs)
find_package_handle_standard_args(libzf_log DEFAULT_MSG LIBZF_LOG_LIBRARY LIBZF_LOG_INCLUDE_DIR)
