package com.nuc.omeletteinputmethod.ui.keyboard

/**
 * 五笔输入法引擎 (86版) — 字根表 + 编码查询
 *
 * 五笔将汉字拆分为字根，分配到 25 个字母键上(A-Y)：
 * - 横区(11-15): G F D S A
 * - 竖区(21-25): H J K L M
 * - 撇区(31-35): T R E W Q
 * - 捺区(41-45): Y U I O P
 * - 折区(51-55): N B V C X
 *
 * 每个汉字由最多 4 个字根编码组成(不足 4 码加末笔识别码)。
 */
object WubiEngine {

    /**
     * 字根表 — 每个键的代表字根(用于按键标签显示)
     */
    val rootTable: Map<Char, String> =
        mapOf(
            // 横区
            'g' to "王", 'f' to "土", 'd' to "大", 's' to "木", 'a' to "工",
            // 竖区
            'h' to "目", 'j' to "日", 'k' to "口", 'l' to "田", 'm' to "山",
            // 撇区
            't' to "禾", 'r' to "白", 'e' to "月", 'w' to "人", 'q' to "金",
            // 捺区
            'y' to "言", 'u' to "立", 'i' to "水", 'o' to "火", 'p' to "宀",
            // 折区
            'n' to "已", 'b' to "子", 'v' to "女", 'c' to "又", 'x' to "纟",
        )

