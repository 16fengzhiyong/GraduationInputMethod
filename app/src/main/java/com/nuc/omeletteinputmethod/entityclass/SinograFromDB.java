package com.nuc.omeletteinputmethod.entityclass;

public class SinograFromDB {
    private String wenzi1;
    private String wenzi2;
    private String pinyin;
    private int jisheng;
    private int id;

    public SinograFromDB(String wenzi1, String wenzi2, int jisheng, int id) {
        this.wenzi1 = wenzi1;
        this.wenzi2 = wenzi2;
        this.jisheng = jisheng;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWenzi1() {
        return wenzi1;
    }

    public void setWenzi1(String wenzi1) {
        this.wenzi1 = wenzi1;
    }

    public String getWenzi2() {
        return wenzi2;
    }

    public void setWenzi2(String wenzi2) {
        this.wenzi2 = wenzi2;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public int getJisheng() {
        return jisheng;
    }

    public void setJisheng(int jisheng) {
        this.jisheng = jisheng;
    }
}
