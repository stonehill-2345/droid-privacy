package com.privacy2345.droidprivacy.util;

import android.util.Log;

import com.privacy2345.droidprivacy.model.AppInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 应用自启动测试工具类<br>
 * 通过模拟系统广播事件来测试应用的自启动行为<br>
 * 主要功能：<br>
 * 1. 进程管理 - 杀死目标应用进程并监控其状态<br>
 * 2. 广播模拟 - 逐个发送系统广播并检测应用响应<br>
 * 3. 自启动检测 - 识别能够触发应用自启动的广播类型<br>
 * 4. 结果收集 - 记录所有导致自启动的广播及其描述<br>
 * 5. 进度反馈 - 通过回调接口提供测试进度和结果<br>
 * 6. 中断控制 - 支持测试过程的中断和恢复<br>
 * <p>
 * 测试原理：通过adb命令发送系统广播，观察应用是否自动启动
 * 用于检测应用是否存在违规的自启动行为
 *
 * @author : zhongjy@2345.com
 */
public class SelfLaunchTester {
    private static final String TAG = "TestSelfLaunchUtil";

    // 测试中断标志
    private static final AtomicBoolean isTestInterrupted = new AtomicBoolean(false);
    private static Thread currentTestThread = null;

    /**
     * 自启动测试结果回调接口
     */
    public interface TestResultCallback {
        /**
         * 测试开始
         */
        void onTestStart();

        /**
         * 测试进度更新
         *
         * @param currentIndex       当前测试的广播索引
         * @param totalCount         总广播数量
         * @param currentAction      当前正在测试的广播action
         * @param currentDescription 当前广播的中文描述
         * @param result             当前发现的可以导致自启动的广播
         */
        void onProgressUpdate(int currentIndex, int totalCount, String currentAction, String currentDescription, String result);

        /**
         * 测试完成
         *
         * @param finalResult 最终测试结果
         */
        void onTestComplete(String finalResult);

        /**
         * 测试失败
         *
         * @param errorMessage 错误信息
         */
        void onTestFailed(String errorMessage);

        /**
         * 测试被中断
         */
        void onTestInterrupted();
    }

    /**
     * 中断当前测试
     */
    public static void interruptTest() {
        isTestInterrupted.set(true);
        if (currentTestThread != null && currentTestThread.isAlive()) {
            currentTestThread.interrupt();
        }
    }

    /**
     * 检查是否有测试正在进行
     *
     * @return 是否有测试正在进行
     */
    public static boolean isTestRunning() {
        return currentTestThread != null && currentTestThread.isAlive();
    }

    /**
     * 开始自启动测试
     *
     * @param selectedApp 选中的应用
     * @param callback    测试结果回调
     */
    public static void startSelfLaunchTest(AppInfo selectedApp, TestResultCallback callback) {
        if (selectedApp == null) {
            callback.onTestFailed("请先选择需要测试的应用");
            return;
        }

        // 如果已有测试在进行，先中断
        if (isTestRunning()) {
            interruptTest();
        }

        // 重置中断标志
        isTestInterrupted.set(false);

        // 在后台线程中执行测试
        currentTestThread = new Thread(() -> {
            try {
                // 1. 杀死应用
                if (!AdbCommandUtil.killApp(selectedApp.getPackageName())) {
                    callback.onTestFailed("无法杀死应用进程，请手动关闭应用");
                    return;
                }

                Thread.sleep(1000); // 等待应用完全关闭

                // 检查是否被中断
                if (isTestInterrupted.get()) {
                    callback.onTestInterrupted();
                    return;
                }

                // 2. 确认应用进程不存在
                if (AdbCommandUtil.isAppAlive(selectedApp.getPackageName())) {
                    callback.onTestFailed("无法杀死应用进程，请手动关闭应用");
                    return;
                }

                // 3. 开始测试广播
                testBroadcasts(selectedApp, callback);

            } catch (Exception e) {
                if (isTestInterrupted.get()) {
                    callback.onTestInterrupted();
                } else {
                    callback.onTestFailed("测试过程中发生错误: " + e.getMessage());
                }
            }
        });

        currentTestThread.start();
    }

    /**
     * 测试广播
     *
     * @param selectedApp 选中的应用
     * @param callback    测试结果回调
     */
    private static void testBroadcasts(AppInfo selectedApp, TestResultCallback callback) {
        StringBuilder testResultBuilder = new StringBuilder();
        List<String> broadcastActions = new ArrayList<>(SystemBroadcastUtil.broadcastInfoMap.keySet());
        int totalCount = broadcastActions.size();

        callback.onTestStart();

        for (int i = 0; i < broadcastActions.size(); i++) {
            // 检查是否被中断
            if (isTestInterrupted.get()) {
                callback.onTestInterrupted();
                return;
            }

            String action = broadcastActions.get(i);
            String description = SystemBroadcastUtil.broadcastInfoMap.get(action);

            try {
                // 发送广播
                String cmd = "am broadcast -a " + action;
                AdbCommandUtil.executeShellCommand(cmd);

                // 等待500ms后检查应用是否启动
                Thread.sleep(500);

                // 检查应用进程是否存活
                if (AdbCommandUtil.isAppAlive(selectedApp.getPackageName())) {
                    // 记录可以导致自启动的广播
                    String result = action + " (" + description + ")\n";
                    testResultBuilder.append(result);

                    // 更新进度
                    callback.onProgressUpdate(i + 1, totalCount, action, description, testResultBuilder.toString());

                    // 再次杀死应用，继续测试下一个广播
                    AdbCommandUtil.killApp(selectedApp.getPackageName());
                    Thread.sleep(1000);
                } else {
                    // 更新进度（即使没有发现自启动广播也要更新进度）
                    callback.onProgressUpdate(i + 1, totalCount, action, description, testResultBuilder.toString());
                }

            } catch (InterruptedException e) {
                Log.d(TAG, "测试被中断");
                callback.onTestInterrupted();
                return;
            } catch (Exception e) {
                Log.e(TAG, "测试广播失败: " + action + ", 错误: " + e.getMessage());
                // 继续测试下一个广播，不中断整个测试
            }
        }

        // 检查是否被中断
        if (isTestInterrupted.get()) {
            callback.onTestInterrupted();
            return;
        }

        // 测试完成
        String finalResult;
        if (testResultBuilder.length() == 0) {
            finalResult = "测试完成：未发现可导致应用自启动的广播";
        } else {
            finalResult = "测试完成！\n\n可导致自启动的广播：\n" + testResultBuilder;
        }
        callback.onTestComplete(finalResult);
    }
}
