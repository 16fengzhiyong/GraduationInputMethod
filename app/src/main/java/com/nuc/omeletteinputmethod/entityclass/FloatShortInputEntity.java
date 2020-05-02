package com.nuc.omeletteinputmethod.entityclass;

public class FloatShortInputEntity {
    private int id;
    private String tag;
    private String packageName;
    private int cishu ;

    public FloatShortInputEntity(int id, String tag, String packageName, int cishu) {
        this.id = id;
        this.tag = tag;
        this.packageName = packageName;
        this.cishu = cishu;
    }

    public FloatShortInputEntity(int id, String tag, String packageName) {
        this.id = id;
        this.tag = tag;
        this.packageName = packageName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