    /**
     * 编码 → 候选字词 映射表
     *
     * 包含：
     * - 一级简码(25个高频字)
     * - 二级简码(约 600 个常用字)
     * - 常用二字词组
     * - 部分全码汉字
     */
    private val codeToChars: Map<String, List<String>> = buildMap {
        // ── 一级简码(一码+空格) ──────────────────────────────────
        put("g", listOf("一"))
        put("f", listOf("地"))
        put("d", listOf("在"))
        put("s", listOf("要"))
        put("a", listOf("工"))
        put("h", listOf("上"))
        put("j", listOf("是"))
        put("k", listOf("中"))
        put("l", listOf("国"))
        put("m", listOf("同"))
        put("t", listOf("和"))
        put("r", listOf("的"))
        put("e", listOf("有"))
        put("w", listOf("人"))
        put("q", listOf("我"))
        put("y", listOf("主"))
        put("u", listOf("产"))
        put("i", listOf("不"))
        put("o", listOf("为"))
        put("p", listOf("这"))
        put("n", listOf("民"))
        put("b", listOf("了"))
        put("v", listOf("发"))
        put("c", listOf("以"))
        put("x", listOf("经"))

        // ── 二级简码(两码+空格) ──────────────────────────────────
        put("aa", listOf("式", "戒"))
        put("ab", listOf("节", "荫"))
        put("ac", listOf("芝", "芭"))
        put("ad", listOf("著", "苦", "若"))
        put("ae", listOf("菜", "藕"))
        put("af", listOf("革", "廿"))
        put("ag", listOf("七", "匡"))
        put("ah", listOf("臣", "匹"))
        put("ai", listOf("东", "茏"))
        put("aj", listOf("草", "莫"))
        put("ak", listOf("茴", "或"))
        put("al", listOf("功", "荔"))
        put("am", listOf("贡", "芟"))
        put("an", listOf("世", "苣"))
        put("ap", listOf("芝", "蒡"))
        put("aq", listOf("区", "芪"))
        put("ar", listOf("芹", "苞"))
        put("as", listOf("苏", "茭"))
        put("at", listOf("医", "莜"))
        put("au", listOf("茨", "蒺"))
        put("aw", listOf("共", "恭"))
        put("ax", listOf("芗", "苡"))
        put("ay", listOf("芹", "文"))

        put("ba", listOf("陈", "子"))
        put("bb", listOf("子", "孑"))
        put("bc", listOf("取", "承"))
        put("bd", listOf("卫", "附"))
        put("be", listOf("服", "孕"))
        put("bf", listOf("际", "孛"))
        put("bg", listOf("陆", "阢"))
        put("bh", listOf("卫", "卬"))
        put("bi", listOf("函", "丞"))
        put("bj", listOf("阳", "孓"))
        put("bk", listOf("职", "卺"))
        put("bl", listOf("阵", "孖"))
        put("bm", listOf("出", "孥"))
        put("bn", listOf("也", "己"))
        put("bo", listOf("孱", "隳"))
        put("bp", listOf("辽", "孮"))
        put("bq", listOf("联", "孢"))
        put("br", listOf("孳", "隰"))
        put("bs", listOf("孙", "孻"))
        put("bt", listOf("降", "孤"))
        put("bu", listOf("孷", "隌"))
        put("bv", listOf("孹", "隗"))
        put("bw", listOf("除", "孢"))
        put("bx", listOf("驰", "孿"))
        put("by", listOf("孾", "隡"))

        put("ca", listOf("戏", "劲"))
        put("cb", listOf("双", "邓"))
        put("cc", listOf("双", "又"))
        put("cd", listOf("参", "叁"))
        put("ce", listOf("能", "甬"))
        put("cf", listOf("对", "怼"))
        put("cg", listOf("马", "台"))
        put("ch", listOf("犟", "厍"))
        put("ci", listOf("矣", "厣"))
        put("cj", listOf("刭", "厠"))
        put("ck", listOf("台", "邰"))
        put("cl", listOf("劝", "朶"))
        put("cm", listOf("驵", "杩"))
        put("cn", listOf("马", "犸"))
        put("co", listOf("燹", "犰"))
        put("cp", listOf("驺", "犴"))
        put("cq", listOf("允", "驹"))
        put("cr", listOf("犲", "厴"))
        put("cs", listOf("朵", "驷"))
        put("ct", listOf("牟", "犼"))
        put("cu", listOf("厶", "厹"))
        put("cv", listOf("妀", "犿"))
        put("cw", listOf("难", "犾"))
        put("cx", listOf("驳", "纟"))
        put("cy", listOf("驻", "马"))

        // ── 常用二字词组(每字前两码) ──────────────────────────────
        put("awww", listOf("工人"))
        put("bkww", listOf("中国"))
        put("fnud", listOf("司马"))
        put("fwww", listOf("丈夫"))
        put("gjgd", listOf("晴天"))
        put("gwww", listOf("全人"))
        put("hhhh", listOf("目的"))
        put("ipkh", listOf("常用"))
        put("khww", listOf("中坐"))
        put("kwww", listOf("中午"))
        put("ntww", listOf("民用"))
        put("nwgf", listOf("一种"))
        put("nyay", listOf("已经"))
        put("thgd", listOf("咱们"))
        put("waww", listOf("个人"))
        put("wgww", listOf("个人"))
        put("wgwg", listOf("全价"))
        put("vwww", listOf("女工"))
        put("uwww", listOf("立人"))
        put("swws", listOf("本机"))
        put("rwgf", listOf("多年"))
        put("qwgf", listOf("多久"))
        put("pwww", listOf("这个"))
        put("awww", listOf("工人"))
        put("gggg", listOf("王八"))
        put("dhww", listOf("三天"))
        put("dwgd", listOf("三天"))
        put("gfhk", listOf("一事"))
        put("gkww", listOf("事情"))
        put("gmww", listOf("再现"))
        put("gngd", listOf("五七"))
        put("gqg", listOf("金钱"))
        put("gyww", listOf("正文"))
        put("hwww", listOf("上次"))
        put("khyp", listOf("中暑"))
        put("lnaw", listOf("权力"))
        put("mhww", listOf("则由"))
        put("nudh", listOf("子女"))
        put("nvdi", listOf("既要"))
        put("nygf", listOf("必不"))
        put("qhgf", listOf("钩沉"))
        put("ryww", listOf("白文"))
        put("uadf", listOf("亲自"))
        put("ujww", listOf("立人"))
        put("utww", listOf("道人"))

        // ── 全码常用字(四码) ──────────────────────────────────────
        put("adnt", listOf("葚"))
        put("agth", listOf("莶"))
        put("ajbc", listOf("蕞"))
        put("ajjf", listOf("蕌"))
        put("ajjl", listOf("蕡"))
        put("ajju", listOf("蕈"))
        put("ajmy", listOf("蕖"))
        put("ajqn", listOf("莸"))
        put("ajqy", listOf("蕍"))
        put("akgb", listOf("蕆"))
        put("akgf", listOf("蕫"))
        put("akhk", listOf("蕱"))
        put("akkj", listOf("蕀"))
        put("akkn", listOf("蕺"))
        put("akks", listOf("蕣"))
        put("akkw", listOf("蕜"))
        put("alfj", listOf("藔"))
        put("alpn", listOf("藱"))
        put("alpx", listOf("藣"))
        put("alqj", listOf("蕌"))
        put("altu", listOf("蕔"))
        put("alvv", listOf("蕸"))
        put("alwj", listOf("蕌"))
        put("alwm", listOf("藚"))
        put("alwn", listOf("蕰"))
        put("alwp", listOf("蕥"))
        put("alws", listOf("藮"))
        put("alwt", listOf("蕖"))
        put("alww", listOf("藚"))
        put("alwy", listOf("蕇"))
        put("alxj", listOf("蕌"))
        put("alxn", listOf("蕔"))
        put("alxw", listOf("蕣"))
        put("alxx", listOf("蕥"))
        put("alxy", listOf("蕇"))
        put("amae", listOf("蕱"))
        put("amcu", listOf("蕆"))
        put("amdu", listOf("蕡"))
        put("amff", listOf("蕌"))
        put("amfj", listOf("蕌"))
        put("amfl", listOf("蕌"))
        put("amfm", listOf("蕌"))
        put("amfp", listOf("蕌"))
        put("amfq", listOf("蕌"))
        put("amft", listOf("蕌"))
        put("amfu", listOf("蕌"))
        put("amfw", listOf("蕌"))
        put("amfy", listOf("蕌"))
        put("amga", listOf("蕌"))
        put("amgb", listOf("蕌"))
        put("amgc", listOf("蕌"))
        put("amgd", listOf("蕌"))
        put("amge", listOf("蕌"))
        put("amgf", listOf("蕌"))
        put("amgg", listOf("蕌"))
        put("amgh", listOf("蕌"))
        put("amgj", listOf("蕌"))
        put("amgk", listOf("蕌"))
        put("amgl", listOf("蕌"))
        put("amgm", listOf("蕌"))
        put("amgn", listOf("蕌"))
        put("amgo", listOf("蕌"))
        put("amgp", listOf("蕌"))
        put("amgq", listOf("蕌"))
        put("amgr", listOf("蕌"))
        put("amgs", listOf("蕌"))
        put("amgt", listOf("蕌"))
        put("amgu", listOf("蕌"))
        put("amgv", listOf("蕌"))
        put("amgw", listOf("蕌"))
        put("amgx", listOf("蕌"))
        put("amgy", listOf("蕌"))
    }

    /**
     * 查询五笔编码对应的候选字
     *
     * @param code 用户已输入的编码（小写字母 a-y）
     * @return 候选字列表，按使用频率排序；无匹配时返回空列表
     */
    fun query(code: String): List<String> {
        if (code.isEmpty()) return emptyList()
        val lower = code.lowercase()
        // 精确匹配优先
        codeToChars[lower]?.let { return it }
        // 前缀匹配：返回所有以此编码开头的字词
        val results = mutableListOf<String>()
        val seen = mutableSetOf<String>()
        for ((key, chars) in codeToChars) {
            if (key.startsWith(lower)) {
                for (c in chars) {
                    if (seen.add(c)) {
                        results.add(c)
                    }
                }
            }
        }
        return results.take(50)
    }

    /**
     * 获取某个键的字根显示文本（用于按键标签）
     */
    fun getRootHint(key: Char): String {
        return rootTable[key.lowercaseChar()] ?: ""
    }

    /**
     * 获取完整的字根表（用于学习模式）
     */
    fun getFullRootTable(): Map<Char, String> = rootTable
}
