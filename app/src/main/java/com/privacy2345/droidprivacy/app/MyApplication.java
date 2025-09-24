package com.privacy2345.droidprivacy.app;

import android.app.Application;

/**
 * 自定义Application
 *
 * @author : zhongjy@2345.com
 */
public class MyApplication extends Application {

    private static Application application;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
    }

    public static Application getApplication() {
        return application;
    }
}
