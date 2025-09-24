package com.privacy2345.droidprivacy.output;


import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.privacy2345.droidprivacy.constant.Constant;
import com.privacy2345.droidprivacy.hook.HookModule;
import com.privacy2345.droidprivacy.model.ApiCallRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 数据中转管理<br>
 * 1、收集到的数据是在被Hook的应用中，可以执行的操作收到被hook的应用限制，将数据通过广播进行跨应用传输，在拦截工具中进行更多处理<br>
 * 2、收集的数据可能会非常多，数据缓存后定时发送
 *
 * @author : zhongjy@2345.com
 */
public class DataDispatchManager {

    private static final String TAG = "DataDispatchManager";

    private static final class InstanceHolder {
        static final DataDispatchManager instance = new DataDispatchManager();
    }

    public static DataDispatchManager getInstance() {
        return InstanceHolder.instance;
    }

    /**
     * 定时任务间隔，单位：毫秒
     */
    private static final int INTERVAL_TIME = 10 * 1000;

    private Handler handler;

    private final Map<String, List<ApiCallRecord>> dataMap;

    private DataDispatchManager() {
        dataMap = new HashMap<>();
        if (HookModule.uploadData || HookModule.localCheckFileInput) {
            handler = new Handler(Looper.getMainLooper());
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    performIntervalTask();
                    handler.postDelayed(this, INTERVAL_TIME);
                }
            };
            handler.postDelayed(runnable, INTERVAL_TIME);
        }
    }

    private void performIntervalTask() {
        Log.d(TAG, "执行定时写入任务");
        Iterator<Map.Entry<String, List<ApiCallRecord>>> it = dataMap.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<String, List<ApiCallRecord>> entry = it.next();
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                ArrayList<ApiCallRecord> data = new ArrayList<>(entry.getValue());
                it.remove();
                notifyWrite(entry.getKey(), data);
            }
        }
    }

    public synchronized void write(String packageName, List<ApiCallRecord> apiCallRecordList) {
        if (TextUtils.isEmpty(packageName) || apiCallRecordList == null || apiCallRecordList.isEmpty()) {
            return;
        }
        List<ApiCallRecord> cacheData = dataMap.get(packageName);
        if (cacheData == null) {
            cacheData = new ArrayList<>();
        }
        cacheData.addAll(apiCallRecordList);
        dataMap.put(packageName, cacheData);
        Log.d(TAG, "缓存数据，packageName:" + packageName + ",本次缓存:" + apiCallRecordList.size() + ",已缓存：" + cacheData.size());
    }

    public void notifyWrite(String packageName, ArrayList<ApiCallRecord> apiCallRecordList) {
        // hook模块运行在被hook的应用中，文件写入需要依赖poi库，不能在目标应用中完成数据写入，所以通过广播完成跨应用数据处理
        // 通过广播发送的数据量超过500kb会失败，通过对数据拆分规避
        ArrayList<ArrayList<ApiCallRecord>> list = splitList(apiCallRecordList, 30);
        for (ArrayList<ApiCallRecord> data : list) {
            DataUploadManager.upload(packageName, data);
            if (HookModule.localCheckFileInput && HookModule.context != null) {
                Intent intent = new Intent(Constant.Intent.ACTION_WRITE_DATA);
                intent.setPackage(Constant.PackageName.SELF);
                intent.putExtra(Constant.Intent.EXTRA_NAME_PACKAGE_NAME, packageName);
                intent.putParcelableArrayListExtra(Constant.Intent.EXTRA_NAME_INVOKE_INFO, data);
                HookModule.context.sendBroadcast(intent);
            }
        }
    }

    public ApiCallRecord getLastInvokerRecord(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return null;
        }
        List<ApiCallRecord> apiCallRecordList = dataMap.get(packageName);
        if (apiCallRecordList == null || apiCallRecordList.isEmpty()) {
            return null;
        }
        return apiCallRecordList.get(apiCallRecordList.size() - 1);
    }

    public static <T> ArrayList<ArrayList<T>> splitList(ArrayList<T> list, int chunkSize) {
        ArrayList<ArrayList<T>> chunks = new ArrayList<>();
        int start = 0;
        while (start < list.size()) {
            int end = Math.min(list.size(), start + chunkSize);
            chunks.add(new ArrayList<>(list.subList(start, end)));
            start += chunkSize;
        }
        return chunks;
    }
}
