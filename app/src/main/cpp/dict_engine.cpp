#include <jni.h>
#include <string>
#include <vector>
#include <algorithm>
#include <cstring>
#include <cstdlib>
#include <cstdio>
#include <map>
#include <android/log.h>

#include "darts.h"
#include "trie.h"

// SQLite3 C API (NDK built-in)
#include "sqlite3.h"

#define LOG_TAG "DictEngine"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static const char* kInternalFilesPath = "/data/data/com.nuc.omeletteinputmethod/files";

static sqlite3* g_dict_db = nullptr;
static sqlite3* g_bigram_db = nullptr;
static bool g_initialized = false;

struct CategoryDbInfo {
    std::string category;
    sqlite3* db;
};
static std::vector<CategoryDbInfo> g_category_dbs;

static std::string g_trieDatPath;

static void closeDatabases() {
    if (g_dict_db) {
        sqlite3_close(g_dict_db);
        g_dict_db = nullptr;
    }
    if (g_bigram_db) {
        sqlite3_close(g_bigram_db);
        g_bigram_db = nullptr;
    }
    for (auto& info : g_category_dbs) {
        if (info.db) {
            sqlite3_close(info.db);
        }
    }
    g_category_dbs.clear();
}

static bool openDatabase(const std::string& path, sqlite3** outDb) {
    int rc = sqlite3_open_v2(path.c_str(), outDb,
        SQLITE_OPEN_READONLY | SQLITE_OPEN_FULLMUTEX, nullptr);
    if (rc != SQLITE_OK) {
        LOGE("Failed to open db %s: %s", path.c_str(), sqlite3_errmsg(*outDb));
        if (*outDb) sqlite3_close(*outDb);
        *outDb = nullptr;
        return false;
    }
    sqlite3_exec(*outDb, "PRAGMA cache_size=4000", nullptr, nullptr, nullptr);
    sqlite3_exec(*outDb, "PRAGMA mmap_size=268435456", nullptr, nullptr, nullptr);
    return true;
}

static std::string jstringToUtf8(JNIEnv* env, jstring jstr) {
    if (!jstr) return "";
    const char* utf = env->GetStringUTFChars(jstr, nullptr);
    std::string result(utf);
    env->ReleaseStringUTFChars(jstr, utf);
    return result;
}

static jstring utf8ToJstring(JNIEnv* env, const std::string& str) {
    return env->NewStringUTF(str.c_str());
}

static bool loadPinyinEntriesFromDB(std::vector<DictTrie::PinyinEntry>& entries) {
    if (!g_dict_db) return false;

    const char* sql =
        "SELECT id, pinyin, freq FROM dict ORDER BY pinyin ASC, freq DESC";
    sqlite3_stmt* stmt = nullptr;
    int rc = sqlite3_prepare_v2(g_dict_db, sql, -1, &stmt, nullptr);
    if (rc != SQLITE_OK) {
        LOGE("SQL prepare failed: %s", sqlite3_errmsg(g_dict_db));
        return false;
    }

    std::string currentPinyin;
    int startId = 0;
    int count = 0;

    while (sqlite3_step(stmt) == SQLITE_ROW) {
        int id = sqlite3_column_int(stmt, 0);
        const char* py = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 1));

        if (!py) continue;

        std::string pinyin(py);

        if (count == 0) {
            currentPinyin = pinyin;
            startId = id;
            count = 1;
            continue;
        }

        if (pinyin != currentPinyin) {
            DictTrie::PinyinEntry entry;
            entry.pinyin = currentPinyin;
            entry.startId = startId;
            entry.endId = id - 1;
            entries.push_back(entry);

            currentPinyin = pinyin;
            startId = id;
            count = 1;
        } else {
            count++;
        }
    }

    if (count > 0) {
        DictTrie::PinyinEntry entry;
        entry.pinyin = currentPinyin;
        entry.startId = startId;

        int lastId = startId;
        sqlite3_stmt* lastStmt = nullptr;
        const char* lastSql = "SELECT MAX(id) FROM dict";
        if (sqlite3_prepare_v2(g_dict_db, lastSql, -1, &lastStmt, nullptr) == SQLITE_OK) {
            if (sqlite3_step(lastStmt) == SQLITE_ROW) {
                lastId = sqlite3_column_int(lastStmt, 0);
            }
            sqlite3_finalize(lastStmt);
        }
        entry.endId = lastId;
        entries.push_back(entry);
    }

    sqlite3_finalize(stmt);
    return true;
}

