package com.privacy2345.droidprivacy.output;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.privacy2345.droidprivacy.constant.Constant;
import com.privacy2345.droidprivacy.model.ApiCallRecord;

import java.util.ArrayList;

/**
 * 通过广播完成跨应用数据接收
 *
 * @author : zhongjy@2345.com
 */
public class DataReceiver extends BroadcastReceiver {

    private static final String TAG = "DataReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String packageName = intent.getStringExtra(Constant.Intent.EXTRA_NAME_PACKAGE_NAME);
            ArrayList<ApiCallRecord> data = intent.getParcelableArrayListExtra(Constant.Intent.EXTRA_NAME_INVOKE_INFO);
            Log.d(TAG, "packageName:" + packageName + ",size:" + (data == null ? 0 : data.size()));
            if (!TextUtils.isEmpty(packageName) && data != null && !data.isEmpty()) {
                DataWriteExcelManager.getInstance().writeFile(packageName, data);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "DataReceiver has error:" + e.getMessage());
        }
    }
}
