# For more information about using CMake with Android Studio, read the

# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC

# or SHARED, and provides the relative paths to its source code.

# You can define multiple libraries, and CMake builds them for you.

# Gradle automatically packages shared libraries with your APK.
set(libs "${CMAKE_SOURCE_DIR}/src/main/jniLibs")
include_directories(${CMAKE_SOURCE_DIR}/src/main/cpp)
include_directories(${CMAKE_SOURCE_DIR}/src/main/cpp/include)

add_library(
        opencv_java3

        SHARED

        IMPORTED)

set_target_properties(

        opencv_java3

        PROPERTIES IMPORTED_LOCATION

        "${libs}/${ANDROID_ABI}/libopencv_java3.so")

add_library(libopencv_java SHARED IMPORTED)
set_target_properties(libopencv_java PROPERTIES
        IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_java.so")

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11 -fexceptions -frtti")

add_library(
        pic_fix
        SHARED
        src/main/cpp/pic_fix.cpp
)
add_library(
        haar_detect
        SHARED
        src/main/cpp/haar_detect.cpp
)

add_library( # Sets the name of the library.

        detection_based_tracker

        # Sets the library as a shared library.

        SHARED

        # Provides a relative path to your source file(s).
        src/main/cpp/DetectionBasedTracker_jni.cpp)


# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.
find_library( # Sets the name of the path variable.
        log-lib
        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)
# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.
target_link_libraries( # Specifies the target library.
        detection_based_tracker

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
        opencv_java3
        libopencv_java
        )
target_link_libraries( # Specifies the target library.
        haar_detect

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
        opencv_java3
        libopencv_java
        )
target_link_libraries( # Specifies the target library.
        pic_fix

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
        opencv_java3
        libopencv_java
        )