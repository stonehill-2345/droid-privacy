package com.privacy2345.droidprivacy.model;

import androidx.annotation.NonNull;

/**
 * 应用信息模型类
 *
 * @author : zhongjy@2345.com
 */
public class AppInfo {
    private String appName;
    private String packageName;

    public AppInfo(String appName, String packageName) {
        this.appName = appName;
        this.packageName = packageName;
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

    @NonNull
    @Override
    public String toString() {
        return appName + " (" + packageName + ")";
    }
} 