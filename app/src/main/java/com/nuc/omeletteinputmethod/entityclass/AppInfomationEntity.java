package com.nuc.omeletteinputmethod.entityclass;

import android.graphics.drawable.Drawable;

public class AppInfomationEntity {
    private int id;
    private String appName;
    private String appPackageName;
    private String versionName;
    private Drawable appIcon;


    /**
     * @param id app在本列表中的id
     * @param appName app到名字
     * @param appPackageName app包名
     * @param appIcon app图标
     */
    public AppInfomationEntity(int id, String appName, String appPackageName,String versionName, Drawable appIcon) {
        this.id = id;
        this.appName = appName;
        this.versionName = versionName;
        this.appPackageName = appPackageName;
        this.appIcon = appIcon;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppPackageName() {
        return appPackageName;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public void setAppPackageName(String appPackageName) {
        this.appPackageName = appPackageName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    @Override
    public String
    toString() {
        return "AppInfomationEntity{" +
                "id=" + id +
                ", appName='" + appName + '\'' +
                ", appPackageName='" + appPackageName + '\'' +
                ", appIcon=" + appIcon +
                '}';
    }
}
