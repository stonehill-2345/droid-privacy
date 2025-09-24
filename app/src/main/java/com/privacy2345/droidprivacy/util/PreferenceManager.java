package com.privacy2345.droidprivacy.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.privacy2345.droidprivacy.constant.Constant;
import com.privacy2345.droidprivacy.provider.MultiprocessSharedPreferences;

/**
 * 多进程 SharedPreferences 工具类
 *
 * @author : zhongjy@2345.com
 */
public class PreferenceManager {

    private static volatile PreferenceManager instance;
    private SharedPreferences sharedPreferences;

    private PreferenceManager(Context context) {
        if (context != null) {
            MultiprocessSharedPreferences.setAuthority(Constant.AUTHORITY);
            sharedPreferences = MultiprocessSharedPreferences.getSharedPreferences(context, Constant.Cache.SP_NAME, Context.MODE_PRIVATE);
        }
    }

    public static PreferenceManager getInstance(Context context) {
        if (instance == null) {
            synchronized (PreferenceManager.class) {
                if (instance == null) {
                    instance = new PreferenceManager(context);
                }
            }
        }
        return instance;
    }

    private boolean isSpAvailable() {
        return sharedPreferences != null;
    }

    public void putString(String key, String value) {
        if (isSpAvailable()) sharedPreferences.edit().putString(key, value).apply();
    }

    public void putInt(String key, int value) {
        if (isSpAvailable()) sharedPreferences.edit().putInt(key, value).apply();
    }

    public void putBoolean(String key, boolean value) {
        if (isSpAvailable()) sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public void putLong(String key, long value) {
        if (isSpAvailable()) sharedPreferences.edit().putLong(key, value).apply();
    }

    public void putFloat(String key, float value) {
        if (isSpAvailable()) sharedPreferences.edit().putFloat(key, value).apply();
    }

    public String getString(String key, String defValue) {
        return isSpAvailable() ? sharedPreferences.getString(key, defValue) : defValue;
    }

    public int getInt(String key, int defValue) {
        return isSpAvailable() ? sharedPreferences.getInt(key, defValue) : defValue;
    }

    public boolean getBoolean(String key, boolean defValue) {
        return isSpAvailable() ? sharedPreferences.getBoolean(key, defValue) : defValue;
    }

    public long getLong(String key, long defValue) {
        return isSpAvailable() ? sharedPreferences.getLong(key, defValue) : defValue;
    }

    public float getFloat(String key, float defValue) {
        return isSpAvailable() ? sharedPreferences.getFloat(key, defValue) : defValue;
    }

    public boolean contains(String key) {
        return isSpAvailable() && sharedPreferences.contains(key);
    }

    public void remove(String key) {
        if (isSpAvailable()) sharedPreferences.edit().remove(key).apply();
    }

    public void clear() {
        if (isSpAvailable()) sharedPreferences.edit().clear().apply();
    }
}