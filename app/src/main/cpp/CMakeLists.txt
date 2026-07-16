cmake_minimum_required(VERSION 3.10.2)

add_library(
    dict_engine
    SHARED
    dict_engine.cpp
    trie.cpp
    sqlite3.c
)

find_library(log-lib log)

target_compile_options(dict_engine PRIVATE -DSQLITE_THREADSAFE=2 -DSQLITE_OMIT_DEPRECATED)
set_source_files_properties(sqlite3.c PROPERTIES COMPILE_FLAGS "-w -Os")

target_include_directories(dict_engine PRIVATE ${CMAKE_CURRENT_SOURCE_DIR})

target_link_libraries(
    dict_engine
    ${log-lib}
)