cmake_minimum_required(VERSION 3.10.2)

add_library(
    dict_engine
    SHARED
    dict_engine.cpp
    trie.cpp
)

find_library(log-lib log)
find_library(sqlite3-lib sqlite3)

target_link_libraries(
    dict_engine
    ${log-lib}
    ${sqlite3-lib}
)