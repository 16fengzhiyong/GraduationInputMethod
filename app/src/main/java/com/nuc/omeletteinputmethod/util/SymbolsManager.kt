package com.nuc.omeletteinputmethod.util

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject

class SymbolsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    lateinit var SMILE: Array<String>
    lateinit var MATH: Array<String>
    lateinit var BU_SHOU: Array<String>
    lateinit var SPECIAL: Array<String>
    lateinit var NET: Array<String>
    lateinit var RUSSIAN: Array<String>
    lateinit var NUMBER: Array<String>
    lateinit var PHONETIC: Array<String>
    lateinit var BOPOMOFO: Array<String>
    lateinit var JAPANESE: Array<String>
    lateinit var GREECE: Array<String>

    lateinit var symbols: Symbols
    private lateinit var iniAnalysis: IniAnalysis

    private fun convertValues(list: List<String>): Array<String> {
        return list.flatMap { it.split("\n").filter { s -> s.isNotEmpty() } }.toTypedArray()
    }

    init {
        iniAnalysis = IniAnalysis(context)
        symbols = Symbols()
        try {
            SMILE = convertValues(iniAnalysis.getValuesFromFile(symbols.SMILE))
            MATH = convertValues(iniAnalysis.getValuesFromFile(symbols.MATH))
            BU_SHOU = convertValues(iniAnalysis.getValuesFromFile(symbols.BU_SHOU))
            SPECIAL = convertValues(iniAnalysis.getValuesFromFile(symbols.SPECIAL))
            NET = convertValues(iniAnalysis.getValuesFromFile(symbols.NET))
            RUSSIAN = convertValues(iniAnalysis.getValuesFromFile(symbols.RUSSIAN))
            PHONETIC = convertValues(iniAnalysis.getValuesFromFile(symbols.PHONETIC))
            NUMBER = convertValues(iniAnalysis.getValuesFromFile(symbols.NUMBER))
            BOPOMOFO = convertValues(iniAnalysis.getValuesFromFile(symbols.BOPOMOFO))
            JAPANESE = convertValues(iniAnalysis.getValuesFromFile(symbols.JAPANESE))
            GREECE = convertValues(iniAnalysis.getValuesFromFile(symbols.GREECE))
        } catch (e: IOException) {
            Log.d("WIVE", "lightViewAnimate" + e.toString())
            e.printStackTrace()
            // Fall back to empty arrays if loading fails
            val emptyArr = emptyArray<String>()
            SMILE = emptyArr; MATH = emptyArr; BU_SHOU = emptyArr; SPECIAL = emptyArr
            NET = emptyArr; RUSSIAN = emptyArr; NUMBER = emptyArr; PHONETIC = emptyArr
            BOPOMOFO = emptyArr; JAPANESE = emptyArr; GREECE = emptyArr
        }
    }

    class Symbols {
        val SMILE = "symbols/smile.ini"
        val MATH = "symbols/shu_xue.ini"
        val BU_SHOU = "symbols/bu_shou.ini"
        val SPECIAL = "symbols/te_shu.ini"
        val NET = "symbols/wang_luo.ini"
        val RUSSIAN = "symbols/e_wen.ini"
        val NUMBER = "symbols/xu_hao.ini"
        val PHONETIC = "symbols/yin_biao.ini"
        val BOPOMOFO = "symbols/zhu_yin.ini"
        val JAPANESE = "symbols/ri_wen_pian_jia.ini"
        val GREECE = "symbols/greece.ini"
    }

    override fun toString(): String {
        return "SymbolsManager{" +
                "SMILE=" + SMILE.contentToString() +
                ", MATH=" + MATH.contentToString() +
                ", BU_SHOU=" + BU_SHOU.contentToString() +
                ", SPECIAL=" + SPECIAL.contentToString() +
                ", NET=" + NET.contentToString() +
                ", RUSSIAN=" + RUSSIAN.contentToString() +
                ", NUMBER=" + NUMBER.contentToString() +
                ", PHONETIC=" + PHONETIC.contentToString() +
                ", BOPOMOFO=" + BOPOMOFO.contentToString() +
                ", JAPANESE=" + JAPANESE.contentToString() +
                ", GREECE=" + GREECE.contentToString() +
                ", symbols=" + symbols +
                ", iniAnalysis=" + iniAnalysis +
                '}'
    }
}