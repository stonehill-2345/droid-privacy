package com.privacy2345.droidprivacy.constant;

/**
 * 项目中使用的相关常量定义
 *
 * @author : zhongjy@2345.com
 */
public class Constant {

    public interface Cache {
        String SP_NAME = "privacy_checker";
        String CHECK_ITEM = "check_item";
        String MOCK_BRAND = "mock_brand";
        String OVERLOAD_FILTER = "overload_filter";
        String LOCAL_CHECK_FILE_INPUT = "local_check_file_input";
    }

    public interface PackageName {

        String SELF = "com.privacy2345.droidprivacy";
    }

    public interface Intent {
        String ACTION_WRITE_DATA = "com.privacy2345.droidprivacy.write.data";
        String EXTRA_NAME_PACKAGE_NAME = "packageName";
        String EXTRA_NAME_INVOKE_INFO = "invokerInfo";
    }

    public static String COMMON_TAG = "DroidPrivacy";

    public static String AUTHORITY = "com.privacy2345.droidprivacy.provider";

}
