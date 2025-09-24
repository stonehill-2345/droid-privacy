package com.privacy2345.droidprivacy.constant;

/**
 * 检测规则
 *
 * @author : zhongjy@2345.com
 */
public class Behavior {

    /**
     * 检测规则分类
     */
    public interface Category {
        String ACCOUNT = "账户信息";
        String RUNNING_APP = "运行列表";
        String APP_LIST = "应用列表";
        String WIFI = "wifi";
        String IMEI = "imei";
        String IMSI = "imsi";
        String LOCATE = "定位";
        String SIM = "运营商信息";
        String PERMISSION = "权限";
        String SERIAL = "设备序列号";
        String BLUETOOTH = "蓝牙";
        String INET_ADDRESS = "ip地址";
        String CONTENT_PROVIDER = "contentProvider";
        String STORAGE = "存储";
        String MAC = "mac";
        String SYSTEM_PROPERTIES = "系统属性(调用多！)";
        String SENSOR = "传感器";
        String ANDROID_ID = "AndroidId";
        String APP_INFO = "应用信息";
        String BROWSER_BOOKMARKS = "浏览器书签";
        String CELL_INFO = "移动网参数";
        String MEDIA = "多媒体";
        String MSA_ID = "移动安全联盟设备标识";
        String NET_INFO = "网络信息(调用多！)";
        String PHONE_CALL_STATE = "手机呼叫状态";
        String PHONE_DATA = "手机数据";
        String PHONE_SERVICE_STATE = "手机服务状态";
        String SHELL = "shell命令";
        String CAMERA = "摄像头";
        String FILE = "文件";
        String BOOT_ID = "系统启动标识boot_id-高敏(小米)";
        String BROADCAST = "系统广播";
    }

    /**
     * 检测规则
     */
    public interface Rule {
        String GET_ACCOUNT = "获取账户信息-高敏";
        String ADD_ACCOUNT = "添加账户-高敏";
        String REMOVE_ACCOUNT = "移除账户-高敏";

        String GET_RUNNING_APP = "获取运行列表-高敏";
        String GET_APP_LIST = "获取应用列表-高敏";

        String WIFI_MAC = "获取wifi的mac地址";
        String WIFI_IP = "获取wifi的ip地址";
        String WIFI_SSID = "获取当前连接wifi的名称 SSID";
        String WIFI_BSSID = "当前连接wifi设备的mac地址 BSSID";
        String WIFI_RSSI = "当前连接wifi的信号强度";
        String SCAN_WIFI = "扫描wifi";
        String GET_SCAN_WIFI_RESULTS = "获取wifi扫描结果";
        String WIFI_INFO = "获取wifi信息";

        String IMEI = "获取imei-高敏";
        String IMSI = "获取imsi-高敏";

        String CELL_LOCATION = "基站定位-高敏";
        String REQUEST_LOCATION = "请求位置信息更新-高敏";
        String GET_LAST_KNOW_LOCATION = "获取最后已知位置-高敏";
        String GET_CURRENT_LOCATION = "获取当前位置-高敏";
        String LOCATION_INFO = "获取详细位置信息-高敏";


        String SIM_SERIAL_NUMBER = "获取sim卡的序列号 ICCID-高敏";
        String SIM_OPERATOR = "获取sim卡运营商编号";
        String SIM_OPERATOR_NAME = "获取sim卡运营商名称";
        String SIM_COUNTRY_ISO = "获取sim卡国家代码";
        String SIM_NETWORK_COUNTRY_ISO = "获取sim卡网络运营商";
        String SIM_NETWORK_OPERATOR = "获取sim卡网络运营商名称";
        String SIM_NETWORK_OPERATOR_NAME = "获取sim卡网络运营商国家码";
        String SIM_STATE = "获取sim卡状态";
        String SIM_INFO = "获取sim卡信息";

        String CHECK_PERMISSION = "检查权限";
        String REQUEST_PERMISSION = "申请权限-高敏";

        String SERIAL = "获取设备序列号-高敏";

        String SCAN_BLUETOOTH = "扫描蓝牙";
        String BLUETOOTH_INFO = "获取蓝牙设备信息";

        String INET_ADDRESS = "获取ip地址";

        String CONTENT_PROVIDER_INTERCEPT = "contentProvider拦截";

        String GET_EXTERNAL_STORAGE_DIRECTORY = "获取外部存储目录";

        String MAC = "获取mac地址-高敏";

        String SYSTEM_PROPERTIES = "获取系统属性";
        String SYSTEM_PROPERTIES_INTERCEPT = "系统属性_拦截";

        String GET_SENSOR = "获取传感器-高敏";

        String LISTEN_SENSOR = "监听传感器-高敏";

        String ANDROID_ID = "获取AndroidId-高敏";

        String APP_INFO = "获取指定应用信息-高敏";


        String BROWSER_BOOKMARKS = "操作浏览器书签-高敏";

        String CELL_INFO = "获取移动网参数";

        String MEDIA_IMAGE = "操作多媒体-图片-高敏";
        String MEDIA_VIDEO = "操作多媒体-视频-高敏";
        String MEDIA_AUDIO = "操作多媒体-音频-高敏";

        String OAID = "获取OAID-高敏";
        String VAID = "获取VAID-高敏";
        String AAID = "获取AAID-高敏";

        String NET_TYPE = "获取网络类型";
        String NET_TYPE_NAME = "获取网络类型名称";
        String NET_CONNECTED = "获取网络是否连接";
        String PHONE_CALL_STATE = "获取手机呼叫状态";
        String PHONE_DATA = "获取手机数据状态";
        String PHONE_SERVICE_STATE = "获取手机服务状态";
        String SHELL = "执行shell命令";
        String SHELL_INTERCEPT = "执行shell命令_拦截";
        String TAKE_PHOTO = "拍照-高敏";
        String CAMERA_LIST = "获取摄像头列表";
        String FILE_INTERCEPT = "文件构建_拦截";

        String BOOT_ID = "系统启动标识boot_id-高敏(小米)";

        String RECEIVE_BROADCAST = "监听系统广播";
        String REGISTER_BROADCAST = "动态注册系统广播监听";
    }

}
