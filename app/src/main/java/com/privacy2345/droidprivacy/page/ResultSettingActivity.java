package com.privacy2345.droidprivacy.page;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.privacy2345.droidprivacy.R;
import com.privacy2345.droidprivacy.constant.Constant;
import com.privacy2345.droidprivacy.output.DataWriteExcelManager;
import com.privacy2345.droidprivacy.util.PreferenceManager;

import java.io.File;

/**
 * 结果设置Activity
 * 配置隐私检查结果的输出方式和相关参数
 * 主要功能：
 * 1. 重载过滤设置 - 配置是否过滤重复的API调用
 * 2. 本地文件输出 - 配置是否将结果保存到本地Excel文件
 * 3. 存储权限管理 - 申请和管理文件写入权限
 * 4. 文件管理 - 删除已生成的检查结果文件
 *
 * @author : zhongjy@2345.com
 */
public class ResultSettingActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;

    private CheckBox overloadFilterCb;

    private CheckBox localCheckFileInputCb;
    private LinearLayout permissionStatusLl;
    private TextView permissionStatusTv;
    private TextView resDirPathTv;
    private Button deleteBt;

    private boolean permissionGranted = false;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_setting);
        preferenceManager = PreferenceManager.getInstance(this);
        initOverloadFilter();
        initLocalInput();
    }

    private void initOverloadFilter() {
        overloadFilterCb = findViewById(R.id.cb_overload_filter);
        overloadFilterCb.setChecked(preferenceManager.getBoolean(Constant.Cache.OVERLOAD_FILTER, true));
        overloadFilterCb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferenceManager.putBoolean(Constant.Cache.OVERLOAD_FILTER, isChecked);
        });
    }

    private void initLocalInput() {
        localCheckFileInputCb = findViewById(R.id.cb_local_check_file_input);
        permissionStatusLl = findViewById(R.id.ll_result_setting);
        permissionStatusTv = findViewById(R.id.tv_permission);
        resDirPathTv = findViewById(R.id.tv_dir_path);
        deleteBt = findViewById(R.id.btn_delete);

        localCheckFileInputCb.setChecked(preferenceManager.getBoolean(Constant.Cache.LOCAL_CHECK_FILE_INPUT, true));
        localCheckFileInputCb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferenceManager.putBoolean(Constant.Cache.LOCAL_CHECK_FILE_INPUT, isChecked);
        });

        permissionStatusLl.setOnClickListener(v -> {
            if (!permissionGranted) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
            }
        });
        updatePermissionStatus();

        deleteBt.setOnClickListener((view) -> {
            if (permissionGranted) {
                clearDir(new File(Environment.getExternalStorageDirectory() + File.separator + DataWriteExcelManager.DIC_NAME));
            } else {
                clearDir(new File(getExternalFilesDir(null).getAbsolutePath() + File.separator + DataWriteExcelManager.DIC_NAME));
            }
            DataWriteExcelManager.getInstance().clear();
            Toast.makeText(ResultSettingActivity.this, "已删除完毕", Toast.LENGTH_SHORT).show();
        });
    }

    private void updatePermissionStatus() {
        permissionGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (permissionGranted) {
            permissionStatusTv.setText("已获取存储权限");
            resDirPathTv.setText(Environment.getExternalStorageDirectory() + File.separator + DataWriteExcelManager.DIC_NAME);
        } else {
            permissionStatusTv.setText("未获取存储权限，点击申请");
            resDirPathTv.setText(getExternalFilesDir(null).getAbsolutePath() + File.separator + DataWriteExcelManager.DIC_NAME);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            updatePermissionStatus();
        }
    }

    private void clearDir(File file) {
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File temp : files) {
                    temp.delete();
                }
            }
        }
    }
}
