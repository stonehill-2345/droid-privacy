package com.privacy2345.droidprivacy.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.privacy2345.droidprivacy.model.AppInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用列表获取工具类
 *
 * @author : zhongjy@2345.com
 */
public class AppListUtil {
    private static final String TAG = "AppListUtil";

    /**
     * 获取已安装的应用列表
     *
     * @param context 上下文
     * @return 应用列表
     */
    public static List<AppInfo> getInstalledApps(Context context) {
        List<AppInfo> appList = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        
        try {
            List<ApplicationInfo> applications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
            
            for (ApplicationInfo appInfo : applications) {
                // 过滤系统应用，只显示用户安装的应用
                if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    String appName = packageManager.getApplicationLabel(appInfo).toString();
                    String packageName = appInfo.packageName;
                    appList.add(new AppInfo(appName, packageName));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "获取应用列表失败: " + e.getMessage());
        }
        
        return appList;
    }
} 