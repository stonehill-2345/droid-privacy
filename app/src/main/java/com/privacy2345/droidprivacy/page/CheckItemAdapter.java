package com.privacy2345.droidprivacy.page;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.privacy2345.droidprivacy.R;
import com.privacy2345.droidprivacy.model.CheckItem;
import com.privacy2345.droidprivacy.model.CheckItemChild;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 检查项适配器，用于在ExpandableListView中展示隐私检查规则的分组列表<br>
 * 主要功能：<br>
 * 1. 分组展示 - 支持展开/折叠显示检查规则分组<br>
 * 2. 选择管理 - 支持单选、全选、分组选择等操作<br>
 * 3. 状态同步 - 自动同步父子项的选择状态<br>
 * 4. 高亮显示 - 对"高敏"规则进行红色高亮显示<br>
 * 5. 回调通知 - 通过接口通知选择状态变化
 *
 * @author : zhongjy@2345.com
 */
public class CheckItemAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<CheckItem> listData;
    private HashMap<String, Boolean> groupCheckedState;
    private Set<String> selectedCheckItemSet;
    private OnSelectedChangeListener listener;
    private int checkChildCount;

    public CheckItemAdapter(Context context, List<CheckItem> listData, OnSelectedChangeListener listener) {
        this.context = context;
        this.listData = listData;
        this.groupCheckedState = new HashMap<>();
        this.listener = listener;
        selectedCheckItemSet = new HashSet<>();
        for (CheckItem checkItem : listData) {
            boolean isAllChecked = true;
            for (CheckItemChild checkItemChild : checkItem.getChildItems()) {
                if (checkItemChild.isSelected()) {
                    selectedCheckItemSet.add(checkItemChild.getChildName());
                } else {
                    isAllChecked = false;
                }
                checkChildCount = checkChildCount + 1;
            }
            groupCheckedState.put(checkItem.getGroupName(), isAllChecked);
        }
        if (listener != null) {
            listener.onPrepare(checkChildCount);
            listener.onChange(selectedCheckItemSet.size());
        }
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.listData.get(groupPosition).getChildItems().get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        CheckItemChild child = (CheckItemChild) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_child, null);
        }
        TextView txtListChild = convertView.findViewById(R.id.tv_name);
        CheckBox checkBox = convertView.findViewById(R.id.cb_child);

        SpannableString spannableString = new SpannableString(child.getChildName());
        String target = "高敏";
        int start = child.getChildName().indexOf(target);
        while (start >= 0) {
            int end = start + target.length();
            spannableString.setSpan(new ForegroundColorSpan(Color.RED), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            start = child.getChildName().indexOf(target, end);
        }
        txtListChild.setText(spannableString);
        checkBox.setChecked(child.isSelected());

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            child.setSelected(isChecked);
            updateCheckItemChange(child, isChecked);
            updateGroupSelectionState(groupPosition);
        });

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.listData.get(groupPosition).getChildItems().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.listData.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.listData.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        CheckItem group = (CheckItem) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_group, null);
        }
        TextView nameTv = convertView.findViewById(R.id.tv_group);
        TextView descTv = convertView.findViewById(R.id.tv_group_desc);
        CheckBox checkBox = convertView.findViewById(R.id.cb_group);

        nameTv.setText(group.getGroupName());
        descTv.setText(group.getDesc());
        if (groupCheckedState.containsKey(group.getGroupName())) {
            checkBox.setChecked(groupCheckedState.get(group.getGroupName()));
        } else {
            checkBox.setChecked(false);
        }

        checkBox.setOnClickListener(v -> {
            selectGroup(groupPosition, checkBox.isChecked());
        });

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private void selectGroup(int groupPosition, boolean isSelected) {
        CheckItem group = (CheckItem) getGroup(groupPosition);
        for (CheckItemChild child : group.getChildItems()) {
            child.setSelected(isSelected);
            updateCheckItemChange(child, isSelected);
        }
        groupCheckedState.put(group.getGroupName(), isSelected);
        notifyDataSetChanged();
    }

    private void updateGroupSelectionState(int groupPosition) {
        CheckItem group = (CheckItem) getGroup(groupPosition);
        boolean allSelected = true;
        for (CheckItemChild child : group.getChildItems()) {
            if (!child.isSelected()) {
                allSelected = false;
                break;
            }
        }
        groupCheckedState.put(group.getGroupName(), allSelected);
        notifyDataSetChanged();
    }

    private void updateCheckItemChange(CheckItemChild checkItemChild, boolean isSelected) {
        if (isSelected) {
            selectedCheckItemSet.add(checkItemChild.getChildName());
        } else {
            selectedCheckItemSet.remove(checkItemChild.getChildName());
        }
        if (listener != null) {
            listener.onChange(selectedCheckItemSet.size());
        }
    }

    public void selectAll() {
        for (CheckItem group : listData) {
            selectGroup(listData.indexOf(group), true);
        }
    }

    public void clearSelection() {
        for (CheckItem group : listData) {
            selectGroup(listData.indexOf(group), false);
        }
    }

    public Set<String> getSelectedCheckItem() {
        return selectedCheckItemSet;
    }

    public interface OnSelectedChangeListener {
        void onPrepare(int count);

        void onChange(int selectCount);
    }
}

