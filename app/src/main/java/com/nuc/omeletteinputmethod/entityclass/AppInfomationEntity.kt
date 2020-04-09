package com.nuc.omeletteinputmethod.entityclass

import android.graphics.drawable.Drawable

class AppInfomationEntity
/**
 * @param id app在本列表中的id
 * @param appName app到名字
 * @param appPackageName app包名
 * @param appIcon app图标
 */
(var id: Int, var appName: String?, var appPackageName: String?, var versionName: String?, var appIcon: Drawable?) {

    override fun toString(): String {
        return "AppInfomationEntity{" +
                "id=" + id +
                ", appName='" + appName + '\''.toString() +
                ", appPackageName='" + appPackageName + '\''.toString() +
                ", appIcon=" + appIcon +
                '}'.toString()
    }
}
