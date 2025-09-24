package com.privacy2345.droidprivacy.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * API调用记录模型类<br>
 * 记录隐私API调用的详细信息，支持Parcelable序列化<br>
 * 用于跨进程传输和持久化存储<br>
 * 包含调用时间、调用者信息、方法详情、参数和堆栈信息等
 *
 * @author : zhongjy@2345.com
 */
public class ApiCallRecord implements Parcelable {

    public String serialNumber;

    public long timeMills;

    public String timestamp;

    public String invokerName;

    public String invokerCategory;

    public String invokerRule;

    public String invokerProcess;

    public String invokerMethod;

    public String invokerMethodArgs;

    public String invokerStack;

    public ApiCallRecord() {
    }


    protected ApiCallRecord(Parcel in) {
        serialNumber = in.readString();
        timeMills = in.readLong();
        timestamp = in.readString();
        invokerName = in.readString();
        invokerCategory = in.readString();
        invokerRule = in.readString();
        invokerProcess = in.readString();
        invokerMethod = in.readString();
        invokerMethodArgs = in.readString();
        invokerStack = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(serialNumber);
        dest.writeLong(timeMills);
        dest.writeString(timestamp);
        dest.writeString(invokerName);
        dest.writeString(invokerCategory);
        dest.writeString(invokerRule);
        dest.writeString(invokerProcess);
        dest.writeString(invokerMethod);
        dest.writeString(invokerMethodArgs);
        dest.writeString(invokerStack);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ApiCallRecord> CREATOR = new Creator<ApiCallRecord>() {
        @Override
        public ApiCallRecord createFromParcel(Parcel in) {
            return new ApiCallRecord(in);
        }

        @Override
        public ApiCallRecord[] newArray(int size) {
            return new ApiCallRecord[size];
        }
    };
}
