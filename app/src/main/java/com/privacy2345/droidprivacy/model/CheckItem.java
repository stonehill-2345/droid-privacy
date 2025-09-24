package com.privacy2345.droidprivacy.model;

import java.util.List;

/**
 * 隐私检查项分组模型类<br>
 * 表示一个隐私检查分类及其包含的子检查项列表<br>
 * 用于在ExpandableListView中展示分组的检查规则<br>
 * 主要特性：<br>
 * 1. 分组管理 - 将相关的检查规则组织成逻辑分组<br>
 * 2. 层级结构 - 支持父级分组和子级检查项的层级关系<br>
 * 3. UI适配 - 为ExpandableListView提供数据源<br>
 * 4. 描述生成 - 自动生成分组的描述信息
 *
 * @author : zhongjy@2345.com
 */
public class CheckItem {
    private final String groupName;
    private final List<CheckItemChild> childItems;

    public CheckItem(String groupName, List<CheckItemChild> childItems) {
        this.groupName = groupName;
        this.childItems = childItems;
    }

    public String getGroupName() {
        return groupName;
    }

    public List<CheckItemChild> getChildItems() {
        return childItems;
    }

    public String getDesc() {
        if (childItems == null || childItems.isEmpty()) {
            return "无检测规则";
        }
        int selectedCount = 0;
        for (CheckItemChild child : childItems) {
            if (child.isSelected()) {
                selectedCount++;
            }
        }
        return "规则:" + childItems.size() + " 启用:" + selectedCount;
    }
}