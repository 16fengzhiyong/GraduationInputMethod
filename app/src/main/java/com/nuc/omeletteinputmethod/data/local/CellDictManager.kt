package com.nuc.omeletteinputmethod.data.local

import android.content.Context
import com.nuc.omeletteinputmethod.data.model.CategoryDictionary
import com.nuc.omeletteinputmethod.data.model.CategoryDictionaryDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CellDictManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val categoryDictionaryDao: CategoryDictionaryDao,
) {
    companion object {
        const val CELLDICT_ASSET_DIR = "celldict"
        const val CELLDICT_FILES_DIR = "celldict"
        const val CAT_MEDICAL = "medical"
        const val CAT_LEGAL = "legal"
        const val CAT_IT = "it"
        const val CAT_GAMING = "gaming"
        const val CAT_GEOGRAPHY = "geography"
        const val CAT_IDIOM = "idiom"
        const val CAT_POETRY = "poetry"
        const val CAT_FOOD = "food"
        const val CAT_SPORTS = "sports"
        const val CAT_FINANCE = "finance"

        val ALL_CATEGORIES = listOf(
            CAT_MEDICAL, CAT_LEGAL, CAT_IT, CAT_GAMING, CAT_GEOGRAPHY,
            CAT_IDIOM, CAT_POETRY, CAT_FOOD, CAT_SPORTS, CAT_FINANCE,
        )

        val FALLBACK_WORDS: Map<String, List<Triple<String, String, Int>>> = mapOf(
            CAT_MEDICAL to listOf(
                Triple("医生", "yi sheng", 5000), Triple("护士", "hu shi", 4800),
                Triple("医院", "yi yuan", 4500), Triple("手术", "shou shu", 4300),
                Triple("药物", "yao wu", 4100), Triple("诊断", "zhen duan", 3900),
                Triple("治疗", "zhi liao", 3700), Triple("病人", "bing ren", 3500),
                Triple("健康", "jian kang", 3300), Triple("疾病", "ji bing", 3100),
                Triple("血压", "xue ya", 2900), Triple("外科", "wai ke", 2700),
                Triple("内科", "nei ke", 2500), Triple("处方", "chu fang", 2300),
                Triple("麻醉", "ma zui", 2100), Triple("门诊", "men zhen", 1900),
                Triple("病房", "bing fang", 1700), Triple("急救", "ji jiu", 1500),
                Triple("疫苗", "yi miao", 1300), Triple("康复", "kang fu", 1200),
            ),
            CAT_LEGAL to listOf(
                Triple("法律", "fa lv", 5000), Triple("合同", "he tong", 4800),
                Triple("诉讼", "su song", 4500), Triple("法院", "fa yuan", 4300),
                Triple("律师", "lv shi", 4100), Triple("判决", "pan jue", 3900),
                Triple("权利", "quan li", 3700), Triple("义务", "yi wu", 3500),
                Triple("证据", "zheng ju", 3300), Triple("犯罪", "fan zui", 3100),
                Triple("刑事", "xing shi", 2900), Triple("民事", "min shi", 2700),
                Triple("赔偿", "pei chang", 2500), Triple("仲裁", "zhong cai", 2300),
                Triple("立法", "li fa", 2100), Triple("执法", "zhi fa", 1900),
                Triple("司法", "si fa", 1700), Triple("条款", "tiao kuan", 1500),
                Triple("公章", "gong zhang", 1300), Triple("法人", "fa ren", 1200),
            ),
            CAT_IT to listOf(
                Triple("计算机", "ji suan ji", 5000), Triple("软件", "ruan jian", 4800),
                Triple("硬件", "ying jian", 4500), Triple("网络", "wang luo", 4300),
                Triple("数据库", "shu ju ku", 4100), Triple("服务器", "fu wu qi", 3900),
                Triple("程序", "cheng xu", 3700), Triple("代码", "dai ma", 3500),
                Triple("算法", "suan fa", 3300), Triple("接口", "jie kou", 3100),
                Triple("协议", "xie yi", 2900), Triple("架构", "jia gou", 2700),
                Triple("编程", "bian cheng", 2500), Triple("开发", "kai fa", 2300),
                Triple("调试", "tiao shi", 2100), Triple("编译", "bian yi", 1900),
                Triple("部署", "bu shu", 1700), Triple("云计算", "yun ji suan", 1500),
                Triple("人工智能", "ren gong zhi neng", 1300), Triple("加密", "jia mi", 1200),
                Triple("前端", "qian duan", 1100), Triple("后端", "hou duan", 1000),
            ),
            CAT_GAMING to listOf(
                Triple("游戏", "you xi", 5000), Triple("玩家", "wan jia", 4800),
                Triple("角色", "jiao se", 4500), Triple("技能", "ji neng", 4300),
                Triple("装备", "zhuang bei", 4100), Triple("副本", "fu ben", 3900),
                Triple("公会", "gong hui", 3700), Triple("竞技", "jing ji", 3500),
                Triple("排行", "pai hang", 3300), Triple("任务", "ren wu", 3100),
                Triple("经验", "jing yan", 2900), Triple("等级", "deng ji", 2700),
                Triple("金币", "jin bi", 2500), Triple("道具", "dao ju", 2300),
                Triple("攻略", "gong lve", 2100), Triple("存档", "cun dang", 1900),
                Triple("联机", "lian ji", 1700), Triple("战局", "zhan ju", 1500),
                Triple("射手", "she shou", 1300), Triple("辅助", "fu zhu", 1200),
            ),
            CAT_GEOGRAPHY to listOf(
                Triple("地理", "di li", 5000), Triple("城市", "cheng shi", 4800),
                Triple("中国", "zhong guo", 4500), Triple("省份", "sheng fen", 4300),
                Triple("河流", "he liu", 4100), Triple("山脉", "shan mai", 3900),
                Triple("首都", "shou du", 3700), Triple("地区", "di qu", 3500),
                Triple("人口", "ren kou", 3300), Triple("面积", "mian ji", 3100),
                Triple("沿海", "yan hai", 2900), Triple("高原", "gao yuan", 2700),
                Triple("盆地", "pen di", 2500), Triple("丘陵", "qiu ling", 2300),
                Triple("沙漠", "sha mo", 2100), Triple("冰川", "bing chuan", 1900),
                Triple("岛屿", "dao yu", 1700), Triple("湖泊", "hu po", 1500),
                Triple("北京", "bei jing", 8000), Triple("上海", "shang hai", 7800),
                Triple("广州", "guang zhou", 7600), Triple("深圳", "shen zhen", 7400),
                Triple("成都", "cheng du", 7200), Triple("杭州", "hang zhou", 7000),
                Triple("南京", "nan jing", 6800), Triple("武汉", "wu han", 6600),
            ),
            CAT_IDIOM to listOf(
                Triple("一心一意", "yi xin yi yi", 5000), Triple("三心二意", "san xin er yi", 4800),
                Triple("四面八方", "si mian ba fang", 4500), Triple("五颜六色", "wu yan liu se", 4300),
                Triple("七上八下", "qi shang ba xia", 4100), Triple("九牛一毛", "jiu niu yi mao", 3900),
                Triple("十全十美", "shi quan shi mei", 3700), Triple("百里挑一", "bai li tiao yi", 3500),
                Triple("千军万马", "qian jun wan ma", 3300), Triple("万无一失", "wan wu yi shi", 3100),
                Triple("画蛇添足", "hua she tian zu", 2900), Triple("守株待兔", "shou zhu dai tu", 2700),
                Triple("亡羊补牢", "wang yang bu lao", 2500), Triple("掩耳盗铃", "yan er dao ling", 2300),
                Triple("拔苗助长", "ba miao zhu zhang", 2100), Triple("刻舟求剑", "ke zhou qiu jian", 1900),
                Triple("明知故犯", "ming zhi gu fan", 1700), Triple("胸有成竹", "xiong you cheng zhu", 1500),
                Triple("对牛弹琴", "dui niu tan qin", 1300), Triple("虎头蛇尾", "hu tou she wei", 1200),
                Triple("龙飞凤舞", "long fei feng wu", 1100), Triple("马到成功", "ma dao cheng gong", 1000),
                Triple("鸟语花香", "niao yu hua xiang", 900), Triple("鸡犬不宁", "ji quan bu ning", 800),
                Triple("狐假虎威", "hu jia hu wei", 700), Triple("狼吞虎咽", "lang tun hu yan", 600),
            ),
            CAT_POETRY to listOf(
                Triple("床前明月光", "chuang qian ming yue guang", 5000),
                Triple("疑是地上霜", "yi shi di shang shuang", 4800),
                Triple("举头望明月", "ju tou wang ming yue", 4500),
                Triple("低头思故乡", "di tou si gu xiang", 4300),
                Triple("白日依山尽", "bai ri yi shan jin", 4100),
                Triple("黄河入海流", "huang he ru hai liu", 3900),
                Triple("欲穷千里目", "yu qiong qian li mu", 3700),
                Triple("更上一层楼", "geng shang yi ceng lou", 3500),
                Triple("春眠不觉晓", "chun mian bu jue xiao", 3300),
                Triple("处处闻啼鸟", "chu chu wen ti niao", 3100),
                Triple("夜来风雨声", "ye lai feng yu sheng", 2900),
                Triple("花落知多少", "hua luo zhi duo shao", 2700),
                Triple("离离原上草", "li li yuan shang cao", 2500),
                Triple("一岁一枯荣", "yi sui yi ku rong", 2300),
                Triple("野火烧不尽", "ye huo shao bu jin", 2100),
                Triple("春风吹又生", "chun feng chui you sheng", 1900),
                Triple("千山鸟飞绝", "qian shan niao fei jue", 1700),
                Triple("万径人踪灭", "wan jing ren zong mie", 1500),
                Triple("孤舟蓑笠翁", "gu zhou suo li weng", 1300),
                Triple("独钓寒江雪", "du diao han jiang xue", 1200),
            ),
            CAT_FOOD to listOf(
                Triple("美食", "mei shi", 5000), Triple("烹饪", "peng ren", 4800),
                Triple("食材", "shi cai", 4500), Triple("厨房", "chu fang", 4300),
                Triple("菜谱", "cai pu", 4100), Triple("点心", "dian xin", 3900),
                Triple("火锅", "huo guo", 3700), Triple("烧烤", "shao kao", 3500),
                Triple("饺子", "jiao zi", 3300), Triple("面条", "mian tiao", 3100),
                Triple("米饭", "mi fan", 2900), Triple("包子", "bao zi", 2700),
                Triple("馒头", "man tou", 2500), Triple("粥", "zhou", 2300),
                Triple("汤圆", "tang yuan", 2100), Triple("粽子", "zong zi", 1900),
                Triple("月饼", "yue bing", 1700), Triple("春卷", "chun juan", 1500),
                Triple("拉面", "la mian", 1300), Triple("凉皮", "liang pi", 1200),
                Triple("麻辣烫", "ma la tang", 1100), Triple("烤鸭", "kao ya", 1000),
            ),
            CAT_SPORTS to listOf(
                Triple("运动", "yun dong", 5000), Triple("比赛", "bi sai", 4800),
                Triple("足球", "zu qiu", 4500), Triple("篮球", "lan qiu", 4300),
                Triple("跑步", "pao bu", 4100), Triple("游泳", "you yong", 3900),
                Triple("健身", "jian shen", 3700), Triple("训练", "xun lian", 3500),
                Triple("冠军", "guan jun", 3300), Triple("体育", "ti yu", 3100),
                Triple("羽毛球", "yu mao qiu", 2900), Triple("乒乓球", "ping pang qiu", 2700),
                Triple("网球", "wang qiu", 2500), Triple("排球", "pai qiu", 2300),
                Triple("田径", "tian jing", 2100), Triple("体操", "ti cao", 1900),
                Triple("拳击", "quan ji", 1700), Triple("武术", "wu shu", 1500),
                Triple("滑雪", "hua xue", 1300), Triple("登山", "deng shan", 1200),
            ),
            CAT_FINANCE to listOf(
                Triple("金融", "jin rong", 5000), Triple("投资", "tou zi", 4800),
                Triple("股票", "gu piao", 4500), Triple("基金", "ji jin", 4300),
                Triple("银行", "yin hang", 4100), Triple("贷款", "dai kuan", 3900),
                Triple("利率", "li lv", 3700), Triple("汇率", "hui lv", 3500),
                Triple("保险", "bao xian", 3300), Triple("理财", "li cai", 3100),
                Triple("证券", "zheng quan", 2900), Triple("期货", "qi huo", 2700),
                Triple("债券", "zhai quan", 2500), Triple("信贷", "xin dai", 2300),
                Triple("资本", "zi ben", 2100), Triple("资产", "zi chan", 1900),
                Triple("负债", "fu zhai", 1700), Triple("分红", "fen hong", 1500),
                Triple("涨停", "zhang ting", 1300), Triple("跌停", "die ting", 1200),
                Triple("创业板", "chuang ye ban", 1100), Triple("科创板", "ke chuang ban", 1000),
            ),
        )
    }

    val celldictDir: File get() = File(context.filesDir, CELLDICT_FILES_DIR)

    fun isCategoryEnabled(category: String): Boolean =
        File(celldictDir, "$category.db").exists()

    suspend fun enableCategory(category: String): Boolean =
        withContext(Dispatchers.IO) {
            if (isCategoryEnabled(category)) return@withContext true

            val dbName = "$category.db"
            val assetPath = "$CELLDICT_ASSET_DIR/$dbName"
            val destFile = File(celldictDir, dbName)

            celldictDir.mkdirs()

            val deployed =
                try {
                    context.assets.open(assetPath).use { input ->
                        destFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    true
                } catch (_: Exception) {
                    false
                }

            if (!deployed) {
                loadFallbackWords(category, destFile)
            }
            true
        }

    suspend fun disableCategory(category: String) {
        withContext(Dispatchers.IO) {
            categoryDictionaryDao.deleteCategory(category)
        }
    }

    fun getEnabledCategories(): Flow<List<String>> = categoryDictionaryDao.getAllCategories()

    private suspend fun loadFallbackWords(category: String, destFile: File) {
        val words = FALLBACK_WORDS[category] ?: return
        val items =
            words.map { (w, py, freq) ->
                CategoryDictionary(category = category, word = w, pinyin = py, freq = freq, source = "fallback")
            }
        categoryDictionaryDao.insertBatch(items)
    }

    fun searchCategoryWords(category: String, pinyin: String, limit: Int = 20): List<String> {
        val words = FALLBACK_WORDS[category] ?: return emptyList()
        val cleanPinyin = pinyin.lowercase().replace(" ", "")
        return words.asSequence()
            .filter { (_, py, _) -> py.replace(" ", "").startsWith(cleanPinyin) }
            .sortedByDescending { it.third }
            .take(limit)
            .map { it.first }
            .toList()
    }
}