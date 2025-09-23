package com.privacy2345.droidprivacy.page;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputFilter;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.privacy2345.droidprivacy.R;
import com.privacy2345.droidprivacy.constant.Constant;
import com.privacy2345.droidprivacy.model.AppInfo;
import com.privacy2345.droidprivacy.util.AppListUtil;
import com.privacy2345.droidprivacy.util.PreferenceManager;
import com.privacy2345.droidprivacy.util.SystemBroadcastSimulator;
import com.privacy2345.droidprivacy.util.SelfLaunchTester;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 检查工具Activity，提供隐私检查相关的辅助工具功能<br>
 * 主要功能：<br>
 * 1. 手机品牌模拟 - 支持模拟不同厂商的设备信息<br>
 * 2. 系统广播发送 - 模拟发送系统广播进行测试<br>
 * 3. 自启动测试 - 测试应用的自启动行为
 *
 * @author : zhongjy@2345.com
 */
public class CheckToolActivity extends AppCompatActivity {
    private TextView brandTv;
    private EditText delaySendBroadcastEt;
    private Button delaySenBroadcastBtn;
    private TextView broadcastProgressTv;

    private TextView testAppTv;
    private Button testSelfLaunchBtn;
    private TextView testProgressTv;
    private TextView testSelfLaunchResTv;

    private Handler handler;
    private AppInfo selectedApp;