struct CandidateItem {
    std::string word;
    int freq;
};

static int queryCandidatesByIdRange(int startId, int endId, int limit,
                                      std::vector<CandidateItem>& out) {
    if (!g_dict_db) return 0;

    const char* sql =
        "SELECT word, freq FROM dict WHERE id BETWEEN ?1 AND ?2 "
        "ORDER BY freq DESC LIMIT ?3";
    sqlite3_stmt* stmt = nullptr;
    int rc = sqlite3_prepare_v2(g_dict_db, sql, -1, &stmt, nullptr);
    if (rc != SQLITE_OK) return 0;

    sqlite3_bind_int(stmt, 1, startId);
    sqlite3_bind_int(stmt, 2, endId);
    sqlite3_bind_int(stmt, 3, limit);

    while (sqlite3_step(stmt) == SQLITE_ROW) {
        CandidateItem item;
        item.word = std::string(
            reinterpret_cast<const char*>(sqlite3_column_text(stmt, 0)));
        item.freq = sqlite3_column_int(stmt, 1);
        out.push_back(item);
    }

    sqlite3_finalize(stmt);
    return static_cast<int>(out.size());
}

static int queryBigramFreq(const std::string& prevWord, const std::string& nextWord) {
    if (!g_bigram_db || prevWord.empty() || nextWord.empty()) return 0;

    const char* sql = "SELECT freq FROM bigram WHERE prev_word = ?1 AND next_word = ?2";
    sqlite3_stmt* stmt = nullptr;
    if (sqlite3_prepare_v2(g_bigram_db, sql, -1, &stmt, nullptr) != SQLITE_OK) {
        return 0;
    }

    sqlite3_bind_text(stmt, 1, prevWord.c_str(), -1, SQLITE_STATIC);
    sqlite3_bind_text(stmt, 2, nextWord.c_str(), -1, SQLITE_STATIC);

    int freq = 0;
    if (sqlite3_step(stmt) == SQLITE_ROW) {
        freq = sqlite3_column_int(stmt, 0);
    }
    sqlite3_finalize(stmt);
    return freq;
}

