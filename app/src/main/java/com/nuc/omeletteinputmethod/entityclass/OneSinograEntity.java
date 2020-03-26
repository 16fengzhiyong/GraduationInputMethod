package com.nuc.omeletteinputmethod.entityclass;


/**
 * 用于使用拼音获取字符串
 */
public class OneSinograEntity {
    private String pinyin ;
    private int geshu;
    private String neirong;

    public OneSinograEntity() {
    }

    public OneSinograEntity(String pinyin, int geshu, String neirong) {
        this.pinyin = pinyin;
        this.geshu = geshu;
        this.neirong = neirong;
    }

    public OneSinograEntity(String pinyin, String neirong) {
        this.pinyin = pinyin;
        this.neirong = neirong;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public int getGeshu() {
        return geshu;
    }

    public void setGeshu(int geshu) {
        this.geshu = geshu;
    }

    public String getNeirong() {
        return neirong;
    }

    public void setNeirong(String neirong) {
        this.neirong = neirong;
    }
}
