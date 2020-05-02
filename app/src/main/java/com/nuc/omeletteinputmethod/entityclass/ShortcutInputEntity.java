package com.nuc.omeletteinputmethod.entityclass;

import java.util.List;

public class ShortcutInputEntity {
    private String appName;
    private String packageName;
    private List<FloatShortInputEntity> inputs;

    public ShortcutInputEntity(String appName, String packageName, List<FloatShortInputEntity> inputs) {
        this.appName = appName;
        this.packageName = packageName;
        this.inputs = inputs;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public List<FloatShortInputEntity> getInputs() {
        return inputs;
    }

    public void setInputs(List<FloatShortInputEntity> inputs) {
        this.inputs = inputs;
    }
}
