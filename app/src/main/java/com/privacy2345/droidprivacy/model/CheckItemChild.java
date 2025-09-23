package com.privacy2345.droidprivacy.model;

/**
 * 检查项子项模型类，表示具体的隐私检查规则项，包含规则名称和是否选中的状态
 *
 * @author : zhongjy@2345.com
 */
public class CheckItemChild {
    private final String childName;
    private boolean isSelected;

    public CheckItemChild(String childName, boolean isSelected) {
        this.childName = childName;
        this.isSelected = isSelected;
    }

    public String getChildName() {
        return childName;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
