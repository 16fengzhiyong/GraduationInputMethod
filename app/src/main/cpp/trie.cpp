#include "trie.h"

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

    Darts::DoubleArray::result_pair_type tmp[256];
    size_t num = g_trie.commonPrefixSearch(prefix.c_str(), tmp, 256, prefix.size());
    if (num == 0 || num > 256) {
        if (num > 256) num = 256;
    }

    for (size_t i = 0; i < num && (int)results.size() < maxResults; i++) {
        int entryIdx = tmp[i].value;
        if (entryIdx < 0 || entryIdx >= (int)g_pinyin_entries.size()) continue;

        PinyinMatchResult r;
        r.pinyin = g_pinyin_entries[entryIdx].pinyin;
        r.startId = g_pinyin_entries[entryIdx].startId;
        r.endId = g_pinyin_entries[entryIdx].endId;
        r.matchLength = tmp[i].length;
        results.push_back(r);
    }

    return results;
}

bool isLoaded() {
    return g_trie_loaded;
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