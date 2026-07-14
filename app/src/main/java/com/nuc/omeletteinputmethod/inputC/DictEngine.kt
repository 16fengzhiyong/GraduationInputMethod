package com.nuc.omeletteinputmethod.inputC

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DictEngine
    @Inject
    constructor() {
        /**
         * 初始化词典引擎
         * @param filesDirPath 应用 files 目录绝对路径 (context.filesDir.absolutePath)
         * @return true 如果初始化成功
         */
        external fun initialize(filesDirPath: String): Boolean

        /**
         * 拼音前缀候选词搜索
         * @param pinyin 拼音字符串 (字母, 不含声调)
         * @return JSON 数组, 每个元素 {"w":"汉字","f":词频}
         */
        external fun search(pinyin: String): String

        /**
         * Bigram 增强候选词搜索
         * @param pinyin 拼音字符串
         * @param prevWord 前一个已确认的字/词 (用于 bigram 上下文)
         * @return JSON 数组, 每个元素 {"w":"汉字","f":词频,"s":加权分数}
         */
        external fun searchWithBigram(
            pinyin: String,
            prevWord: String,
        ): String

        /**
         * 字词联想: 以 wenzi 为前缀的候选词
         * @param wenzi 汉字前缀
         * @return JSON 数组
         */
        external fun associate(wenzi: String): String

        /**
         * 启用分类词库: 通知 C++ 层加载额外的分类 SQLite 数据库
         * @param dbPath 分类词库 SQLite 文件的绝对路径
         * @param category 分类名称 (medical/legal/it 等)
         * @return true 如果加载成功
         */
        external fun enableCategoryDict(dbPath: String, category: String): Boolean

        /**
         * 带分类词库的增强搜索
         * @param pinyin 拼音字符串
         * @param categories 已启用的分类名称数组 (可为 null)
         * @return JSON 数组, 合并主词库和分类词库结果 (分类词库结果权重 ×0.8)
         */
        external fun searchWithCategories(pinyin: String, categories: Array<String>?): String

        /**
         * 释放引擎资源
         */
        external fun destroy()

        companion object {
            init {
                System.loadLibrary("dict_engine")
            }
        }
    }
