cmake_minimum_required(VERSION 3.22.1)
project("toollauncher")
set(ANDROID_PLATFORM 21)
set(ANDROID_ABI armeabi-v7a)
set(NDK_TOOLCHAIN_VERSION clang)
set(CMAKE_ANDROID_ARM_NEON TRUE)
set(CMAKE_ANDROID_ARM_MODE TRUE)

if(CMAKE_BUILD_TYPE STREQUAL "Release")
    add_definitions(-DNDEBUG)
endif()
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

add_library(substrate SHARED
    substrate/hde64.c
    substrate/SubstrateDebug.cpp
    substrate/SubstratePosixMemory.cpp
    substrate/SymbolFinder.cpp
    substrate/SubstrateHook.cpp
)

target_include_directories(substrate PUBLIC
    ${CMAKE_CURRENT_SOURCE_DIR}/substrate
)

target_compile_options(substrate PRIVATE
    -ffast-math
    -fPIC
)

target_compile_definitions(substrate PRIVATE
    ANDROID=1
)

add_library(toollauncher SHARED
    main.cpp
    Pointer.cpp
    API/Instrument.cpp
    Local/PlayerLocal.cpp
)

target_include_directories(toollauncher PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/include
    ${CMAKE_CURRENT_SOURCE_DIR}/substrate
)

target_compile_options(toollauncher PRIVATE
    -ffast-math
    -fPIC
)

target_compile_definitions(toollauncher PRIVATE
    ANDROID=1
    BL_WITH_UNDEFINED_SYMS=no
)

target_link_libraries(toollauncher
    substrate
    log
    android
    GLESv1_CM
    GLESv2
    -L${CMAKE_SOURCE_DIR}/libs/arm7
    -lminecraftpe
    -lfmod
)

target_link_libraries(substrate
    log
)

add_dependencies(toollauncher substrate)

add_custom_command(TARGET toollauncher POST_BUILD
    COMMAND ${CMAKE_COMMAND} -E copy
    ${CMAKE_SOURCE_DIR}/libs/arm7/libfmod.so
    ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libfmod.so
    COMMAND ${CMAKE_COMMAND} -E copy
    ${CMAKE_SOURCE_DIR}/libs/arm7/libminecraftpe.so
    ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libminecraftpe.so
)