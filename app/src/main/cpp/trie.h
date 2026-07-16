#ifndef DICT_TRIE_H_
#define DICT_TRIE_H_

#include <string>
#include <vector>
#include "darts.h"

namespace DictTrie {

struct PinyinEntry {
    std::string pinyin;
    int startId;
    int endId;
};

struct PinyinMatchResult {
    std::string pinyin;
    int startId;
    int endId;
    int matchLength;
};

/**
 * 从拼音条目列表构建 Double-Array Trie
 * entries 必须已按 pinyin 排序
 */
bool buildFromPinyinEntries(const std::vector<PinyinEntry>& entries);

/**
 * 保存已构建的 Trie 到二进制文件
 */
bool saveToFile(const std::string& path);

/**
 * 从二进制文件加载 Trie
 */
bool loadFromFile(const std::string& path);

/**
 * 前缀搜索: 返回所有匹配 pinyin 前缀的 PinyinMatchResult
 */
std::vector<PinyinMatchResult> prefixSearch(const std::string& prefix, int maxResults);

/**
 * 设置拼音条目列表 (从外部加载, 如从 dict.db 读取)
 * 用于 loadFromFile 后补充 entries, 使 prefixSearch 能正常工作
 */
void setPinyinEntries(const std::vector<PinyinEntry>& entries);

bool isLoaded();
void clear();
int getTrieSize();

}  // namespace DictTrie

#endif  // DICT_TRIE_H_