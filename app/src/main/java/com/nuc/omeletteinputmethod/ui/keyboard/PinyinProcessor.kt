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
