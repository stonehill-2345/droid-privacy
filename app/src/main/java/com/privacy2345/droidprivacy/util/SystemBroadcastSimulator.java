package com.privacy2345.droidprivacy.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 模拟发送系统广播工具类
 *
 * @author : zhongjy@2345.com
 */
public class SystemBroadcastSimulator {

    private static final String TAG = "SimulateBroadcastUtil";
    private static Thread broadcastThread;
    private static final AtomicBoolean isBroadcastInterrupted = new AtomicBoolean(false);

    /**
     * 广播发送结果回调接口
     */
    public interface BroadcastResultCallback {
        /**
         * 广播发送开始
         */
        void onBroadcastStart();

        /**
         * 广播发送进度更新
         *
         * @param currentIndex       当前发送的广播索引
         * @param totalCount         总广播数量
         * @param currentAction      当前正在发送的广播action
         * @param currentDescription 当前广播的中文描述
         */
        void onProgressUpdate(int currentIndex, int totalCount, String currentAction, String currentDescription);

        /**
         * 广播发送完成
         */
        void onBroadcastComplete();

        /**
         * 广播发送失败
         *
         * @param errorMessage 错误信息
         */
        void onBroadcastFailed(String errorMessage);

        /**
         * 广播发送被中断
         */
        void onBroadcastInterrupted();
    }

    /**
     * 中断当前广播发送
     */
    public static void interruptBroadcast() {
        isBroadcastInterrupted.set(true);
        if (broadcastThread != null && broadcastThread.isAlive()) {
            broadcastThread.interrupt();
        }
    }

    /**
     * 检查是否有广播发送正在进行
     *
     * @return 是否有广播发送正在进行
     */
    public static boolean isBroadcastRunning() {
        return broadcastThread != null && broadcastThread.isAlive();
    }

    public static void sendSystemBroadcast(boolean ignoreCrashBroadcast, BroadcastResultCallback callback) {
        if (broadcastThread != null && broadcastThread.isAlive()) {
            broadcastThread.interrupt();
        }
        
        isBroadcastInterrupted.set(false);
        
        // 创建新线程并执行广播发送任务
        broadcastThread = new Thread(() -> {
            try {
                List<String> broadcastActions = new ArrayList<>(SystemBroadcastUtil.broadcastInfoMap.keySet());
                int totalCount = broadcastActions.size();
                
                callback.onBroadcastStart();
                
                for (int i = 0; i < broadcastActions.size(); i++) {
                    // 检查是否被中断
                    if (isBroadcastInterrupted.get()) {
                        callback.onBroadcastInterrupted();
                        return;
                    }
                    
                    String action = broadcastActions.get(i);
                    String description = SystemBroadcastUtil.broadcastInfoMap.get(action);
                    
                    if (ignoreCrashBroadcast && SystemBroadcastUtil.crashBroadcastActionSet.contains(action)) {
                        Log.d(TAG, "跳过发送可能导致模拟器桌面崩溃的广播: " + action + " - " + description);
                        // 即使跳过也要更新进度
                        callback.onProgressUpdate(i + 1, totalCount, action, description);
                        continue;
                    }

                    String cmd = "am broadcast -a " + action;
                    Log.d(TAG, "正在发送广播: " + action + " - " + description);
                    executeShellCommand(cmd);

                    // 更新进度
                    callback.onProgressUpdate(i + 1, totalCount, action, description);

                    try {
                        Thread.sleep(300); // 可选：发送广播间隔，避免太快
                    } catch (InterruptedException e) {
                        Log.d(TAG, "线程休眠被中断，停止广播发送");
                        callback.onBroadcastInterrupted();
                        return;
                    }
                }

                // 检查是否被中断
                if (isBroadcastInterrupted.get()) {
                    callback.onBroadcastInterrupted();
                    return;
                }

                Log.d(TAG, "广播发送完成");
                callback.onBroadcastComplete();
                
            } catch (Exception e) {
                if (isBroadcastInterrupted.get()) {
                    callback.onBroadcastInterrupted();
                } else {
                    callback.onBroadcastFailed("广播发送过程中发生错误: " + e.getMessage());
                }
            }
        });

        broadcastThread.start();
    }

    // 保持向后兼容的方法
    public static void sendSystemBroadcast(boolean ignoreCrashBroadcast) {
        sendSystemBroadcast(ignoreCrashBroadcast, new BroadcastResultCallback() {
            @Override
            public void onBroadcastStart() {
                Log.d(TAG, "开始发送系统广播");
            }

            @Override
            public void onProgressUpdate(int currentIndex, int totalCount, String currentAction, String currentDescription) {
                Log.d(TAG, String.format("广播发送进度: %d/%d - %s", currentIndex, totalCount, currentAction));
            }

            @Override
            public void onBroadcastComplete() {
                Log.d(TAG, "广播发送完成");
            }

            @Override
            public void onBroadcastFailed(String errorMessage) {
                Log.e(TAG, "广播发送失败: " + errorMessage);
            }

            @Override
            public void onBroadcastInterrupted() {
                Log.d(TAG, "广播发送被中断");
            }
        });
    }

    private static void executeShellCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", command});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                Log.d(TAG, line);
            }
            reader.close();
            process.waitFor();
        } catch (Exception e) {
            Log.e(TAG, "发送广播失败: " + e.getMessage());
        }
    }
}
