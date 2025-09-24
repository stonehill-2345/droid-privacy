package com.privacy2345.droidprivacy.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * ADB命令执行工具类
 *
 * @author : zhongjy@2345.com
 */
public class AdbCommandUtil {
    private static final String TAG = "AdbCommandUtil";

    /**
     * 杀死指定包名的应用
     *
     * @param packageName 包名
     * @return 是否成功
     */
    public static boolean killApp(String packageName) {
        String command = "am force-stop " + packageName;
        return executeShellCommand(command);
    }

    /**
     * 检查应用进程是否存活
     *
     * @param packageName 包名
     * @return 是否存活
     */
    public static boolean isAppAlive(String packageName) {
        String command = "ps | grep " + packageName;
        String result = executeShellCommandWithResult(command);
        return result != null && !result.trim().isEmpty();
    }

    /**
     * 执行shell命令
     *
     * @param command 命令
     * @return 是否成功
     */
    public static boolean executeShellCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", command});
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            Log.e(TAG, "执行命令失败: " + command + ", 错误: " + e.getMessage());
            return false;
        }
    }

    /**
     * 执行shell命令并返回结果
     *
     * @param command 命令
     * @return 执行结果
     */
    private static String executeShellCommandWithResult(String command) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", command});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
            reader.close();
            process.waitFor();
            return result.toString();
        } catch (Exception e) {
            Log.e(TAG, "执行命令失败: " + command + ", 错误: " + e.getMessage());
            return null;
        }
    }
} 