// JNI exports must match the package/class/method name
extern "C" {

/**
 * 初始化引擎: 打开 dict.db + bigram.db, 构建/加载 Trie
 */
JNIEXPORT jboolean JNICALL
Java_com_nuc_omeletteinputmethod_inputC_DictEngine_initialize(
    JNIEnv* env, jobject /* thiz */, jstring filesDirPath) {

    if (g_initialized) return JNI_TRUE;

    DictTrie::clear();
    closeDatabases();

    std::string filesDir = jstringToUtf8(env, filesDirPath);

    std::string dictDbPath = filesDir + "/dict.db";
    std::string bigramDbPath = filesDir + "/bigram.db";
    g_trieDatPath = filesDir + "/dict_trie.dat";

    LOGI("Initializing dict engine...");
    LOGI("dict.db: %s", dictDbPath.c_str());
    LOGI("bigram.db: %s", bigramDbPath.c_str());

    // Step 1: 打开数据库
    if (!openDatabase(dictDbPath, &g_dict_db)) {
        LOGE("Cannot open dict.db");
        return JNI_FALSE;
    }
    if (!openDatabase(bigramDbPath, &g_bigram_db)) {
        LOGW("Cannot open bigram.db, bigram rerank disabled");
    }

    // Step 2: 尝试加载已有的 Trie 二进制文件
    bool trieReady = false;
    if (DictTrie::loadFromFile(g_trieDatPath)) {
        trieReady = true;
        LOGI("Loaded existing trie.dat (%d units)", DictTrie::getTrieSize());

        // 从文件加载 Trie 后, 仍需从 dict.db 加载拼音条目列表,
        // 否则 prefixSearch 无法将 Trie 匹配结果映射到 dict.db 的 id 范围
        std::vector<DictTrie::PinyinEntry> entries;
        if (loadPinyinEntriesFromDB(entries)) {
            DictTrie::setPinyinEntries(entries);
            LOGI("Reloaded %zu pinyin entries from dict.db", entries.size());
        } else {
            LOGE("Failed to reload pinyin entries from dict.db");
            return JNI_FALSE;
        }
    }

    // Step 3: 如果 Trie 未加载, 从 dict.db 构建
    if (!trieReady) {
        LOGI("Building Trie from dict.db ...");
        std::vector<DictTrie::PinyinEntry> entries;
        if (!loadPinyinEntriesFromDB(entries)) {
            LOGE("Failed to read pinyin entries from dict.db");
            return JNI_FALSE;
        }
        LOGI("Got %zu pinyin entries", entries.size());

        if (!DictTrie::buildFromPinyinEntries(entries)) {
            LOGE("Failed to build Trie");
            return JNI_FALSE;
        }
        LOGI("Trie built: %d units", DictTrie::getTrieSize());

        // 保存 Trie 二进制文件以便下次快速加载
        if (DictTrie::saveToFile(g_trieDatPath)) {
            LOGI("Saved trie to %s", g_trieDatPath.c_str());
        } else {
            LOGW("Failed to save trie.dat (non-fatal)");
        }
    }

    g_initialized = true;
    LOGI("DictEngine initialized successfully");
    return JNI_TRUE;
}

/**
 * 拼音前缀候选词搜索
 * 返回 JSON 格式候选词数组
 */
JNIEXPORT jstring JNICALL
Java_com_nuc_omeletteinputmethod_inputC_DictEngine_search(
    JNIEnv* env, jobject /* thiz */, jstring pinyin) {

    if (!g_initialized || !g_dict_db || !DictTrie::isLoaded()) {
        return utf8ToJstring(env, "[]");
    }

    std::string query = jstringToUtf8(env, pinyin);
    if (query.empty()) return utf8ToJstring(env, "[]");

    auto matches = DictTrie::prefixSearch(query, 200);
    if (matches.empty()) {
        return utf8ToJstring(env, "[]");
    }

    // 汇总所有匹配拼音的候选词ID范围, 取最宽的覆盖
    int minStart = matches[0].startId;
    int maxEnd = matches[0].endId;
    for (auto& m : matches) {
        if (m.startId < minStart) minStart = m.startId;
        if (m.endId > maxEnd) maxEnd = m.endId;
    }

    std::vector<CandidateItem> candidates;
    queryCandidatesByIdRange(minStart, maxEnd, 50, candidates);

    // 构建 JSON 数组
    std::string json = "[";
    for (size_t i = 0; i < candidates.size(); i++) {
        if (i > 0) json += ",";
        json += "{\"w\":\"";
        json += candidates[i].word;
        json += "\",\"f\":";
        json += std::to_string(candidates[i].freq);
        json += "}";
    }
    json += "]";
    return utf8ToJstring(env, json);
}

/**
 * Bigram 增强候选词排序
 * 输入: pinyin (当前拼音), prevWord (前一个已确认的字/词)
 * 返回: 带 bigram 加权重排序后的候选词 JSON
 */
JNIEXPORT jstring JNICALL
Java_com_nuc_omeletteinputmethod_inputC_DictEngine_searchWithBigram(
    JNIEnv* env, jobject /* thiz */, jstring pinyin, jstring prevWord) {

    if (!g_initialized || !g_dict_db || !DictTrie::isLoaded()) {
        return utf8ToJstring(env, "[]");
    }

    std::string query = jstringToUtf8(env, pinyin);
    std::string prev = jstringToUtf8(env, prevWord);

    auto matches = DictTrie::prefixSearch(query, 50);
    if (matches.empty()) {
        return utf8ToJstring(env, "[]");
    }

    int minStart = matches[0].startId;
    int maxEnd = matches[0].endId;
    for (auto& m : matches) {
        if (m.startId < minStart) minStart = m.startId;
        if (m.endId > maxEnd) maxEnd = m.endId;
    }

    std::vector<CandidateItem> candidates;
    queryCandidatesByIdRange(minStart, maxEnd, 100, candidates);

    // Bigram 加权: 取前字(或多字词末字)与每个候选词首字的 bigram freq
    std::string prevChar;
    if (!prev.empty()) {
        prevChar = std::string(1, prev[prev.size() - 1]);
    }

    // 按 (dict_freq + bigram * factor) 重排序
    // bigram 查的是字级 (前字→后词首字) 或 (前字→后词本身)
    std::vector<std::pair<size_t, double>> scored;
    for (size_t i = 0; i < candidates.size(); i++) {
        double score = static_cast<double>(candidates[i].freq);

        if (!prevChar.empty() && !candidates[i].word.empty()) {
            std::string nextFirstChar = std::string(1, candidates[i].word[0]);
            int bf = queryBigramFreq(prevChar, nextFirstChar);
            if (bf > 0) {
                score += bf * 100.0;
            }
        }

        scored.push_back({i, score});
    }

    std::sort(scored.begin(), scored.end(),
        [](const std::pair<size_t, double>& a, const std::pair<size_t, double>& b) {
            return a.second > b.second;
        });

    std::string json = "[";
    int limit = std::min(50, static_cast<int>(scored.size()));
    for (int i = 0; i < limit; i++) {
        if (i > 0) json += ",";
        auto& item = candidates[scored[i].first];
        json += "{\"w\":\"";
        json += item.word;
        json += "\",\"f\":";
        json += std::to_string(item.freq);
        json += ",\"s\":";
        json += std::to_string(static_cast<int>(scored[i].second));
        json += "}";
    }
    json += "]";
    return utf8ToJstring(env, json);
}

/**
 * 字词联想: 给定 zenzi 前缀, 返回以该前缀开头的候选词
 */
JNIEXPORT jstring JNICALL
Java_com_nuc_omeletteinputmethod_inputC_DictEngine_associate(
    JNIEnv* env, jobject /* thiz */, jstring wenzi) {

    if (!g_initialized || !g_dict_db) {
        return utf8ToJstring(env, "[]");
    }

    std::string prefix = jstringToUtf8(env, wenzi);
    if (prefix.empty()) return utf8ToJstring(env, "[]");

    const char* sql =
        "SELECT word, freq FROM dict WHERE word LIKE ?1 || '%' "
        "ORDER BY freq DESC LIMIT 30";
    sqlite3_stmt* stmt = nullptr;
    if (sqlite3_prepare_v2(g_dict_db, sql, -1, &stmt, nullptr) != SQLITE_OK) {
        return utf8ToJstring(env, "[]");
    }

    sqlite3_bind_text(stmt, 1, prefix.c_str(), -1, SQLITE_TRANSIENT);

    std::string json = "[";
    int count = 0;
    while (sqlite3_step(stmt) == SQLITE_ROW) {
        if (count > 0) json += ",";
        const char* word = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 0));
        int freq = sqlite3_column_int(stmt, 1);
        json += "{\"w\":\"";
        json += word;
        json += "\",\"f\":";
        json += std::to_string(freq);
        json += "}";
        count++;
    }
    json += "]";
    sqlite3_finalize(stmt);
    return utf8ToJstring(env, json);
}

