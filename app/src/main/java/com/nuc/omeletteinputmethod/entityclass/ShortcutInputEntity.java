package com.nuc.omeletteinputmethod.entityclass;

import java.util.List;

public class ShortcutInputEntity {
    private String appName;
    private String packageName;
    private List<String> inputs;

    public ShortcutInputEntity(String appName, String packageName, List<String> inputs) {
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

    public List<String> getInputs() {
        return inputs;
    }

    public void setInputs(List<String> inputs) {
        this.inputs = inputs;
    }
}
