cmake_minimum_required(VERSION 3.15)
project(VersionVault VERSION 1.0.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

find_package(OpenSSL REQUIRED)

include_directories(${CMAKE_SOURCE_DIR}/src/core)

set(CORE_SOURCES
    src/core/FileObject.cpp
    src/core/ObjectStore.cpp
    src/core/DiffEngine.cpp
)

add_library(vv_core SHARED ${CORE_SOURCES})
target_link_libraries(vv_core OpenSSL::SSL OpenSSL::Crypto)
target_include_directories(vv_core PUBLIC ${CMAKE_SOURCE_DIR}/src/core)

install(TARGETS vv_core
    LIBRARY DESTINATION lib
    ARCHIVE DESTINATION lib
)

install(DIRECTORY src/core/
    DESTINATION include/versionvault
    FILES_MATCHING PATTERN "*.h"
)