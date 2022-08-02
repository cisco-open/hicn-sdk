########################################
#
# Find the hcin libraries and includes
# This module sets:
#  HICN_FOUND: True if hicn was found
#  HICN_LIBRARY:  The hicn library
#  HICN_LIBRARIES:  The hicn library and dependencies
#  HCIN_INCLUDE_DIR:  The hicn include dir
#

set(HICN_SEARCH_PATH_LIST
  ${HICN_HOME} 
  $ENV{HICN_HOME} 
  $ENV{FOUNDATION_HOME} 
  /usr/local 
  /opt
  /usr 
  )

find_path(HICN_INCLUDE_DIR hicn.h
  HINTS ${HICN_SEARCH_PATH_LIST}
  PATH_SUFFIXES include
  DOC "Find the hicn includes" )
	  
find_library(HICN_LIBRARY NAMES hicn
  HINTS ${HICN_SEARCH_PATH_LIST}
  PATH_SUFFIXES lib
  DOC "Find the hicn libraries" )

set(HICN_LIBRARIES ${HICN_LIBRARY})
set(HICN_INCLUDE_DIRS ${HICN_INCLUDE_DIR})

include(FindPackageHandleStandardArgs)
find_package_handle_standard_args(hicn  DEFAULT_MSG HICN_LIBRARY HICN_INCLUDE_DIR)
