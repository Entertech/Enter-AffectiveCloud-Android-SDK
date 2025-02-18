# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

cmake_minimum_required(VERSION 3.4.1)

include(CheckCXXCompilerFlag)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds it for you.
# Gradle automatically packages shared libraries with your APK.


include_directories(src/main/cpp/package/include)
include_directories(src/main/cpp/package/libs/NumCpp)
include_directories(src/main/cpp/package/libs/Eigen)
include_directories(src/main/cpp/package/source)

# 选项：不使用 Boost
option(NUMCPP_NO_USE_BOOST "Set to switch not use boost" ON)
add_definitions(-DNUMCPP_NO_USER_BOOST)

# 设置 C++17 标准
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# 输出 C++ 标准版本（可选，方便调试）
message(STATUS "Using C++ standard: ${CMAKE_CXX_STANDARD}")

if (NOT CMAKE_BUILD_TYPE)
    set(CMAKE_BUILD_TYPE "Release")
endif ()
#file(GLOB SOURCES_CPP "src/main/cpp/package/source/*.cpp")
file(GLOB SOURCES_CPP "src/main/cpp/package/source/*.cpp" "src/main/cpp/package/source/*.c" "src/main/cpp/package/source/AC/*.cpp" "src/main/cpp/package/source/DP/*.cpp" "src/main/cpp/package/source/BASIC/TOOL/*.cpp" "src/main/cpp/package/source/BASIC/ENTERDSP/*.cpp" "src/main/cpp/package/source/BASIC/ENTERAFFECTION/*.cpp" )
file(GLOB INCLUDE_H "src/main/cpp/package/inclue/*.h" )
file(GLOB INCLUDE_HPP "src/main/cpp/package/inclue/*.hpp")
add_library( # Sets the name of the library.
        affective

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        # Associated headers in the same location as their source
        # file are automatically included.
        src/main/cpp/package/native-lib.cpp

        # lib part
        src/main/cpp/package/libs/Eigen/Dense
#        src/main/cpp/libs/EnterEEG/EnterEEG/define/Device.cpp
#        src/main/cpp/libs/EnterEEG/EnterEEG/dsp/Preprocessor.cpp
#        src/main/cpp/libs/EnterEEG/EnterEEG/dsp/Processor.cpp
#        src/main/cpp/libs/EnterEEG/EnterEEG/network/keras.cpp
#
#
#        src/main/cpp/libs/EnterEEG/EnterEEG/io/Basic.cpp
#        src/main/cpp/libs/EnterEEG/EnterEEG/io/FileHeader.cpp
#        src/main/cpp/libs/EnterEEG/EnterEEG/io/NapAnalyzedData.cpp
#        src/main/cpp/libs/EnterEEG/EnterEEG/io/NapReportData.cpp
#
#        src/main/cpp/libs/EnterEEG/EnterEEG/io/Basic.h
#        src/main/cpp/libs/EnterEEG/EnterEEG/io/FileHeader.h
#        src/main/cpp/libs/EnterEEG/EnterEEG/io/NapAnalyzedData.h
#        src/main/cpp/libs/EnterEEG/EnterEEG/io/NapReportData.h

        # nap part
        ${SOURCES_CPP}
        ${INCLUDE_H}
        ${INCLUDE_HPP}
        src/main/cpp/package/public/Affective.h
        src/main/cpp/package/public/Define.h
        )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because system libraries are included in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in the
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        affective

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})
