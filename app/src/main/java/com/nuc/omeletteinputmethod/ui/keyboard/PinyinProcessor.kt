package com.nuc.omeletteinputmethod.ui.keyboard

object PinyinProcessor {
    // ====================================================================
    // 模糊音映射表 (Fuzzy Pinyin Mapping)
    // ====================================================================

    private val FUZZY_PAIR_MAP: Map<String, Pair<String, String>> =
        mapOf(
            "zh↔z" to ("zh" to "z"),
            "ch↔c" to ("ch" to "c"),
            "sh↔s" to ("sh" to "s"),
            "n↔l" to ("n" to "l"),
            "r↔l" to ("r" to "l"),
            "ang↔an" to ("ang" to "an"),
            "eng↔en" to ("eng" to "en"),
            "ing↔in" to ("ing" to "in"),
            "h↔f" to ("h" to "f"),
        )

    /**
     * 根据启用的模糊规则生成所有可能的拼音变体
     * 输入 "zan" + {"zh↔z"} → 返回 ["zan", "zhan"]
     */
    fun expandFuzzyPinyin(
        raw: String,
        enabledRules: Set<String>,
    ): List<String> {
        if (enabledRules.isEmpty() || raw.isEmpty()) return listOf(raw)

        val variants = mutableSetOf(raw)
        val queue = mutableListOf(raw)

        while (queue.isNotEmpty()) {
            val current = queue.removeAt(0)
            for (ruleKey in enabledRules) {
                val (a, b) = FUZZY_PAIR_MAP[ruleKey] ?: continue
                // 前缀替换：尝试将拼音前缀替换为模糊配对
                val replaced = replaceInitialOrFinal(current, a, b)
                if (replaced != null && variants.add(replaced)) {
                    queue.add(replaced)
                }
            }
        }

        return variants.toList()
    }

    private fun replaceInitialOrFinal(
        pinyin: String,
        from: String,
        to: String,
    ): String? {
        // 声母替换：只替换在开头的部分
        if (pinyin.startsWith(from) && from.length >= 2) {
            val replaced = to + pinyin.substring(from.length)
            if (looksLikeValidPinyin(replaced)) return replaced
        }
        if (pinyin.startsWith(to) && to.length >= 2) {
            val replaced = from + pinyin.substring(to.length)
            if (looksLikeValidPinyin(replaced)) return replaced
        }
        // 韵母替换：替换在末尾的韵母部分
        if (pinyin.endsWith(from) && from.length >= 1) {
            val replaced = pinyin.substring(0, pinyin.length - from.length) + to
            if (looksLikeValidPinyin(replaced)) return replaced
        }
        if (pinyin.endsWith(to) && to.length >= 1) {
            val replaced = pinyin.substring(0, pinyin.length - to.length) + from
            if (looksLikeValidPinyin(replaced)) return replaced
        }
        return null
    }

    private val validInitials =
        setOf(
            "b", "p", "m", "f", "d", "t", "n", "l", "g", "k", "h",
            "j", "q", "x", "zh", "ch", "sh", "r", "z", "c", "s",
            "y", "w",
        )

    private val validFinals =
        setOf(
            "a", "o", "e", "i", "u", "v", "ü",
            "ai", "ei", "ao", "ou", "an", "en", "ang", "eng", "ong",
            "ia", "ie", "iu", "iao", "ian", "iang", "ing", "iong",
            "ua", "uo", "uai", "ui", "uan", "un", "uang",
            "ve", "üe", "van", "uan", "vn", "ün", "ue", "er",
        )

    private fun looksLikeValidPinyin(pinyin: String): Boolean {
        if (pinyin.isEmpty()) return false
        for (initial in validInitials.sortedByDescending { it.length }) {
            if (pinyin.startsWith(initial)) {
                val rest = pinyin.substring(initial.length)
                return rest.isEmpty() || rest in validFinals
            }
        }
        return pinyin in validFinals
    }

    // ====================================================================
    // 拼音音节切分 (Pinyin Syllable Segmentation)
    // ====================================================================
    // 解决 "nihao" 无法查到 "你好" 的问题：
    // dict.db 存储拼音时用空格分隔音节 ("ni hao")，而用户连续输入无分隔符 ("nihao")。
    // 本模块将无分隔符拼音自动切分为空格分隔的多音节形式，匹配词库格式。