    private String brand;
    private int brandIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_tool);
        initBrandSelect();
        initSendBroadcast();
        initTestSelfLaunch();
    }

    private void initBrandSelect() {
        brandTv = findViewById(R.id.tv_brand);
        brandTv.setOnClickListener(v -> showModeBrandDialog());
        brand = PreferenceManager.getInstance(this).getString(Constant.Cache.MOCK_BRAND, Build.BRAND);
        updateSelectBrand();
    }

    private void initSendBroadcast() {
        delaySendBroadcastEt = findViewById(R.id.et_delay);
        delaySenBroadcastBtn = findViewById(R.id.btn_send_broadcast);
        broadcastProgressTv = findViewById(R.id.tv_broadcast_progress);

        delaySendBroadcastEt.setFilters(new InputFilter[]{(source, start, end, dest, dstart, dend) -> {
            String result = dest.subSequence(0, dstart) + source.toString() + dest.subSequence(dend, dest.length());

            // 允许空字符串（支持删除）
            if (result.isEmpty()) {
                return null;
            }

            // 不允许前导0（如"01"），可选：放开则注释掉此行
            if (result.length() > 1 && result.startsWith("0")) {
                return "";
            }

            try {
                int value = Integer.parseInt(result);
                if (value < 0) {
                    return "";
                }
            } catch (NumberFormatException e) {
                return "";
            }
            return null;
        }});

        delaySenBroadcastBtn.setOnClickListener(v -> startBroadcastSend());
    }

    /**
     * 开始发送广播
     */
    private void startBroadcastSend() {
        // 如果广播发送正在进行，先中断发送
        if (SystemBroadcastSimulator.isBroadcastRunning()) {
            SystemBroadcastSimulator.interruptBroadcast();
            Toast.makeText(this, "已终止当前广播发送", Toast.LENGTH_SHORT).show();
            // 清空进度显示
            broadcastProgressTv.setText("等待开始发送广播...");
            return;
        }

        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }
        int delayTimeMills = getDalySendTime();
        Toast.makeText(this, delayTimeMills + "秒后开始模拟发送系统广播", Toast.LENGTH_SHORT).show();

        handler.postDelayed(() -> {
            SystemBroadcastSimulator.sendSystemBroadcast(false, new SystemBroadcastSimulator.BroadcastResultCallback() {
                @Override
                public void onBroadcastStart() {
                    runOnUiThread(() -> {
                        broadcastProgressTv.setText("开始发送系统广播...");
                    });
                }

                @Override
                public void onProgressUpdate(int currentIndex, int totalCount, String currentAction, String currentDescription) {
                    runOnUiThread(() -> {
                        // 更新广播发送进度
                        String progressText = String.format("广播发送进度: %d/%d\n当前发送: %s\n中文描述: %s",
                                currentIndex, totalCount, currentAction, currentDescription);
                        broadcastProgressTv.setText(progressText);
                    });
                }

                @Override
                public void onBroadcastComplete() {
                    runOnUiThread(() -> {
                        broadcastProgressTv.setText("广播发送完成");
                        Toast.makeText(CheckToolActivity.this, "系统广播发送完成", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onBroadcastFailed(String errorMessage) {
                    runOnUiThread(() -> {
                        broadcastProgressTv.setText("广播发送失败");
                        Toast.makeText(CheckToolActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onBroadcastInterrupted() {
                    runOnUiThread(() -> {
                        broadcastProgressTv.setText("广播发送已终止");
                        Toast.makeText(CheckToolActivity.this, "广播发送已终止", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }, delayTimeMills * 1000L);
    }

    private int getDalySendTime() {
        String input = delaySendBroadcastEt.getText().toString().trim();
        if (!input.isEmpty()) {
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    private void initTestSelfLaunch() {
        testAppTv = findViewById(R.id.tv_test_app);
        testSelfLaunchBtn = findViewById(R.id.btn_test_self_launch);
        testProgressTv = findViewById(R.id.tv_test_progress);
        testSelfLaunchResTv = findViewById(R.id.tv_self_launch_result);

        // 设置应用选择点击事件
        testAppTv.setOnClickListener(v -> showAppSelectionDialog());

        // 设置自启动测试点击事件
        testSelfLaunchBtn.setOnClickListener(v -> startSelfLaunchTest());
    }

    /**
     * 显示应用选择对话框
     */
    private void showAppSelectionDialog() {
        // 如果测试正在进行，先中断测试
        if (SelfLaunchTester.isTestRunning()) {
            SelfLaunchTester.interruptTest();
            Toast.makeText(this, "已终止当前测试", Toast.LENGTH_SHORT).show();
            // 清空进度和结果显示
            testProgressTv.setText("等待开始测试...");
            testSelfLaunchResTv.setText("测试结果将在这里显示");
        }

        List<AppInfo> appList = AppListUtil.getInstalledApps(this);

        if (appList.isEmpty()) {
            Toast.makeText(this, "未找到已安装的应用", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] appNames = new String[appList.size()];
        for (int i = 0; i < appList.size(); i++) {
            appNames[i] = appList.get(i).toString();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择需要测试的应用");
        builder.setItems(appNames, (dialog, which) -> {
            selectedApp = appList.get(which);
            testAppTv.setText(selectedApp.getAppName());
            Toast.makeText(this, "已选择: " + selectedApp.getAppName(), Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }

    /**
     * 开始自启动测试
     */
    private void startSelfLaunchTest() {
        // 如果测试正在进行，先中断测试
        if (SelfLaunchTester.isTestRunning()) {
            SelfLaunchTester.interruptTest();
            Toast.makeText(this, "已终止当前测试", Toast.LENGTH_SHORT).show();
            // 清空进度和结果显示
            testProgressTv.setText("等待开始测试...");
            testSelfLaunchResTv.setText("测试结果将在这里显示");
            return;
        }

        SelfLaunchTester.startSelfLaunchTest(selectedApp, new SelfLaunchTester.TestResultCallback() {
            @Override
            public void onTestStart() {
                runOnUiThread(() -> {
                    testProgressTv.setText("开始测试自启动广播...");
                    testSelfLaunchResTv.setText("等待测试结果...");
                });
            }

            @Override
            public void onProgressUpdate(int currentIndex, int totalCount, String currentAction, String currentDescription, String result) {
                runOnUiThread(() -> {
                    // 更新测试进度
                    String progressText = String.format("测试进度: %d/%d\n当前测试: %s\n中文描述: %s",
                            currentIndex, totalCount, currentAction, currentDescription);
                    testProgressTv.setText(progressText);

                    // 更新测试结果
                    if (result != null && !result.trim().isEmpty()) {
                        testSelfLaunchResTv.setText("已发现的可以导致自启动的广播：\n" + result);
                    }
                });
            }

            @Override
            public void onTestComplete(String finalResult) {
                runOnUiThread(() -> {
                    testProgressTv.setText("测试完成");
                    testSelfLaunchResTv.setText(finalResult);
                    Toast.makeText(CheckToolActivity.this, "自启动测试完成", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onTestFailed(String errorMessage) {
                runOnUiThread(() -> {
                    testProgressTv.setText("测试失败");
                    Toast.makeText(CheckToolActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    testSelfLaunchResTv.setText("测试失败：" + errorMessage);
                });
            }

            @Override
            public void onTestInterrupted() {
                runOnUiThread(() -> {
                    testProgressTv.setText("测试已终止");
                    testSelfLaunchResTv.setText("测试已被用户终止");
                    Toast.makeText(CheckToolActivity.this, "测试已终止", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    public void showModeBrandDialog() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("请选择需要模拟的手机品牌");

        Set<String> brandSet = new HashSet<>(Arrays.asList("xiaomi", "huawei", "honor", "oppo", "vivo", "meizu", "samsung", Build.BRAND));

        String[] brandArray = brandSet.toArray(new String[0]);

        brandIndex = 0;
        for (int i = 0; i < brandArray.length; i++) {
            if (TextUtils.equals(brandArray[i], brand)) {
                brandIndex = i;
                break;
            }
        }

        alertBuilder.setSingleChoiceItems(brandArray, brandIndex, (dialog, which) -> {
            brandIndex = which;
        });

        alertBuilder.setPositiveButton("确定", (dialogInterface, i) -> {
            brand = brandArray[brandIndex];
            updateSelectBrand();
            dialogInterface.dismiss();
            PreferenceManager.getInstance(this).putString(Constant.Cache.MOCK_BRAND, brand);
        });
        alertBuilder.setNegativeButton("取消", (dialogInterface, i) -> dialogInterface.dismiss());

        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();
    }


    public void updateSelectBrand() {
        if (brand == null || brand.isEmpty()) {
            brandTv.setText(Build.BRAND);
        } else {
            brandTv.setText(brand);
        }
    }
}
