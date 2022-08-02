############################################################
# Distillery CMake Module
#
# This is a framework for Distillery Modules using cmake. 
#
# Modules can add themselves do distillery by calling the addCMakeModule
# function. A module called Foo would do the following:
#
# $(eval $(call addCmakeModule,Foo))
#
# Assumptions
# - The source for Foo is in git, located at: ${DISTILLERY_GITHUB_URL}/Foo
#   You can change this via a variable, see bellow.
# - The source can do an off-tree build.
# - The source is compiled via CMake



define addCMakeModule
$(eval $(call addModule,$1))

${$1_BUILD_DIR}/Makefile: ${$1_SOURCE_DIR}/CMakeLists.txt ${DISTILLERY_STAMP}
	    mkdir -p ${$1_BUILD_DIR};
	    echo ${$1_BUILD_DIR};
	    cd ${$1_BUILD_DIR}; \
	    DEPENDENCY_HOME=${DISTILLERY_EXTERN_DIR} \
	    ${SDK}/cmake/${ANDROID_CMAKE_REV_3_18}/bin/cmake \ # -DCMAKE_TOOLCHAIN_FILE=${NDK}/build/cmake/android.toolchain.cmake \
	    -DANDROID_TOOLCHAIN=clang -DANDROID_STL=c++_static \
	    -DANDROID_ABI=${ANDROID_ABI} \ #ANDROID_STANDALONE_TOOLCHAIN_SEARCH_PATH=${DISTILLERY_ROOT_DIR}/sdk/toolchain_${ABI} \
	    -DCMAKE_BUILD_TYPE=Release \
	    -DCMAKE_PREFIX_PATH=${DISTILLERY_INSTALL_DIR} \
	    -DCMAKE_FIND_ROOT_PATH=${DISTILLERY_INSTALL_DIR} \
	    -DANDROID_NATIVE_API_LEVEL=26 -DANDROID_API=ON -DINSTALL_HEADER=ON -DHAVE_FSETXATTR=OFF \
	    -DEVENT__LIBRARY_TYPE="STATIC" -DEVENT__DISABLE_TESTS=ON \
	    -DJSONCPP_WITH_WARNING_AS_ERROR=OFF \
	    -DBUILD_EXAMPLES=OFF -DBUILD_TESTS=OFF \
	    -DCMAKE_DEBUG_POSTFIX="" \
		-DDISABLE_SHARED_LIBRARIES=ON \
		-DDISABLE_EXECUTABLES=ON \
	    -DBUILD_SHARED_LIBS=OFF -DBUILD_APPS=ON -DBUILD_TESTING=OFF -DJSONCPP_WITH_TESTS=OFF \
	    -DHICNET=ON -DCMAKE_INSTALL_PREFIX=${DISTILLERY_INSTALL_DIR} ${$1_SOURCE_DIR} -DBUILD_SHARED_LIBS=0
${$1_SOURCE_DIR}/CMakeLists.txt:
		@echo "**option **1"
	    @$(MAKE) distillery.checkout.error

endef