    /** 所有合法拼音音节的完整集合（含声母+韵母组合以及零声母纯韵母） */
    val ALL_PINYIN_SYLLABLES: Set<String> by lazy {
        val set = mutableSetOf<String>()
        // 纯韵母（零声母音节）：如 a, ai, an, ang, ao, e, ei, en, eng, er, o, ou
        for (fin in validFinals) {
            set.add(fin)
        }
        // 声母+韵母组合：如 ba, zhong, nü, lve ...
        for (init in validInitials) {
            for (fin in validFinals) {
                val syl = init + fin
                // 非法组合过滤：汉语拼音不存在 b+ong, f+ong, g+iu, k+iu, q+ong, x+ong 等
                if (!isInvalidSyllable(syl)) {
                    set.add(syl)
                }
            }
        }
        // 额外添加几个带 ü 的特殊音节（validFinals 用 v/ue 表示 ü 相关）
        set.addAll(listOf("nü", "lü", "nüe", "lüe", "jü", "qü", "xü", "yü"))
        set.toSet()
    }

    /** 汉语拼音中不存在的声母+韵母组合 */
    private fun isInvalidSyllable(syl: String): Boolean {
        val invalidCombos = setOf(
            // 唇音 b/p/m/f + ong 不存在
            "bong", "pong", "mong", "fong",
            // 软腭音 g/k/h + ong → gong/kong/hong 合法，不在此列
            // 腭音 j/q/x + ong → jiong/qiong/xiong 合法（用 iong），但 j/q/x + ong 不合法
            "jong", "qong", "xong",
            // 软腭音 g/k/h + iu 不合法（giu/kiu/hiu 不存在）
            "giu", "kiu", "hiu",
            // 唇齿音 f + ai/ao/ou/iu/ie/ia → 大多不合法（fou 合法）
            "fai", "fao", "fiu", "fie", "fia",
            // d/t + uai/uo/uang 不合法
            "duai", "tuai", "duang", "tuang",
            // n/l + uai/uang 不合法
            "nuai", "nuang", "luai", "luang",
            // j/q/x + uai/uang 不合法
            "juai", "juang", "quai", "quang", "xuai", "xuang",
            // zh/ch/sh/r + ong → zhong/chong/rong 合法，不在此列
            "shong",
            // z/c/s + ong → zong/cong/song 合法，zh/ch/sh + ong → zhong/chong/ 合法
            // r + uai/uang 不合法
            "ruai", "ruang",
            // b/p/m/f/d/t/n/l/g/k/h/zh/ch/sh/r/z/c/s + iong 均不合法（仅 j/q/x + iong 合法）
            "biong", "piong", "miong", "fiong",
            "diong", "tiong", "niong", "liong",
            "giong", "kiong", "hiong",
            "zhiong", "chiong", "shiong", "riong",
            "ziong", "ciong", "siong",
            // 各类声母 + van(v表示ü) 不合法
            "bvan", "pvan", "mvan", "fvan",
            "dvan", "tvan", "nvan", "lvan",
            "gvan", "kvan", "hvan",
            "zhvan", "chvan", "shvan", "rvan",
            "zvan", "cvan", "svan",
        )
        return syl in invalidCombos
    }

    /** 切分缓存，避免重复计算 */
    private val segmentationCache = java.util.concurrent.ConcurrentHashMap<String, List<String>>()

    /**
     * 将连续拼音字符串切分为所有可能的空格分隔音节组合。
     *
     * 例：
     *   "nihao"    → ["ni hao"]
     *   "xian"     → ["xian", "xi an"]     （全返回策略）
     *   "nihaoa"   → ["ni hao a"]
     *   "women"    → ["wo men"]
     *   "zhongguo" → ["zhong guo"]
     *   "nih"      → ["ni h"]              （不完全切分，末尾残留作为前缀）
     *
     * @param raw 无分隔符的连续拼音字符串（小写）
     * @return 所有有效切分结果，每个结果用空格连接音节；支持不完全切分（末尾残留字母）
     */
    fun segmentPinyin(raw: String): List<String> {
        if (raw.isEmpty()) return emptyList()

        // 缓存命中
        segmentationCache[raw]?.let { return it }

        // 快速路径：本身就是合法单音节
        val syllables = ALL_PINYIN_SYLLABLES
        if (raw in syllables) {
            val result = listOf(raw)
            segmentationCache[raw] = result
            return result
        }

        val results = mutableListOf<String>()
        // 先尝试完全切分
        segmentRecursive(raw, 0, mutableListOf(), results, syllables)

        // 如果完全切分失败，尝试不完全切分（允许末尾有残留前缀）
        if (results.isEmpty()) {
            segmentPrefixRecursive(raw, 0, mutableListOf(), results, syllables)
        }

        val final = results.distinct()

        segmentationCache[raw] = final
        return final
    }

