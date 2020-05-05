package com.nuc.omeletteinputmethod.util;

import android.content.Context;
import android.util.Log;


import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by admin on 2015/10/30.
 */
public class SymbolsManager {

    public String[] SMILE;
    public String[] MATH;
    public String[] BU_SHOU;
    public String[] SPECIAL;
    public String[] NET;
    public String[] RUSSIAN;
    public String[] NUMBER;
    public String[] PHONETIC;
    public String[] BOPOMOFO;
    public String[] JAPANESE;
    public String[] GREECE;

    public Symbols symbols;
    private  IniAnalysis iniAnalysis;
    
    private String[] convertValues(List<String> list) {
        return StringUtil.convertListToString(list);
    }

    public SymbolsManager(Context context){
        iniAnalysis = new IniAnalysis(context);
        symbols = new Symbols();
        try {
            SMILE       = convertValues(iniAnalysis.getValuesFromFile(symbols.SMILE));
            MATH        = convertValues(iniAnalysis.getValuesFromFile(symbols.MATH));
            BU_SHOU     = convertValues(iniAnalysis.getValuesFromFile(symbols.BU_SHOU));
            SPECIAL     = convertValues(iniAnalysis.getValuesFromFile(symbols.SPECIAL));
            NET         = convertValues(iniAnalysis.getValuesFromFile(symbols.NET));
            RUSSIAN     = convertValues(iniAnalysis.getValuesFromFile(symbols.RUSSIAN));
            PHONETIC    = convertValues(iniAnalysis.getValuesFromFile(symbols.PHONETIC));
            NUMBER      = convertValues(iniAnalysis.getValuesFromFile(symbols.NUMBER));
            BOPOMOFO    = convertValues(iniAnalysis.getValuesFromFile(symbols.BOPOMOFO));
            JAPANESE    = convertValues(iniAnalysis.getValuesFromFile(symbols.JAPNAESE));
            GREECE      = convertValues(iniAnalysis.getValuesFromFile(symbols.GREECE));
        } catch (IOException e) {
            Log.d("WIVE","lightViewAnimate"+e.toString());
            e.printStackTrace();
        }

    }

    public class Symbols{
        public String SMILE     = "symbols/smile.ini";
        public String MATH      = "symbols/shu_xue.ini";
        public String BU_SHOU   = "symbols/bu_shou.ini";
        public String SPECIAL   = "symbols/te_shu.ini";
        public String NET       = "symbols/wang_luo.ini";
        public String RUSSIAN   = "symbols/e_wen.ini";
        public String NUMBER    = "symbols/xu_hao.ini";
        public String PHONETIC  = "symbols/yin_biao.ini";
        public String BOPOMOFO  = "symbols/zhu_yin.ini";
        public String JAPNAESE  = "symbols/ri_wen_pian_jia.ini";
        public String GREECE    = "symbols/greece.ini";
    }

    @Override
    public String toString() {
        return "SymbolsManager{" +
                "SMILE=" + Arrays.toString(SMILE) +
                ", MATH=" + Arrays.toString(MATH) +
                ", BU_SHOU=" + Arrays.toString(BU_SHOU) +
                ", SPECIAL=" + Arrays.toString(SPECIAL) +
                ", NET=" + Arrays.toString(NET) +
                ", RUSSIAN=" + Arrays.toString(RUSSIAN) +
                ", NUMBER=" + Arrays.toString(NUMBER) +
                ", PHONETIC=" + Arrays.toString(PHONETIC) +
                ", BOPOMOFO=" + Arrays.toString(BOPOMOFO) +
                ", JAPANESE=" + Arrays.toString(JAPANESE) +
                ", GREECE=" + Arrays.toString(GREECE) +
                ", symbols=" + symbols +
                ", iniAnalysis=" + iniAnalysis +
                '}';
    }
}