/**
 * 启用分类词库: 加载额外 SQLite 数据库到内存
 */
JNIEXPORT jboolean JNICALL
Java_com_nuc_omeletteinputmethod_inputC_DictEngine_enableCategoryDict(
    JNIEnv* env, jobject /* thiz */, jstring dbPath, jstring category) {

    std::string path = jstringToUtf8(env, dbPath);
    std::string cat = jstringToUtf8(env, category);

    for (auto& info : g_category_dbs) {
        if (info.category == cat) {
            return JNI_TRUE;
        }
    }

    sqlite3* catDb = nullptr;
    if (!openDatabase(path, &catDb)) {
        LOGE("Cannot open category db: %s", path.c_str());
        return JNI_FALSE;
    }

    CategoryDbInfo info;
    info.category = cat;
    info.db = catDb;
    g_category_dbs.push_back(info);

    LOGI("Enabled category dict: %s (%s)", cat.c_str(), path.c_str());
    return JNI_TRUE;
}

/**
 * 带分类词库的增强搜索: 主词库 + 分类词库 UNION 合并
 */
JNIEXPORT jstring JNICALL
Java_com_nuc_omeletteinputmethod_inputC_DictEngine_searchWithCategories(
    JNIEnv* env, jobject /* thiz */, jstring pinyin, jobjectArray categories) {

    if (!g_initialized || !g_dict_db || !DictTrie::isLoaded()) {
        return utf8ToJstring(env, "[]");
    }

    std::string query = jstringToUtf8(env, pinyin);
    if (query.empty()) return utf8ToJstring(env, "[]");

    auto matches = DictTrie::prefixSearch(query, 50);
    if (matches.empty()) {
        return utf8ToJstring(env, "[]");
    }

    int minStart = matches[0].startId;
    int maxEnd = matches[0].endId;
    for (auto& m : matches) {
        if (m.startId < minStart) minStart = m.startId;
        if (m.endId > maxEnd) maxEnd = m.endId;
    }

    std::vector<CandidateItem> candidates;
    queryCandidatesByIdRange(minStart, maxEnd, 150, candidates);

    std::map<std::string, double> scored;
    for (size_t i = 0; i < candidates.size(); i++) {
        scored[candidates[i].word] = static_cast<double>(candidates[i].freq);
    }

    if (categories != nullptr) {
        jsize catCount = env->GetArrayLength(categories);
        for (jsize ci = 0; ci < catCount; ci++) {
            jstring jcat = (jstring)env->GetObjectArrayElement(categories, ci);
            std::string cat = jstringToUtf8(env, jcat);
            env->DeleteLocalRef(jcat);

            for (auto& info : g_category_dbs) {
                if (info.category != cat || !info.db) continue;

                const char* catSql =
                    "SELECT word, freq FROM category_dict WHERE pinyin "
                    "LIKE ?1 || '%' ORDER BY freq DESC LIMIT 50";
                sqlite3_stmt* stmt = nullptr;
                if (sqlite3_prepare_v2(info.db, catSql, -1, &stmt, nullptr) == SQLITE_OK) {
                    sqlite3_bind_text(stmt, 1, query.c_str(), -1, SQLITE_TRANSIENT);
                    while (sqlite3_step(stmt) == SQLITE_ROW) {
                        const char* w = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 0));
                        int f = sqlite3_column_int(stmt, 1);
                        if (w) {
                            std::string word(w);
                            double weighted = static_cast<double>(f) * 0.8;
                            auto it = scored.find(word);
                            if (it != scored.end()) {
                                if (weighted > it->second) {
                                    it->second = weighted;
                                }
                            } else {
                                scored[word] = weighted;
                            }
                        }
                    }
                    sqlite3_finalize(stmt);
                }
            }
        }
    }

    std::vector<std::pair<std::string, double>> sorted;
    for (auto& kv : scored) {
        sorted.push_back(kv);
    }
    std::sort(sorted.begin(), sorted.end(),
        [](const std::pair<std::string, double>& a, const std::pair<std::string, double>& b) {
            return a.second > b.second;
        });

    std::string json = "[";
    int limit = std::min(80, static_cast<int>(sorted.size()));
    for (int i = 0; i < limit; i++) {
        if (i > 0) json += ",";
        json += "{\"w\":\"";
        json += sorted[i].first;
        json += "\",\"f\":";
        json += std::to_string(static_cast<int>(sorted[i].second));
        json += "}";
    }
    json += "]";
    return utf8ToJstring(env, json);
}

/**
 * 释放引擎资源
 */
JNIEXPORT void JNICALL
Java_com_nuc_omeletteinputmethod_inputC_DictEngine_destroy(
    JNIEnv* /* env */, jobject /* thiz */) {

    g_initialized = false;
    DictTrie::clear();
    closeDatabases();
    LOGI("DictEngine destroyed");
}

}  // extern "C"