    /**
     * 回溯递归切分（完全切分）：
     * - 从位置 [pos] 开始尝试所有匹配音节
     * - 切下一个音节后递归处理剩余部分
     * - 到达末尾时记录完整切分路径
     */
    private fun segmentRecursive(
        raw: String,
        pos: Int,
        path: MutableList<String>,
        results: MutableList<String>,
        syllables: Set<String>,
    ) {
        if (pos >= raw.length) {
            // 完全切分成功，记录结果
            results.add(path.joinToString(" "))
            return
        }

        val remaining = raw.length - pos
        // 尝试从 pos 开始的每种可能音节长度（最长6字符：zhuang）
        val maxSylLen = minOf(6, remaining)
        for (len in 1..maxSylLen) {
            val candidate = raw.substring(pos, pos + len)
            if (candidate in syllables) {
                path.add(candidate)
                segmentRecursive(raw, pos + len, path, results, syllables)
                path.removeAt(path.size - 1)
            }
        }
    }

    /**
     * 不完全切分（前缀切分）：
     * - 允许末尾有未切分的残留字母（作为前缀匹配）
     * - 用于处理用户正在输入拼音中间的情况，如 "nih" → "ni h"
     */
    private fun segmentPrefixRecursive(
        raw: String,
        pos: Int,
        path: MutableList<String>,
        results: MutableList<String>,
        syllables: Set<String>,
    ) {
        if (pos >= raw.length) {
            // 完全切分成功
            results.add(path.joinToString(" "))
            return
        }

        val remaining = raw.length - pos

        // 尝试匹配完整音节
        var foundSyllable = false
        val maxSylLen = minOf(6, remaining)
        for (len in 1..maxSylLen) {
            val candidate = raw.substring(pos, pos + len)
            if (candidate in syllables) {
                foundSyllable = true
                path.add(candidate)
                segmentPrefixRecursive(raw, pos + len, path, results, syllables)
                path.removeAt(path.size - 1)
            }
        }

        // 如果没有匹配到完整音节，或者剩余部分不是任何音节的前缀，
        // 则将剩余部分作为残留前缀加入
        if (!foundSyllable || !couldBeSyllablePrefix(raw.substring(pos), syllables)) {
            val remainder = raw.substring(pos)
            if (remainder.isNotEmpty()) {
                path.add(remainder)
                results.add(path.joinToString(" "))
                path.removeAt(path.size - 1)
            }
        }
    }

    /**
     * 检查字符串是否是某个合法音节的前缀
     */
    private fun couldBeSyllablePrefix(s: String, syllables: Set<String>): Boolean {
        if (s.isEmpty()) return false
        // 检查是否是任何音节的前缀
        for (syl in syllables) {
            if (syl.startsWith(s)) return true
        }
        return false
    }

    // ====================================================================
    // 拼音纠错 (Pinyin Typo Correction)
    // ====================================================================

    private val pinyinInitChars = "abcdefghijklmnopqrstuvwxyz".toSet()

