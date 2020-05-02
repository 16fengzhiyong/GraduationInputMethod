package com.nuc.omeletteinputmethod.entityclass;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class AppInfomationEntity implements Parcelable {
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

    protected AppInfomationEntity(Parcel in) {
        id = in.readInt();
        appName = in.readString();
        appPackageName = in.readString();
        versionName = in.readString();
    }

    public static final Creator<AppInfomationEntity> CREATOR = new Creator<AppInfomationEntity>() {
        @Override
        public AppInfomationEntity createFromParcel(Parcel in) {
            return new AppInfomationEntity(in);
        }

        @Override
        public AppInfomationEntity[] newArray(int size) {
            return new AppInfomationEntity[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(appName);
        dest.writeString(appPackageName);
        dest.writeString(versionName);
    }
}
