package com.privacy2345.droidprivacy.page;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.privacy2345.droidprivacy.R;
import com.privacy2345.droidprivacy.constant.Constant;
import com.privacy2345.droidprivacy.hook.HookConfigManager;
import com.privacy2345.droidprivacy.model.CheckItem;
import com.privacy2345.droidprivacy.util.PreferenceManager;
import com.privacy2345.droidprivacy.util.StringHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 主Activity
 * 隐私检查器的主界面，提供核心功能入口
 * 主要功能：
 * 1. 隐私检查规则配置 - 选择需要检测的隐私行为类型
 * 2. 规则管理 - 支持全选、展开/折叠等操作
 * 3. 配置保存 - 保存用户选择的检测规则
 * 4. 功能导航 - 跳转到检查工具和结果设置页面
 *
 * @author : zhongjy@2345.com
 */
public class MainActivity extends AppCompatActivity {

    private TextView expandCollapseTv;
    private TextView checkCountTv;
    private TextView selectedCountTv;
    private LinearLayout resultSettingLv;
    private CheckBox totalSelectCb;
    private Button saveConfigBtn;
    private Button checkToolBtn;
    private ExpandableListView checkItemLv;
    private CheckItemAdapter checkItemAdapter;
    private Set<String> checkItemSet = new HashSet<>();
    private List<CheckItem> checkItemList = new ArrayList<>();

    private boolean isInitConfig;
    private boolean isExpand = false;
    private int checkItemCount;

    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultSettingLv = findViewById(R.id.ll_result_setting);
        expandCollapseTv = findViewById(R.id.tv_expand_collapse);
        checkCountTv = findViewById(R.id.tv_check_count);
        selectedCountTv = findViewById(R.id.tv_select_count);
        totalSelectCb = findViewById(R.id.cb_select_total);
        checkItemLv = findViewById(R.id.lv_check_item);
        saveConfigBtn = findViewById(R.id.btn_save_config);
        checkToolBtn = findViewById(R.id.btn_check_tool);

        resultSettingLv.setOnClickListener(v -> startActivity(new Intent(this, ResultSettingActivity.class)));

        totalSelectCb.setOnClickListener(v -> {
            if (totalSelectCb.isChecked()) {
                checkItemAdapter.selectAll();
            } else {
                checkItemAdapter.clearSelection();
            }
        });

        expandCollapseTv.setOnClickListener(v -> {
            isExpand = !isExpand;
            updateExpandStatues();
        });

        saveConfigBtn.setOnClickListener(v -> saveConfig());
        checkToolBtn.setOnClickListener(v -> startActivity(new Intent(this, CheckToolActivity.class)));

        initConfig();
    }

    private void initConfig() {
        preferenceManager = PreferenceManager.getInstance(this);
        if (preferenceManager.contains(Constant.Cache.CHECK_ITEM)) {
            isInitConfig = false;
            String checkItemString = preferenceManager.getString(Constant.Cache.CHECK_ITEM, null);
            checkItemSet = StringHelper.splitToSet(checkItemString, "、", checkItemSet);
        } else {
            isInitConfig = true;
        }

        checkItemList = HookConfigManager.getInstance().getCheckItemList(isInitConfig, checkItemSet);
        checkItemAdapter = new CheckItemAdapter(this, checkItemList, new CheckItemAdapter.OnSelectedChangeListener() {
            @Override
            public void onPrepare(int count) {
                checkItemCount = count;
                checkCountTv.setText("（数量：" + count);
            }

            @Override
            public void onChange(int selectCount) {
                totalSelectCb.setChecked(selectCount == checkItemCount);
                selectedCountTv.setText("，启用：" + selectCount + ")");
            }
        });
        checkItemLv.setAdapter(checkItemAdapter);
        updateExpandStatues();
    }

    private void updateExpandStatues() {
        expandCollapseTv.setText(isExpand ? "折叠∧" : "展开∨");
        for (int i = 0; i < checkItemAdapter.getGroupCount(); i++) {
            if (isExpand) {
                checkItemLv.expandGroup(i);
            } else {
                checkItemLv.collapseGroup(i);
            }
        }
    }

    private void showShortToast(String toast) {
        Toast.makeText(MainActivity.this, toast, Toast.LENGTH_SHORT).show();
    }

    private void saveConfig() {
        Set<String> selectedCheckItemSet = checkItemAdapter.getSelectedCheckItem();
        if (selectedCheckItemSet == null || selectedCheckItemSet.isEmpty()) {
            showShortToast("请选择需要检测的规则");
            return;
        }
        preferenceManager.putString(Constant.Cache.CHECK_ITEM, StringHelper.join(selectedCheckItemSet.toArray(), "、"));
        showShortToast("配置已保存，重启待检测应用即可生效");
    }

}