    /**
     * 基于编辑距离=1的拼音纠错
     * 返回所有可能的纠正候选列表，需要调用方通过字典引擎过滤
     */
    fun correctPinyinTypo(raw: String): List<String> {
        if (raw.isEmpty()) return emptyList()
        val results = mutableListOf<String>()
        val lower = raw.lowercase()

        // 替换 (substitution): 每个位置替换为任意字母
        for (i in lower.indices) {
            for (c in pinyinInitChars) {
                if (lower[i] != c) {
                    results.add(lower.replaceRange(i, i + 1, c.toString()))
                }
            }
        }

        // 删除 (deletion): 删除每个位置的字符
        for (i in lower.indices) {
            val deleted = lower.removeRange(i, i + 1)
            if (deleted.isNotEmpty()) {
                results.add(deleted)
            }
        }

        // 插入 (insertion): 在每个位置插入任意字母
        for (i in 0..lower.length) {
            for (c in pinyinInitChars) {
                results.add(lower.substring(0, i) + c + lower.substring(i))
            }
        }

        return results.distinct().sortedBy { editDistance(lower, it) }
    }

    private fun editDistance(
        s1: String,
        s2: String,
    ): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j
        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(dp[i - 1][j] + 1, dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost)
            }
        }
        return dp[s1.length][s2.length]
    }

    // ====================================================================
    // 双拼输入方案 (Double Pinyin Schemes)
    // ====================================================================

    // 小鹤双拼方案
    private val XIAOHE_INITIAL_MAP: Map<Char, String> =
        mapOf(
            'b' to "b", 'c' to "c", 'd' to "d", 'f' to "f", 'g' to "g",
            'h' to "h", 'j' to "j", 'k' to "k", 'l' to "l", 'm' to "m",
            'n' to "n", 'p' to "p", 'q' to "q", 'r' to "r", 's' to "s",
            't' to "t", 'w' to "w", 'x' to "x", 'y' to "y",
            'v' to "zh", 'i' to "ch", 'u' to "sh",
            'z' to "z",
        )

    private val XIAOHE_FINAL_MAP: Map<Char, String> =
        mapOf(
            'a' to "a", 'b' to "in", 'c' to "ao", 'd' to "ai",
            'e' to "e", 'f' to "en", 'g' to "eng", 'h' to "ang",
            'i' to "i", 'j' to "an", 'k' to "ing", 'l' to "iang",
            'm' to "ian", 'n' to "iao", 'o' to "uo", 'p' to "ie",
            'q' to "iu", 'r' to "uan", 's' to "ong", 't' to "ue",
            'u' to "u", 'v' to "ui", 'w' to "ei", 'x' to "ia",
            'y' to "un", 'z' to "ou",
        )

    // 零声母韵母映射 (小鹤)
    private val XIAOHE_ZERO_INITIAL_MAP: Map<Char, String> =
        mapOf(
            'a' to "a",
            'e' to "e",
            'o' to "o",
        )

    // 微软双拼方案
    private val MS_INITIAL_MAP: Map<Char, String> =
        mapOf(
            'b' to "b", 'c' to "c", 'd' to "d", 'f' to "f", 'g' to "g",
            'h' to "h", 'j' to "j", 'k' to "k", 'l' to "l", 'm' to "m",
            'n' to "n", 'p' to "p", 'q' to "q", 'r' to "r", 's' to "s",
            't' to "t", 'w' to "w", 'x' to "x", 'y' to "y",
            'v' to "zh", 'i' to "ch", 'u' to "sh", 'z' to "z",
        )

    private val MS_FINAL_MAP: Map<Char, String> =
        mapOf(
            'a' to "a", 'b' to "ou", 'c' to "iao", 'd' to "uang",
            'e' to "e", 'f' to "en", 'g' to "eng", 'h' to "ang",
            'i' to "i", 'j' to "an", 'k' to "ao", 'l' to "ai",
            'm' to "ian", 'n' to "in", 'o' to "uo", 'p' to "un",
            'q' to "iu", 'r' to "uan", 's' to "iong", 't' to "ue",
            'u' to "u", 'v' to "ui", 'w' to "ia", 'x' to "ie",
            'y' to "ing", 'z' to "ei",
        )

    // 自然码方案
    private val ZIRANMA_INITIAL_MAP: Map<Char, String> =
        mapOf(
            'b' to "b", 'c' to "c", 'd' to "d", 'f' to "f", 'g' to "g",
            'h' to "h", 'j' to "j", 'k' to "k", 'l' to "l", 'm' to "m",
            'n' to "n", 'p' to "p", 'q' to "q", 'r' to "r", 's' to "s",
            't' to "t", 'w' to "w", 'x' to "x", 'y' to "y",
            'v' to "zh", 'i' to "ch", 'u' to "sh", 'z' to "z",
        )

    private val ZIRANMA_FINAL_MAP: Map<Char, String> =
        mapOf(
            'a' to "a", 'b' to "ou", 'c' to "iao", 'd' to "iang",
            'e' to "e", 'f' to "en", 'g' to "eng", 'h' to "ang",
            'i' to "i", 'j' to "an", 'k' to "ao", 'l' to "ai",
            'm' to "ian", 'n' to "in", 'o' to "uo", 'p' to "un",
            'q' to "iu", 'r' to "uan", 's' to "iong", 't' to "ve",
            'u' to "u", 'v' to "ui", 'w' to "ua", 'x' to "ie",
            'y' to "ing", 'z' to "ei",
        )

    // 零声母韵母映射 (自然码)
    private val ZIRANMA_ZERO_INITIAL_MAP: Map<Char, String> =
        mapOf(
            'a' to "a",
            'e' to "e",
            'o' to "o",
        )

    data class DoubleScheme(
        val initialMap: Map<Char, String>,
        val finalMap: Map<Char, String>,
        val zeroInitialMap: Map<Char, String> = emptyMap(),
    )

    private val DOUBLE_SCHEMES: Map<String, DoubleScheme> =
        mapOf(
            "xiaohe" to DoubleScheme(XIAOHE_INITIAL_MAP, XIAOHE_FINAL_MAP, XIAOHE_ZERO_INITIAL_MAP),
            "ms" to DoubleScheme(MS_INITIAL_MAP, MS_FINAL_MAP),
            "ziranma" to DoubleScheme(ZIRANMA_INITIAL_MAP, ZIRANMA_FINAL_MAP, ZIRANMA_ZERO_INITIAL_MAP),
        )

    val schemeNames: Set<String> get() = DOUBLE_SCHEMES.keys

    /**
     * 双拼击键序列 → 全拼字符串
     * 输入 "xiaohe" + "xahd" → 输出 "xiao'he'da"
     * 每两个字母组成一个音节
     */
    fun toFullPinyin(
        doubleInput: String,
        scheme: String,
    ): String {
        val s = DOUBLE_SCHEMES[scheme] ?: return doubleInput
        if (doubleInput.length < 2) return doubleInput

        val syllables = mutableListOf<String>()
        var i = 0
        while (i < doubleInput.length - 1) {
            val initialKey = doubleInput[i]
            val finalKey = doubleInput[i + 1]

            // 零声母音节：第一个键直接是韵母，第二个键也是韵母键
            val initialZero = s.zeroInitialMap[initialKey]
            if (initialZero != null) {
                // 第一个键对应零声母完整音节，第二个键开启新音节
                syllables.add(initialZero)
                i += 1
                continue
            }

            // 正常声母+韵母音节
            val initial = s.initialMap[initialKey] ?: initialKey.toString()
            val final = s.finalMap[finalKey] ?: finalKey.toString()
            syllables.add(initial + final)
            i += 2
        }

        // 剩余的单个字符直接保留
        if (i < doubleInput.length) {
            syllables.add(doubleInput[i].toString())
        }

        return syllables.joinToString("'")
    }

    // ====================================================================
    // 中英文混输检测
    // ====================================================================

    private val commonEnglishWords: Set<String> =
        setOf(
            "the", "a", "an", "is", "are", "was", "were", "be", "been",
            "have", "has", "had", "do", "does", "did", "will", "would",
            "can", "could", "may", "might", "shall", "should", "must",
            "i", "you", "he", "she", "it", "we", "they", "me", "him",
            "her", "us", "them", "my", "your", "his", "its", "our",
            "their", "this", "that", "these", "those", "not", "no",
            "yes", "or", "and", "but", "if", "so", "to", "in", "on",
            "at", "by", "for", "of", "with", "from", "as", "about",
            "into", "through", "during", "before", "after", "above",
            "below", "between", "out", "off", "up", "down", "over",
            "under", "again", "further", "then", "once", "here",
            "there", "when", "where", "why", "how", "all", "both",
            "each", "few", "more", "most", "other", "some", "such",
            "only", "own", "same", "new", "good", "high", "old",
            "great", "big", "small", "large", "long", "early",
            "young", "important", "public", "bad", "free", "sure",
            "able", "available", "possible", "right", "true", "real",
            "full", "special", "recent", "like", "just", "still",
            "also", "very", "too", "quite", "really", "already",
            "always", "never", "often", "sometimes", "usually",
            "well", "way", "even", "much", "many", "far", "near",
            "today", "now", "thing", "world", "life", "hand",
            "part", "place", "case", "week", "company", "system",
            "program", "work", "government", "number", "night",
            "point", "home", "water", "room", "area", "money",
            "story", "fact", "month", "lot", "right", "study",
            "book", "eye", "job", "word", "business", "issue",
            "side", "kind", "head", "house", "service", "friend",
            "father", "power", "hour", "game", "line", "end",
            "member", "law", "car", "city", "name", "president",
            "team", "minute", "idea", "kid", "body", "info",
            "back", "parent", "face", "level", "office",
            "person", "art", "war", "history", "party",
            "result", "morning", "reason", "research",
            "girl", "guy", "moment", "air", "teacher",
            "force", "education", "Apple", "Google", "iPhone",
            "iPad", "Mac", "Windows", "Android", "Linux",
            "Microsoft", "Amazon", "Facebook", "Twitter",
            "Instagram", "YouTube", "Netflix", "Spotify",
        )

    /**
     * 检测 buffer 末尾的英文段，返回需要 commit 的英文文本和剩余拼音前缀
     * 规则：
     * 1. 连续大写字母 → 视为英文段
     * 2. 末尾匹配已知英文单词 → 视为英文段
     * 返回 Pair<英文部分, 拼音部分>，英文部分为空表示无英文段
     */
    fun detectMixedEnglish(buffer: String): Pair<String, String> {
        if (buffer.isEmpty()) return "" to ""

        // 查找连续大写字母段（在拼音 inputBuffer 中的任何位置）
        val upperPattern = Regex("[A-Z]+")
        val upperMatch = upperPattern.findAll(buffer).lastOrNull()
        if (upperMatch != null && upperMatch.value.isNotEmpty()) {
            val upperEnd = upperMatch.range.last + 1
            val englishPart = buffer.substring(upperMatch.range.first, upperEnd)
            val rest = buffer.substring(upperEnd)
            // 大写段前的拼音部分保留
            val before = buffer.substring(0, upperMatch.range.first)
            val pinyinRest = before + rest
            return englishPart to pinyinRest
        }

        // 检测末尾是否有已知英文单词（包括大写开头词）
        val lowerBuffer = buffer.lowercase()
        val sortedWords = commonEnglishWords.sortedByDescending { it.length }
        for (word in sortedWords) {
            if (lowerBuffer.endsWith(word) && !isPinyinFragment(word)) {
                // 确认 word 前的字符不是字母（或者 buffer 就是 word）
                val startIndex = buffer.length - word.length
                if (startIndex == 0 || !buffer[startIndex - 1].isLetter()) {
                    val englishPart = buffer.substring(startIndex)
                    val pinyinPart = buffer.substring(0, startIndex)
                    return englishPart to pinyinPart
                }
            }
        }

        return "" to buffer
    }

    private fun isPinyinFragment(s: String): Boolean {
        val ambiguous =
            setOf(
                "a", "an", "ang", "ai", "ao", "e", "en", "eng", "ei", "er",
                "i", "in", "ing", "o", "ou", "u", "un", "he", "ha", "hi", "hu", "la", "li",
                "lu", "ma", "mi", "mu", "na", "ni", "nu", "pa", "pi", "pu", "ba", "bi",
                "bu", "da", "di", "du", "ta", "ti", "tu", "ka", "ke", "ku", "ga", "ge",
                "gu", "fa", "fu", "sa", "si", "su", "ca", "cu", "za", "zu", "ya", "yi",
                "yu", "wa", "wo", "wu", "re", "ri", "ru", "le", "lo", "me", "mo", "ne",
                "no", "pe", "po", "te", "to", "se", "so", "ce", "co", "ze", "zo", "we",
                "ye", "yo", "ju", "qu", "xu", "ji", "qi", "xi",
            )
        return s in ambiguous
    }
}
