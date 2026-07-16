#include "trie.h"
#include <algorithm>

namespace DictTrie {

static Darts::DoubleArray g_trie;
static std::vector<PinyinEntry> g_pinyin_entries;
static bool g_trie_loaded = false;

bool buildFromPinyinEntries(const std::vector<PinyinEntry>& entries) {
    if (entries.empty()) return false;

    g_pinyin_entries = entries;

    std::vector<const char*> keys(entries.size());
    std::vector<std::size_t> lengths(entries.size());
    std::vector<Darts::DoubleArray::value_type> values(entries.size());

    for (size_t i = 0; i < entries.size(); i++) {
        keys[i] = entries[i].pinyin.c_str();
        lengths[i] = entries[i].pinyin.size();
        values[i] = static_cast<Darts::DoubleArray::value_type>(i);
    }

    g_trie.clear();

    try {
        int ret = g_trie.build(entries.size(), &keys[0], &lengths[0], &values[0]);
        if (ret != 0) {
            return false;
        }
        g_trie_loaded = true;
    } catch (const Darts::Details::Exception& ex) {
        return false;
    }

    return true;
}

bool saveToFile(const std::string& path) {
    if (!g_trie_loaded) return false;
    return g_trie.save(path.c_str()) == 0;
}

bool loadFromFile(const std::string& path) {
    g_trie.clear();
    try {
        int ret = g_trie.open(path.c_str());
        if (ret != 0) return false;
        g_trie_loaded = true;
    } catch (const Darts::Details::Exception& ex) {
        return false;
    }
    return true;
}

std::vector<PinyinMatchResult> prefixSearch(const std::string& prefix, int maxResults) {
    std::vector<PinyinMatchResult> results;

    if (!g_trie_loaded || prefix.empty() || g_pinyin_entries.empty()) {
        return results;
    }

    // 二分查找第一个 pinyin >= prefix 的条目
    // g_pinyin_entries 已按 pinyin 字典序排列 (loadPinyinEntriesFromDB ORDER BY pinyin ASC)
    auto it = std::lower_bound(g_pinyin_entries.begin(), g_pinyin_entries.end(), prefix,
        [](const PinyinEntry& entry, const std::string& pref) {
            return entry.pinyin < pref;
        });

    // 线性收集所有以 prefix 开头的条目, 最多 maxResults 个
    for (; it != g_pinyin_entries.end() && (int)results.size() < maxResults; ++it) {
        // compare(0, n, s) 检查前 n 个字符是否等于 s
        if (it->pinyin.compare(0, prefix.size(), prefix) != 0) {
            break;  // 不再以 prefix 开头, 停止
        }
        PinyinMatchResult r;
        r.pinyin = it->pinyin;
        r.startId = it->startId;
        r.endId = it->endId;
        r.matchLength = (int)it->pinyin.size();
        results.push_back(r);
    }

    return results;
}

bool isLoaded() {
    return g_trie_loaded;
}

void setPinyinEntries(const std::vector<PinyinEntry>& entries) {
    g_pinyin_entries = entries;
}

void clear() {
    g_trie.clear();
    g_pinyin_entries.clear();
    g_trie_loaded = false;
}

int getTrieSize() {
    return g_trie_loaded ? static_cast<int>(g_trie.size()) : 0;
}

}  // namespace DictTrie