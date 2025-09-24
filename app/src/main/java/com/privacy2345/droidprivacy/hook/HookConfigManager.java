package com.privacy2345.droidprivacy.hook;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.admin.DevicePolicyManager;
import android.app.usage.UsageStatsManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContextWrapper;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.privacy2345.droidprivacy.constant.Behavior;
import com.privacy2345.droidprivacy.model.CheckItem;
import com.privacy2345.droidprivacy.model.CheckItemChild;

import java.io.File;
import java.io.FilterOutputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;

/**
 * Hook配置管理类
 *
 * @author : zhongjy@2345.com
 */
public class HookConfigManager {

    private static final String TAG = "HookConfigManager";

    private static final class InstanceHolder {
        static final HookConfigManager instance = new HookConfigManager();
    }

    public static HookConfigManager getInstance() {
        return InstanceHolder.instance;
    }

    private List<HookMethod> hookMethodList;

    private boolean isInitConfig;

    private Set<String> checkItemSet;

    public List<HookMethod> getConfig(Set<String> checkItemSet) {
        if (hookMethodList == null) {
            hookMethodList = new ArrayList<>();
        }
        if (!hookMethodList.isEmpty()) {
            return hookMethodList;
        }
        if (checkItemSet == null || checkItemSet.isEmpty()) {
            return hookMethodList;
        }
        addPermission();
        addLocate();
        addCamera();
        addAppList();
        addRunningApp();
        addWifi();
        addSerial();
        addImei();
        addImsi();
        addSim();
        addAccount();
        addBluetoothLe();
        addInetAddress();
        addContentResolver();
        addStorage();
        addMac();
        addSystemProperties();
        addSensor();
        addAndroidId();
        addAppInfo();
        addCellInfo();
        addNetType();
        addPhoneCallState();
        addPhoneData();
        addPhoneService();
        addBroadcast();
        addShell();
        addMsaId();
        addFile();

        // 部分规则需要拦截到方法调用并判断参数之后才能出判断具体规则
        checkItemSet.add(Behavior.Rule.CONTENT_PROVIDER_INTERCEPT);
        checkItemSet.add(Behavior.Rule.SYSTEM_PROPERTIES_INTERCEPT);
        checkItemSet.add(Behavior.Rule.SHELL_INTERCEPT);
        checkItemSet.add(Behavior.Rule.FILE_INTERCEPT);

        // 过滤重复添加的规则，过滤用户未勾选的规则
        List<HookMethod> methodList = new ArrayList<>();
        for (HookMethod method : hookMethodList) {
            if (checkItemSet.contains(method.getRule())) {
                methodList.add(method);
            }
        }
        hookMethodList = methodList;
        return hookMethodList;
    }

    private void addAccount() {
        hookMethodList.add(new HookMethod.Builder().setCls(AccountManager.class).setMethodName("getAccounts")
                .setCategory(Behavior.Category.ACCOUNT).setRule(Behavior.Rule.GET_ACCOUNT).build());
        hookMethodList.add(new HookMethod.Builder().setCls(AccountManager.class).setMethodName("getAccountsByType")
                .setCategory(Behavior.Category.ACCOUNT).setRule(Behavior.Rule.GET_ACCOUNT).build());
        hookMethodList.add(new HookMethod.Builder().setCls(AccountManager.class).setMethodName("getAccountsAsUser")
                .setCategory(Behavior.Category.ACCOUNT).setRule(Behavior.Rule.GET_ACCOUNT).build());
        hookMethodList.add(new HookMethod.Builder().setCls(AccountManager.class).setMethodName("getAccountsByTypeForPackage")
                .setCategory(Behavior.Category.ACCOUNT).setRule(Behavior.Rule.GET_ACCOUNT).build());
        hookMethodList.add(new HookMethod.Builder().setCls(AccountManager.class).setMethodName("getAccountsByTypeAndFeatures")
                .setCategory(Behavior.Category.ACCOUNT).setRule(Behavior.Rule.GET_ACCOUNT).build());
        hookMethodList.add(new HookMethod.Builder().setCls(AccountManager.class).setMethodName("getAccountsAndVisibilityForPackage")
                .setCategory(Behavior.Category.ACCOUNT).setRule(Behavior.Rule.GET_ACCOUNT).build());
        hookMethodList.add(new HookMethod.Builder().setCls(AccountManager.class).setMethodName("getPackagesAndVisibilityForAccount")
                .setCategory(Behavior.Category.ACCOUNT).setRule(Behavior.Rule.GET_ACCOUNT).build());
        hookMethodList.add(new HookMethod.Builder().setCls(AccountManager.class).setMethodName("getAccountVisibility")
                .setCategory(Behavior.Category.ACCOUNT).setRule(Behavior.Rule.GET_ACCOUNT).build());
        hookMethodList.add(new HookMethod.Builder().setCls(AccountManager.class).setMethodName("addAccountExplicitly")
                .setCategory(Behavior.Category.ACCOUNT).setRule(Behavior.Rule.ADD_ACCOUNT).build());
        hookMethodList.add(new HookMethod.Builder().setCls(AccountManager.class).setMethodName("removeAccountExplicitly")
                .setCategory(Behavior.Category.ACCOUNT).setRule(Behavior.Rule.REMOVE_ACCOUNT).build());
    }

    private void addAppList() {
        hookMethodList.add(new HookMethod.Builder().setClsName("android.app.ApplicationPackageManager").setMethodName("getInstalledApplications")
                .setCategory(Behavior.Category.APP_LIST).setRule(Behavior.Rule.GET_APP_LIST).build());
        hookMethodList.add(new HookMethod.Builder().setClsName("android.app.ApplicationPackageManager").setMethodName("getInstalledPackages")
                .setCategory(Behavior.Category.APP_LIST).setRule(Behavior.Rule.GET_APP_LIST).build());
        hookMethodList.add(new HookMethod.Builder().setClsName("android.app.ApplicationPackageManager").setMethodName("getInstalledPackagesAsUser")
                .setCategory(Behavior.Category.APP_LIST).setRule(Behavior.Rule.GET_APP_LIST).build());
        hookMethodList.add(new HookMethod.Builder().setClsName("android.app.ApplicationPackageManager").setMethodName("getPackagesForUid")
                .setCategory(Behavior.Category.APP_LIST).setRule(Behavior.Rule.GET_APP_LIST).build());
        hookMethodList.add(new HookMethod.Builder().setClsName("android.app.ApplicationPackageManager").setMethodName("queryIntentActivities")
                .setCategory(Behavior.Category.APP_LIST).setRule(Behavior.Rule.GET_APP_LIST).build());
        hookMethodList.add(new HookMethod.Builder().setCls(UsageStatsManager.class).setMethodName("queryUsageStats")
                .setCategory(Behavior.Category.APP_LIST).setRule(Behavior.Rule.GET_APP_LIST).build());
        hookMethodList.add(new HookMethod.Builder().setClsName("android.app.ApplicationPackageManager").setMethodName("getInstalledModules")
                .setCategory(Behavior.Category.APP_LIST).setRule(Behavior.Rule.GET_APP_LIST).build());
    }

    private void addRunningApp() {
        hookMethodList.add(new HookMethod.Builder().setCls(ActivityManager.class).setMethodName("getRunningAppProcesses")
                .setCategory(Behavior.Category.RUNNING_APP).setRule(Behavior.Rule.GET_RUNNING_APP).build());
        hookMethodList.add(new HookMethod.Builder().setCls(Application.class).setMethodName("getProcessName")
                .setCategory(Behavior.Category.RUNNING_APP).setRule(Behavior.Rule.GET_RUNNING_APP).build());
        hookMethodList.add(new HookMethod.Builder().setClsName("android.app.ActivityThread").setMethodName("getProcessName")
                .setCategory(Behavior.Category.RUNNING_APP).setRule(Behavior.Rule.GET_RUNNING_APP).build());
        hookMethodList.add(new HookMethod.Builder().setClsName("android.app.ActivityThread").setMethodName("currentProcessName")
                .setCategory(Behavior.Category.RUNNING_APP).setRule(Behavior.Rule.GET_RUNNING_APP).build());
    }

    private void addWifi() {
        hookMethodList.add(new HookMethod.Builder().setCls(WifiInfo.class).setMethodName("getMacAddress")
                .setCategory(Behavior.Category.WIFI).setRule(Behavior.Rule.WIFI_MAC).build());
        hookMethodList.add(new HookMethod.Builder().setCls(WifiInfo.class).setMethodName("getIpAddress")
                .setCategory(Behavior.Category.WIFI).setRule(Behavior.Rule.WIFI_IP).build());
        hookMethodList.add(new HookMethod.Builder().setCls(WifiInfo.class).setMethodName("getSSID")
                .setCategory(Behavior.Category.WIFI).setRule(Behavior.Rule.WIFI_SSID).build());
        hookMethodList.add(new HookMethod.Builder().setCls(WifiInfo.class).setMethodName("getBSSID")
                .setCategory(Behavior.Category.WIFI).setRule(Behavior.Rule.WIFI_BSSID).build());
        hookMethodList.add(new HookMethod.Builder().setCls(WifiInfo.class).setMethodName("getRssi")
                .setCategory(Behavior.Category.WIFI).setRule(Behavior.Rule.WIFI_RSSI).build());
        hookMethodList.add(new HookMethod.Builder().setCls(WifiManager.class).setMethodName("startScan")
                .setCategory(Behavior.Category.WIFI).setRule(Behavior.Rule.SCAN_WIFI).build());
        hookMethodList.add(new HookMethod.Builder().setCls(WifiManager.class).setMethodName("getScanResults")
                .setCategory(Behavior.Category.WIFI).setRule(Behavior.Rule.GET_SCAN_WIFI_RESULTS).build());
        hookMethodList.add(new HookMethod.Builder().setCls(WifiManager.class).setMethodName("getConnectionInfo")
                .setCategory(Behavior.Category.WIFI).setRule(Behavior.Rule.WIFI_INFO).build());
        hookMethodList.add(new HookMethod.Builder().setCls(WifiManager.class).setMethodName("getConfiguredNetworks")
                .setCategory(Behavior.Category.WIFI).setRule(Behavior.Rule.WIFI_INFO).build());
        hookMethodList.add(new HookMethod.Builder().setCls(WifiManager.class).setMethodName("getWifiState")
                .setCategory(Behavior.Category.WIFI).setRule(Behavior.Rule.WIFI_INFO).build());
    }

    private void addImei() {
        hookMethodList.add(new HookMethod.Builder().setCls(TelephonyManager.class).setMethodName("getImei")
                .setCategory(Behavior.Category.IMEI).setRule(Behavior.Rule.IMEI).build());
        hookMethodList.add(new HookMethod.Builder().setCls(TelephonyManager.class).setMethodName("getMeid")
                .setCategory(Behavior.Category.IMEI).setRule(Behavior.Rule.IMEI).build());
        hookMethodList.add(new HookMethod.Builder().setCls(TelephonyManager.class).setMethodName("getDeviceId")
                .setCategory(Behavior.Category.IMEI).setRule(Behavior.Rule.IMEI).build());
    }

    private void addImsi() {
        hookMethodList.add(new HookMethod.Builder().setCls(TelephonyManager.class).setMethodName("getSubscriberId")
                .setCategory(Behavior.Category.IMSI).setRule(Behavior.Rule.IMSI).build());
    }

    private void addSim() {
        // SIM 卡的序列号（即 ICCID）
        hookMethodList.add(new HookMethod.Builder().setCls(TelephonyManager.class).setMethodName("getSimSerialNumber")
                .setCategory(Behavior.Category.SIM).setRule(Behavior.Rule.SIM_SERIAL_NUMBER).build());

        // 获取SIM卡运营商编号 前3位是MCC(Mobile Country Code，移动国家码)，后几位是MNC(Mobile Network Code，移动网络码)
        // getSimOperator会调用getSimOperatorNumeric
        hookMethodList.add(new HookMethod.Builder().setCls(TelephonyManager.class).setMethodName("getSimOperatorNumeric")
                .setCategory(Behavior.Category.SIM).setRule(Behavior.Rule.SIM_OPERATOR).build());

        // 获取特定电话的SIM卡运营商名称，getSimOperatorName会调用getSimOperatorNameForPhone
        hookMethodList.add(new HookMethod.Builder().setCls(TelephonyManager.class).setMethodName("getSimOperatorNameForPhone")
                .setCategory(Behavior.Category.SIM).setRule(Behavior.Rule.SIM_OPERATOR_NAME).build());

        // 获取特定电话的sim卡国家代码 getSimCountryIso会调用getSimCountryIsoForPhone
        hookMethodList.add(new HookMethod.Builder().setCls(TelephonyManager.class).setMethodName("getSimCountryIsoForPhone")
                .setCategory(Behavior.Category.SIM).setRule(Behavior.Rule.SIM_COUNTRY_ISO).build());

        // 获取特定电话的网络运营商，getNetworkOperator会调用getNetworkOperatorForPhone
        hookMethodList.add(new HookMethod.Builder().setCls(TelephonyManager.class).setMethodName("getNetworkOperatorForPhone")
                .setCategory(Behavior.Category.SIM).setRule(Behavior.Rule.SIM_NETWORK_OPERATOR).build());

        // 获取网络运营商名称
        hookMethodList.add(new HookMethod.Builder().setCls(TelephonyManager.class).setMethodName("getNetworkOperatorName")
                .setCategory(Behavior.Category.SIM).setRule(Behavior.Rule.SIM_NETWORK_OPERATOR_NAME).build());

        // 获取网络运营商国家码
        hookMethodList.add(new HookMethod.Builder().setCls(TelephonyManager.class).setMethodName("getNetworkCountryIso")
                .setCategory(Behavior.Category.SIM).setRule(Behavior.Rule.SIM_NETWORK_COUNTRY_ISO).build());

        hookMethodList.add(new HookMethod.Builder().setCls(TelephonyManager.class).setMethodName("getSimState")
                .setCategory(Behavior.Category.SIM).setRule(Behavior.Rule.SIM_STATE).build());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            hookMethodList.add(new HookMethod.Builder().setCls(TelephonyManager.class).setMethodName("getUiccCardsInfo")
                    .setCategory(Behavior.Category.SIM).setRule(Behavior.Rule.SIM_SERIAL_NUMBER).build());
        }
    }

    private void addLocate() {
        hookMethodList.add(new HookMethod.Builder().setCls(TelephonyManager.class).setMethodName("getCellLocation")
                .setCategory(Behavior.Category.LOCATE).setRule(Behavior.Rule.CELL_LOCATION).build());
        hookMethodList.add(new HookMethod.Builder().setCls(CdmaCellLocation.class).setMethodName("getBaseStationId")
                .setCategory(Behavior.Category.LOCATE).setRule(Behavior.Rule.CELL_LOCATION).build());
        hookMethodList.add(new HookMethod.Builder().setCls(CdmaCellLocation.class).setMethodName("getNetworkId")
                .setCategory(Behavior.Category.LOCATE).setRule(Behavior.Rule.CELL_LOCATION).build());
        hookMethodList.add(new HookMethod.Builder().setCls(CdmaCellLocation.class).setMethodName("getSystemId")
                .setCategory(Behavior.Category.LOCATE).setRule(Behavior.Rule.CELL_LOCATION).build());
        hookMethodList.add(new HookMethod.Builder().setCls(GsmCellLocation.class).setMethodName("getCid")
                .setCategory(Behavior.Category.LOCATE).setRule(Behavior.Rule.CELL_LOCATION).build());
        hookMethodList.add(new HookMethod.Builder().setCls(GsmCellLocation.class).setMethodName("getLac")
                .setCategory(Behavior.Category.LOCATE).setRule(Behavior.Rule.CELL_LOCATION).build());
        hookMethodList.add(new HookMethod.Builder().setCls(GsmCellLocation.class).setMethodName("getPsc")
                .setCategory(Behavior.Category.LOCATE).setRule(Behavior.Rule.CELL_LOCATION).build());

        //  requestSingleUpdate最后都调用到了requestLocationUpdates
        hookMethodList.add(new HookMethod.Builder().setCls(LocationManager.class).setMethodName("requestLocationUpdates")
                .setCategory(Behavior.Category.LOCATE).setRule(Behavior.Rule.REQUEST_LOCATION).build());
        hookMethodList.add(new HookMethod.Builder().setCls(LocationManager.class).setMethodName("getLastKnownLocation")
                .setCategory(Behavior.Category.LOCATE).setRule(Behavior.Rule.GET_LAST_KNOW_LOCATION).build());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            hookMethodList.add(new HookMethod.Builder().setCls(LocationManager.class).setMethodName("getCurrentLocation")
                    .setCategory(Behavior.Category.LOCATE).setRule(Behavior.Rule.GET_CURRENT_LOCATION).build());
        }

        hookMethodList.add(new HookMethod.Builder().setCls(Location.class).setMethodName("getLatitude")
                .setCategory(Behavior.Category.LOCATE).setRule(Behavior.Rule.LOCATION_INFO).build());
        hookMethodList.add(new HookMethod.Builder().setCls(Location.class).setMethodName("getLongitude")
                .setCategory(Behavior.Category.LOCATE).setRule(Behavior.Rule.LOCATION_INFO).build());
        hookMethodList.add(new HookMethod.Builder().setCls(Location.class).setMethodName("getAccuracy")
                .setCategory(Behavior.Category.LOCATE).setRule(Behavior.Rule.LOCATION_INFO).build());
        hookMethodList.add(new HookMethod.Builder().setCls(Location.class).setMethodName("getAltitude")
                .setCategory(Behavior.Category.LOCATE).setRule(Behavior.Rule.LOCATION_INFO).build());
        hookMethodList.add(new HookMethod.Builder().setCls(Location.class).setMethodName("getBearing")
                .setCategory(Behavior.Category.LOCATE).setRule(Behavior.Rule.LOCATION_INFO).build());
        hookMethodList.add(new HookMethod.Builder().setCls(Location.class).setMethodName("getBearingAccuracyDegrees")
                .setCategory(Behavior.Category.LOCATE).setRule(Behavior.Rule.LOCATION_INFO).build());
        hookMethodList.add(new HookMethod.Builder().setCls(Location.class).setMethodName("getElapsedRealtimeNanos")
                .setCategory(Behavior.Category.LOCATE).setRule(Behavior.Rule.LOCATION_INFO).build());
        hookMethodList.add(new HookMethod.Builder().setCls(Location.class).setMethodName("getProvider")
                .setCategory(Behavior.Category.LOCATE).setRule(Behavior.Rule.LOCATION_INFO).build());
        hookMethodList.add(new HookMethod.Builder().setCls(Location.class).setMethodName("getSpeed")
                .setCategory(Behavior.Category.LOCATE).setRule(Behavior.Rule.LOCATION_INFO).build());
        hookMethodList.add(new HookMethod.Builder().setCls(Location.class).setMethodName("getSpeedAccuracyMetersPerSecond")
                .setCategory(Behavior.Category.LOCATE).setRule(Behavior.Rule.LOCATION_INFO).build());
        hookMethodList.add(new HookMethod.Builder().setCls(Location.class).setMethodName("getTime")
                .setCategory(Behavior.Category.LOCATE).setRule(Behavior.Rule.LOCATION_INFO).build());
        hookMethodList.add(new HookMethod.Builder().setCls(Location.class).setMethodName("getVerticalAccuracyMeters")
                .setCategory(Behavior.Category.LOCATE).setRule(Behavior.Rule.LOCATION_INFO).build());
        hookMethodList.add(new HookMethod.Builder().setCls(TelephonyManager.class).setMethodName("listen")
                .setCategory(Behavior.Category.LOCATE).setRule(Behavior.Rule.LOCATION_INFO)
                .setListener((hookMethod, param, lpp) -> {
                    if (isTelephonyManagerListenerTypeMatch(param, PhoneStateListener.LISTEN_CELL_LOCATION)) {
                        hookMethod.setRule(Behavior.Rule.LOCATION_INFO);
                    } else {
                        hookMethod.setRule(null);
                    }
                }).build());
    }

    private void addCamera() {
        hookMethodList.add(new HookMethod.Builder().setCls(CameraManager.class).setMethodName("getCameraIdList")
                .setCategory(Behavior.Category.CAMERA).setRule(Behavior.Rule.CAMERA_LIST).build());
        hookMethodList.add(new HookMethod.Builder().setCls(CameraManager.class).setMethodName("getCameraIdListNoLazy")
                .setCategory(Behavior.Category.CAMERA).setRule(Behavior.Rule.CAMERA_LIST).build());
        hookMethodList.add(new HookMethod.Builder().setCls(Camera.class).setMethodName("open")
                .setCategory(Behavior.Category.CAMERA).setRule(Behavior.Rule.TAKE_PHOTO).build());
        hookMethodList.add(new HookMethod.Builder().setCls(CameraManager.class).setMethodName("openCamera")
                .setCategory(Behavior.Category.CAMERA).setRule(Behavior.Rule.TAKE_PHOTO).build());
        hookMethodList.add(new HookMethod.Builder().setClsName("androidx.camera.core.ImageCapture").setMethodName("takePicture")
                .setCategory(Behavior.Category.CAMERA).setRule(Behavior.Rule.TAKE_PHOTO).build());
    }

    private void addPermission() {
        hookMethodList.add(new HookMethod.Builder().setCls(ContextWrapper.class).setMethodName("checkSelfPermission")
                .setCategory(Behavior.Category.PERMISSION).setRule(Behavior.Rule.CHECK_PERMISSION).build());
        hookMethodList.add(new HookMethod.Builder().setCls(ContextCompat.class).setMethodName("checkSelfPermission")
                .setCategory(Behavior.Category.PERMISSION).setRule(Behavior.Rule.CHECK_PERMISSION).build());
        hookMethodList.add(new HookMethod.Builder().setCls(Activity.class).setMethodName("requestPermissions")
                .setCategory(Behavior.Category.PERMISSION).setRule(Behavior.Rule.REQUEST_PERMISSION).build());
        hookMethodList.add(new HookMethod.Builder().setCls(ActivityCompat.class).setMethodName("requestPermissions")
                .setCategory(Behavior.Category.PERMISSION).setRule(Behavior.Rule.REQUEST_PERMISSION).build());
        hookMethodList.add(new HookMethod.Builder().setClsName("android.support.v4.app.ActivityCompat").setMethodName("requestPermissions")
                .setCategory(Behavior.Category.PERMISSION).setRule(Behavior.Rule.REQUEST_PERMISSION).build());
    }

    private void addSerial() {
        hookMethodList.add(new HookMethod.Builder().setCls(Build.class).setMethodName("getSerial")
                .setCategory(Behavior.Category.SERIAL).setRule(Behavior.Rule.SERIAL).build());
    }

    private void addBluetoothLe() {
        hookMethodList.add(new HookMethod.Builder().setCls(BluetoothLeScanner.class).setMethodName("startScan")
                .setCategory(Behavior.Category.BLUETOOTH).setRule(Behavior.Rule.SCAN_BLUETOOTH).build());
        hookMethodList.add(new HookMethod.Builder().setCls(BluetoothAdapter.class).setMethodName("startLeScan")
                .setCategory(Behavior.Category.BLUETOOTH).setRule(Behavior.Rule.SCAN_BLUETOOTH).build());
        hookMethodList.add(new HookMethod.Builder().setCls(BluetoothAdapter.class).setMethodName("startDiscovery")
                .setCategory(Behavior.Category.BLUETOOTH).setRule(Behavior.Rule.SCAN_BLUETOOTH).build());
        hookMethodList.add(new HookMethod.Builder().setCls(BluetoothDevice.class).setMethodName("getName")
                .setCategory(Behavior.Category.BLUETOOTH).setRule(Behavior.Rule.BLUETOOTH_INFO).build());
        hookMethodList.add(new HookMethod.Builder().setCls(BluetoothDevice.class).setMethodName("getAddress")
                .setCategory(Behavior.Category.BLUETOOTH).setRule(Behavior.Rule.BLUETOOTH_INFO).build());
        hookMethodList.add(new HookMethod.Builder().setCls(BluetoothDevice.class).setMethodName("getAlias")
                .setCategory(Behavior.Category.BLUETOOTH).setRule(Behavior.Rule.BLUETOOTH_INFO).build());
        hookMethodList.add(new HookMethod.Builder().setCls(BluetoothDevice.class).setMethodName("getType")
                .setCategory(Behavior.Category.BLUETOOTH).setRule(Behavior.Rule.BLUETOOTH_INFO).build());
        hookMethodList.add(new HookMethod.Builder().setCls(BluetoothDevice.class).setMethodName("getUuids")
                .setCategory(Behavior.Category.BLUETOOTH).setRule(Behavior.Rule.BLUETOOTH_INFO).build());
    }

    private void addInetAddress() {
        hookMethodList.add(new HookMethod.Builder().setCls(NetworkInterface.class).setMethodName("getInetAddresses")
                .setCategory(Behavior.Category.INET_ADDRESS).setRule(Behavior.Rule.INET_ADDRESS).build());
        hookMethodList.add(new HookMethod.Builder().setCls(InetAddress.class).setMethodName("getHostName")
                .setCategory(Behavior.Category.INET_ADDRESS).setRule(Behavior.Rule.INET_ADDRESS).build());
        hookMethodList.add(new HookMethod.Builder().setCls(Inet4Address.class).setMethodName("getHostAddress")
                .setCategory(Behavior.Category.INET_ADDRESS).setRule(Behavior.Rule.INET_ADDRESS).build());
        hookMethodList.add(new HookMethod.Builder().setCls(Inet4Address.class).setMethodName("getAddress")
                .setCategory(Behavior.Category.INET_ADDRESS).setRule(Behavior.Rule.INET_ADDRESS).build());
        hookMethodList.add(new HookMethod.Builder().setCls(Inet6Address.class).setMethodName("getHostAddress")
                .setCategory(Behavior.Category.INET_ADDRESS).setRule(Behavior.Rule.INET_ADDRESS).build());
        hookMethodList.add(new HookMethod.Builder().setCls(Inet6Address.class).setMethodName("getAddress")
                .setCategory(Behavior.Category.INET_ADDRESS).setRule(Behavior.Rule.INET_ADDRESS).build());
        hookMethodList.add(new HookMethod.Builder().setCls(InetSocketAddress.class).setMethodName("getHostString")
                .setCategory(Behavior.Category.INET_ADDRESS).setRule(Behavior.Rule.INET_ADDRESS).build());
        hookMethodList.add(new HookMethod.Builder().setCls(InetSocketAddress.class).setMethodName("getAddress")
                .setCategory(Behavior.Category.INET_ADDRESS).setRule(Behavior.Rule.INET_ADDRESS).build());
        hookMethodList.add(new HookMethod.Builder().setCls(InetSocketAddress.class).setMethodName("getHostName")
                .setCategory(Behavior.Category.INET_ADDRESS).setRule(Behavior.Rule.INET_ADDRESS).build());
        hookMethodList.add(new HookMethod.Builder().setCls(InetSocketAddress.class).setMethodName("getPort")
                .setCategory(Behavior.Category.INET_ADDRESS).setRule(Behavior.Rule.INET_ADDRESS).build());
    }

    private void addContentResolver() {
        HookMethod.HookedMethodListener listener = (hookMethod, param, lpp) -> {
            if (param != null && param.args.length > 0 && param.args[0] instanceof Uri) {
                Uri uri = (Uri) param.args[0];
                String uriString = uri.toString();
                if (TextUtils.isEmpty(uriString)) {
                    hookMethod.setCategory(null);
                    hookMethod.setRule(null);
                    return;
                }
                Log.d(TAG, "ContentResolver uriString:" + uriString);
                if (uriString.contains("content://browser/bookmarks") || uriString.contains("content://com.android.chrome.browser/bookmarks")) {// 浏览器书签
                    hookMethod.setCategory(Behavior.Category.BROWSER_BOOKMARKS);
                    hookMethod.setRule(Behavior.Rule.BROWSER_BOOKMARKS);
                } else if (uriString.contains(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString())) {// 多媒体-图片
                    hookMethod.setCategory(Behavior.Category.MEDIA);
                    hookMethod.setRule(Behavior.Rule.MEDIA_IMAGE);
                } else if (uriString.contains(MediaStore.Video.Media.EXTERNAL_CONTENT_URI.toString())) {// 多媒体-视频
                    hookMethod.setCategory(Behavior.Category.MEDIA);
                    hookMethod.setRule(Behavior.Rule.MEDIA_VIDEO);
                } else if (uriString.contains(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString())) {// 多媒体-音频
                    hookMethod.setCategory(Behavior.Category.MEDIA);
                    hookMethod.setRule(Behavior.Rule.MEDIA_AUDIO);
                } else if (uriString.contains("content://com.android.settings.applications/applications")) {// 应用列表
                    hookMethod.setCategory(Behavior.Category.APP_LIST);
                    hookMethod.setRule(Behavior.Rule.GET_APP_LIST);
                } else {
                    hookMethod.setCategory(null);
                    hookMethod.setRule(null);
                }
            } else {
                hookMethod.setCategory(null);
                hookMethod.setRule(null);
            }
        };
        hookMethodList.add(new HookMethod.Builder().setCls(ContentResolver.class).setMethodName("query")
                .setCategory(Behavior.Category.CONTENT_PROVIDER).setRule(Behavior.Rule.CONTENT_PROVIDER_INTERCEPT)
                .setListener(listener).build());
        hookMethodList.add(new HookMethod.Builder().setCls(ContentResolver.class).setMethodName("update")
                .setCategory(Behavior.Category.CONTENT_PROVIDER).setRule(Behavior.Rule.CONTENT_PROVIDER_INTERCEPT)
                .setListener(listener).build());
        hookMethodList.add(new HookMethod.Builder().setCls(ContentResolver.class).setMethodName("insert")
                .setCategory(Behavior.Category.CONTENT_PROVIDER).setRule(Behavior.Rule.CONTENT_PROVIDER_INTERCEPT)
                .setListener(listener).build());
        hookMethodList.add(new HookMethod.Builder().setCls(ContentResolver.class).setMethodName("delete")
                .setCategory(Behavior.Category.CONTENT_PROVIDER).setRule(Behavior.Rule.CONTENT_PROVIDER_INTERCEPT)
                .setListener(listener).build());
        hookMethodList.add(new HookMethod.Builder().setCls(ContentResolver.class).setMethodName("registerContentObserver")
                .setCategory(Behavior.Category.CONTENT_PROVIDER).setRule(Behavior.Rule.CONTENT_PROVIDER_INTERCEPT)
                .setListener(listener).build());
    }

    private void addStorage() {
        hookMethodList.add(new HookMethod.Builder().setCls(Environment.class).setMethodName("getExternalStorageDirectory")
                .setCategory(Behavior.Category.STORAGE).setRule(Behavior.Rule.GET_EXTERNAL_STORAGE_DIRECTORY).build());
    }

    private void addMac() {
        hookMethodList.add(new HookMethod.Builder().setCls(NetworkInterface.class).setMethodName("getHardwareAddress")
                .setCategory(Behavior.Category.MAC).setRule(Behavior.Rule.MAC).build());
        hookMethodList.add(new HookMethod.Builder().setCls(WifiInfo.class).setMethodName("getMacAddress")
                .setCategory(Behavior.Category.MAC).setRule(Behavior.Rule.MAC).build());
        hookMethodList.add(new HookMethod.Builder().setCls(WifiManager.class).setMethodName("getFactoryMacAddresses")
                .setCategory(Behavior.Category.MAC).setRule(Behavior.Rule.MAC).build());
        hookMethodList.add(new HookMethod.Builder().setCls(DevicePolicyManager.class).setMethodName("getWifiMacAddress")
                .setCategory(Behavior.Category.MAC).setRule(Behavior.Rule.MAC).build());
    }

    private void addSystemProperties() {
        HookMethod.HookedMethodListener listener = (hookMethod, param, lpp) -> {
            if (param != null && param.args.length > 0 && param.args[0] instanceof String) {
                String arg = param.args[0].toString();
                if (TextUtils.isEmpty(arg)) {
                    hookMethod.setCategory(null);
                    hookMethod.setRule(null);
                    return;
                }
                Log.d(TAG, "SystemProperties properties:" + arg);
                if (arg.contains("ril.gsm.imei") || arg.contains("ril.cdma.meid")) {
                    hookMethod.setCategory(Behavior.Category.IMEI);
                    hookMethod.setRule(Behavior.Rule.IMEI);
                } else if (arg.contains("ro.serialno") || arg.contains("persist.sys.product.serialno")) {
                    hookMethod.setCategory(Behavior.Category.SERIAL);
                    hookMethod.setRule(Behavior.Rule.SERIAL);
                } else if (arg.contains("gsm.sim.state")) {
                    hookMethod.setCategory(Behavior.Category.SIM);
                    hookMethod.setRule(Behavior.Rule.SIM_STATE);
                } else if (arg.contains("gsm.sim.operator.numeric") || arg.contains("gsm.operator.numeric")) {
                    hookMethod.setCategory(Behavior.Category.SIM);
                    hookMethod.setRule(Behavior.Rule.SIM_OPERATOR);
                } else if (arg.contains("gsm.operator.alpha") || arg.contains("gsm.sim.operator.alpha")) {
                    hookMethod.setCategory(Behavior.Category.SIM);
                    hookMethod.setRule(Behavior.Rule.SIM_OPERATOR_NAME);
                } else if (arg.contains("gsm.sim.operator.iso-country") || arg.contains("gsm.operator.iso-country")) {
                    hookMethod.setCategory(Behavior.Category.SIM);
                    hookMethod.setRule(Behavior.Rule.SIM_COUNTRY_ISO);
                } else if (arg.contains("gsm.network.type")) {
                    hookMethod.setCategory(Behavior.Category.NET_INFO);
                    hookMethod.setRule(Behavior.Rule.NET_TYPE);
                } else {
                    hookMethod.setCategory(null);
                    hookMethod.setRule(null);
                }
            } else {
                hookMethod.setCategory(null);
                hookMethod.setRule(null);
            }
        };
        hookMethodList.add(new HookMethod.Builder().setClsName("android.os.SystemProperties").setMethodName("get")
                .setCategory(Behavior.Category.SYSTEM_PROPERTIES).setRule(Behavior.Rule.SYSTEM_PROPERTIES_INTERCEPT)
                .setListener(listener).build());
        hookMethodList.add(new HookMethod.Builder().setClsName("android.os.SystemProperties").setMethodName("getInt")
                .setCategory(Behavior.Category.SYSTEM_PROPERTIES).setRule(Behavior.Rule.SYSTEM_PROPERTIES_INTERCEPT)
                .setListener(listener).build());
        hookMethodList.add(new HookMethod.Builder().setClsName("android.os.SystemProperties").setMethodName("getLong")
                .setCategory(Behavior.Category.SYSTEM_PROPERTIES).setRule(Behavior.Rule.SYSTEM_PROPERTIES_INTERCEPT)
                .setListener(listener).build());
        hookMethodList.add(new HookMethod.Builder().setClsName("android.os.SystemProperties").setMethodName("getBoolean")
                .setCategory(Behavior.Category.SYSTEM_PROPERTIES).setRule(Behavior.Rule.SYSTEM_PROPERTIES_INTERCEPT)
                .setListener(listener).build());

        hookMethodList.add(new HookMethod.Builder().setClsName("android.os.SystemProperties").setMethodName("get")
                .setCategory(Behavior.Category.SYSTEM_PROPERTIES).setRule(Behavior.Rule.SYSTEM_PROPERTIES).build());
        hookMethodList.add(new HookMethod.Builder().setClsName("android.os.SystemProperties").setMethodName("getInt")
                .setCategory(Behavior.Category.SYSTEM_PROPERTIES).setRule(Behavior.Rule.SYSTEM_PROPERTIES).build());
        hookMethodList.add(new HookMethod.Builder().setClsName("android.os.SystemProperties").setMethodName("getLong")
                .setCategory(Behavior.Category.SYSTEM_PROPERTIES).setRule(Behavior.Rule.SYSTEM_PROPERTIES).build());
        hookMethodList.add(new HookMethod.Builder().setClsName("android.os.SystemProperties").setMethodName("getBoolean")
                .setCategory(Behavior.Category.SYSTEM_PROPERTIES).setRule(Behavior.Rule.SYSTEM_PROPERTIES).build());
    }

    private void addSensor() {
        hookMethodList.add(new HookMethod.Builder().setCls(SensorManager.class).setMethodName("getSensorList")
                .setCategory(Behavior.Category.SENSOR).setRule(Behavior.Rule.GET_SENSOR).build());
        hookMethodList.add(new HookMethod.Builder().setCls(SensorManager.class).setMethodName("registerListener")
                .setCategory(Behavior.Category.SENSOR).setRule(Behavior.Rule.LISTEN_SENSOR).build());
    }

    private void addAndroidId() {
        HookMethod.HookedMethodListener listener = (hookMethod, param, lpp) -> {
            if (param != null && param.args.length >= 2 && param.args[1] instanceof String) {
                if (TextUtils.equals(Settings.Secure.ANDROID_ID, param.args[1].toString())) {
                    hookMethod.setCategory(Behavior.Category.ANDROID_ID);
                    hookMethod.setRule(Behavior.Rule.ANDROID_ID);
                    return;
                }
            }
            hookMethod.setCategory(null);
            hookMethod.setRule(null);
        };

        hookMethodList.add(new HookMethod.Builder().setCls(Settings.Secure.class).setMethodName("getString")
                .setCategory(Behavior.Category.ANDROID_ID).setRule(Behavior.Rule.ANDROID_ID)
                .setListener(listener).build());
        hookMethodList.add(new HookMethod.Builder().setCls(Settings.System.class).setMethodName("getString")
                .setCategory(Behavior.Category.ANDROID_ID).setRule(Behavior.Rule.ANDROID_ID)
                .setListener(listener).build());
    }

    private void addAppInfo() {
        hookMethodList.add(new HookMethod.Builder().setClsName("android.app.ApplicationPackageManager").setMethodName("getPackageInfo")
                .setCategory(Behavior.Category.APP_INFO).setRule(Behavior.Rule.APP_INFO)
                .setListener((hookMethod, param, lpp) -> {
                    if (param != null && param.args.length >= 1 && param.args[0] instanceof String) {
                        if (!TextUtils.equals(param.args[0].toString(), lpp.packageName)) {
                            hookMethod.setCategory(Behavior.Category.APP_INFO);
                            hookMethod.setRule(Behavior.Rule.APP_INFO);
                            return;
                        }
                    }
                    hookMethod.setCategory(null);
                    hookMethod.setRule(null);
                }).build());
    }

    private void addCellInfo() {
        hookMethodList.add(new HookMethod.Builder().setCls(TelephonyManager.class).setMethodName("getSignalStrength")
                .setCategory(Behavior.Category.CELL_INFO).setRule(Behavior.Rule.CELL_INFO).build());
        hookMethodList.add(new HookMethod.Builder().setCls(TelephonyManager.class).setMethodName("listen")
                .setCategory(Behavior.Category.CELL_INFO).setRule(Behavior.Rule.CELL_INFO).build());
        hookMethodList.add(new HookMethod.Builder().setCls(TelephonyManager.class).setMethodName("getAllCellInfo")
                .setCategory(Behavior.Category.CELL_INFO).setRule(Behavior.Rule.CELL_INFO).build());
        hookMethodList.add(new HookMethod.Builder().setCls(ConnectivityManager.class).setMethodName("getActiveNetworkInfo")
                .setCategory(Behavior.Category.CELL_INFO).setRule(Behavior.Rule.CELL_INFO).build());
    }

    private void addNetType() {
        hookMethodList.add(new HookMethod.Builder().setCls(NetworkCapabilities.class).setMethodName("hasTransport")
                .setCategory(Behavior.Category.NET_INFO).setRule(Behavior.Rule.NET_TYPE).build());
        hookMethodList.add(new HookMethod.Builder().setCls(TelephonyManager.class).setMethodName("getNetworkType")
                .setCategory(Behavior.Category.NET_INFO).setRule(Behavior.Rule.NET_TYPE).build());
        hookMethodList.add(new HookMethod.Builder().setCls(NetworkInfo.class).setMethodName("getType")
                .setCategory(Behavior.Category.NET_INFO).setRule(Behavior.Rule.NET_TYPE).build());
        hookMethodList.add(new HookMethod.Builder().setCls(NetworkInfo.class).setMethodName("isConnectedOrConnecting")
                .setCategory(Behavior.Category.NET_INFO).setRule(Behavior.Rule.NET_CONNECTED).build());
        hookMethodList.add(new HookMethod.Builder().setCls(NetworkInfo.class).setMethodName("isConnected")
                .setCategory(Behavior.Category.NET_INFO).setRule(Behavior.Rule.NET_CONNECTED).build());
        hookMethodList.add(new HookMethod.Builder().setCls(NetworkInfo.class).setMethodName("isAvailable")
                .setCategory(Behavior.Category.NET_INFO).setRule(Behavior.Rule.NET_CONNECTED).build());
        hookMethodList.add(new HookMethod.Builder().setCls(NetworkInfo.class).setMethodName("getTypeName")
                .setCategory(Behavior.Category.NET_INFO).setRule(Behavior.Rule.NET_TYPE_NAME).build());
    }

    private boolean isTelephonyManagerListenerTypeMatch(XC_MethodHook.MethodHookParam param, int event) {
        return param != null && param.args != null && param.args.length == 2
                && param.args[1] instanceof Integer && ((Integer) param.args[1] == event);
    }

    private void addPhoneCallState() {
        hookMethodList.add(new HookMethod.Builder().setCls(TelephonyManager.class).setMethodName("getCallState")
                .setCategory(Behavior.Category.PHONE_CALL_STATE).setRule(Behavior.Rule.PHONE_CALL_STATE).build());
        hookMethodList.add(new HookMethod.Builder().setCls(TelephonyManager.class).setMethodName("listen")
                .setCategory(Behavior.Category.PHONE_CALL_STATE).setRule(Behavior.Rule.PHONE_CALL_STATE)
                .setListener((hookMethod, param, lpp) -> {
                    if (isTelephonyManagerListenerTypeMatch(param, PhoneStateListener.LISTEN_CALL_STATE)) {
                        hookMethod.setRule(Behavior.Rule.PHONE_CALL_STATE);
                    } else {
                        hookMethod.setRule(null);
                    }
                }).build());
        hookMethodList.add(new HookMethod.Builder().setCls(TelephonyManager.class).setMethodName("registerTelephonyCallback")
                .setCategory(Behavior.Category.PHONE_CALL_STATE).setRule(Behavior.Rule.PHONE_CALL_STATE).build());
    }

    private void addPhoneData() {
        hookMethodList.add(new HookMethod.Builder().setCls(TelephonyManager.class).setMethodName("getDataState")
                .setCategory(Behavior.Category.PHONE_DATA).setRule(Behavior.Rule.PHONE_DATA).build());
        hookMethodList.add(new HookMethod.Builder().setCls(TelephonyManager.class).setMethodName("getDataActivity")
                .setCategory(Behavior.Category.PHONE_DATA).setRule(Behavior.Rule.PHONE_DATA).build());
    }

    private void addPhoneService() {
        hookMethodList.add(new HookMethod.Builder().setCls(TelephonyManager.class).setMethodName("getServiceState")
                .setCategory(Behavior.Category.PHONE_SERVICE_STATE).setRule(Behavior.Rule.PHONE_SERVICE_STATE).build());
    }

    private void addBroadcast() {
        hookMethodList.add(new HookMethod.Builder().setCls(BroadcastReceiver.class).setMethodName("onReceive")
                .setHookMethodType(HookMethod.HookMethodType.ABSTRACT)
                .setCategory(Behavior.Category.BROADCAST).setRule(Behavior.Rule.RECEIVE_BROADCAST).build());

        hookMethodList.add(new HookMethod.Builder().setCls(ContextWrapper.class).setMethodName("registerReceiver")
                .setCategory(Behavior.Category.BROADCAST).setRule(Behavior.Rule.REGISTER_BROADCAST).build());
    }

    private boolean hookFilterOutputStream = false;

    private HookMethod.HookedMethodListener getShellHookedMethodListener1(boolean isCheckShell) {
        return (hookMethod, param, lpp) -> {
            if (param.args != null && param.args.length > 0 && param.args[0] instanceof Object[]) {
                for (Object command : (Object[]) param.args[0]) {
                    if (TextUtils.equals(command.toString(), "sh")) {
                        hookFilterOutputStream = true;
                        hookMethod.setRule(null);
                        return;
                    }
                }

                hookFilterOutputStream = false;
                if (isCheckShell) {
                    hookMethod.setCategory(Behavior.Category.SHELL);
                    hookMethod.setRule(Behavior.Rule.SHELL);
                    return;
                }

                Pair<String, String> categoryAndDesc = getCategoryAndDesc(getCommandString(param));
                if (categoryAndDesc == null) {
                    hookMethod.setRule(null);
                } else {
                    hookMethod.setCategory(categoryAndDesc.first);
                    hookMethod.setRule(categoryAndDesc.second);
                }
            }
        };
    }

    private String getCommandString(XC_MethodHook.MethodHookParam param) {
        String commandString = null;
        try {
            if (param.args[0] instanceof byte[]) {
                commandString = new String((byte[]) param.args[0]);
            } else {
                commandString = Arrays.toString((String[]) param.args[0]);
            }
            if (!TextUtils.isEmpty(commandString)) {
                if (commandString.startsWith("[")) {
                    commandString = commandString.replace("[", "");
                }
                if (commandString.endsWith("]")) {
                    commandString = commandString.replace("]", "");
                }
                if (commandString.contains(",")) {
                    commandString = commandString.replace(",", "");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return commandString;
    }

    private HookMethod.HookedMethodListener getShellHookedMethodListener2(boolean isCheckShell) {
        return (hookMethod, param, lpp) -> {
            if (hookFilterOutputStream) {
                hookFilterOutputStream = false;
                if (param.args != null && param.args.length > 0 && param.args[0] instanceof byte[]) {
                    if (isCheckShell) {
                        hookMethod.setCategory(Behavior.Category.SHELL);
                        hookMethod.setRule(Behavior.Rule.SHELL);
                    } else {
                        Pair<String, String> categoryAndDesc = getCategoryAndDesc(new String((byte[]) param.args[0]));
                        if (categoryAndDesc == null) {
                            hookMethod.setRule(null);
                        } else {
                            hookMethod.setCategory(categoryAndDesc.first);
                            hookMethod.setRule(categoryAndDesc.second);
                        }
                    }
                } else {
                    hookMethod.setRule(null);
                }
            } else {
                hookMethod.setRule(null);
            }
        };
    }

    private void addShell() {
        hookMethodList.add(new HookMethod.Builder().setClsName("java.lang.ProcessImpl").setMethodName("start")
                .setCategory(Behavior.Category.SHELL).setRule(Behavior.Rule.SHELL_INTERCEPT)
                .setListener(getShellHookedMethodListener1(false)).build());

        hookMethodList.add(new HookMethod.Builder().setCls(FilterOutputStream.class).setMethodName("write")
                .setCategory(Behavior.Category.SHELL).setRule(Behavior.Rule.SHELL_INTERCEPT)
                .setListener(getShellHookedMethodListener2(false)).build());

        hookMethodList.add(new HookMethod.Builder().setClsName("java.lang.ProcessImpl").setMethodName("start")
                .setCategory(Behavior.Category.SHELL).setRule(Behavior.Rule.SHELL)
                .setListener(getShellHookedMethodListener1(true)).build());

        hookMethodList.add(new HookMethod.Builder().setCls(FilterOutputStream.class).setMethodName("write")
                .setCategory(Behavior.Category.SHELL).setRule(Behavior.Rule.SHELL)
                .setListener(getShellHookedMethodListener2(true)).build());
    }

    private Pair<String, String> getCategoryAndDesc(String command) {
        if (TextUtils.isEmpty(command)) {
            return null;
        }
        Log.d(TAG, "command:" + command);
        if (command.contains("pm list package")
                || command.contains("dumpsys package")) {
            return new Pair<>(Behavior.Category.APP_LIST, Behavior.Rule.GET_APP_LIST);
        } else if (command.contains("service call iphonesubinfo 7")
                || command.contains("getprop | grep 'imsi'")) {
            return new Pair<>(Behavior.Category.IMSI, Behavior.Rule.IMSI);
        } else if (command.contains("service call iphonesubinfo 12")
                || command.contains("service call iphonesubinfo 11")
                || command.contains("getprop | grep iccid")
                || command.contains("dumpsys telephony.registry | grep iccid")) {
            return new Pair<>(Behavior.Category.SIM, Behavior.Rule.SIM_SERIAL_NUMBER);
        } else if (command.contains("service call iphonesubinfo 1")
                || command.contains("service call iphonesubinfo 2")
                || command.contains("getprop | grep imei")
                || command.contains("dumpsys iphonesubinfo")) {
            return new Pair<>(Behavior.Category.IMEI, Behavior.Rule.IMEI);
        } else if (command.contains("get-serialno")
                || command.contains("getprop ro.serialno")) {
            return new Pair<>(Behavior.Category.SERIAL, Behavior.Rule.SERIAL);
        } else if (command.contains("getprop")) {
            return new Pair<>(Behavior.Category.SYSTEM_PROPERTIES, Behavior.Rule.SYSTEM_PROPERTIES);
        } else if (command.contains("settings get secure android_id")) {
            return new Pair<>(Behavior.Category.ANDROID_ID, Behavior.Rule.ANDROID_ID);
        } else if (command.contains("cat /sys/class/net/wlan0/address")
                || command.contains("busybox ifconfig")
                || command.contains("ifconfig wlan0 | grep 'HWaddr' | awk '{print $5}'")) {
            return new Pair<>(Behavior.Category.MAC, Behavior.Rule.MAC);
        } else if (command.endsWith("cmd wifi start-scan")
                || command.contains("cmd wifi scan")
                || command.contains("wpa_cli scan")) {
            return new Pair<>(Behavior.Category.WIFI, Behavior.Rule.SCAN_WIFI);
        } else if (command.contains("wpa_cli scan_results")) {
            return new Pair<>(Behavior.Category.WIFI, Behavior.Rule.GET_SCAN_WIFI_RESULTS);
        } else if (command.contains("dumpsys wifi | grep BSSID")) {
            return new Pair<>(Behavior.Category.WIFI, Behavior.Rule.WIFI_BSSID);
        } else if (command.contains("dumpsys wifi | grep SSID")) {
            return new Pair<>(Behavior.Category.WIFI, Behavior.Rule.WIFI_SSID);
        } else if (command.contains("dumpsys wifi | grep RSSI")) {
            return new Pair<>(Behavior.Category.WIFI, Behavior.Rule.WIFI_RSSI);
        } else if (command.contains("dumpsys wifi")) {
            return new Pair<>(Behavior.Category.WIFI, Behavior.Rule.WIFI_INFO);
        } else if (command.contains("dumpsys telephony.registry | grep -E")
                && command.contains("mDataActivity")) {
            return new Pair<>(Behavior.Category.PHONE_DATA, Behavior.Rule.PHONE_DATA);
        } else if (command.contains("dumpsys telephony.registry | grep -E")
                && command.contains("mServiceState")) {
            return new Pair<>(Behavior.Category.PHONE_SERVICE_STATE, Behavior.Rule.PHONE_SERVICE_STATE);
        } else if (command.contains("dumpsys telephony.registry | grep -i 'sim'")) {
            return new Pair<>(Behavior.Category.SIM, Behavior.Rule.SIM_STATE);
        } else if (command.contains("dumpsys telephony")) {
            return new Pair<>(Behavior.Category.SIM, Behavior.Rule.SIM_INFO);
        } else if (command.endsWith("ps")) {
            return new Pair<>(Behavior.Category.RUNNING_APP, Behavior.Rule.GET_RUNNING_APP);
        } else if (command.contains("/proc/sys/kernel/random/boot_id")) {
            return new Pair<>(Behavior.Category.BOOT_ID, Behavior.Rule.BOOT_ID);
        } else {
            return null;
        }
    }

    private void addMsaId() {
        hookMethodList.add(new HookMethod.Builder().setClsName("com.bun.miitmdid.core.MdidSdkHelper").setMethodName("InitSdk")
                .setCategory(Behavior.Category.MSA_ID).setRule(Behavior.Rule.OAID)
                .setListener((hookMethod, param, lpp) -> {
                    if (param != null && param.args != null && param.args.length > 0) {
                        if (param.args.length == 3) {
                            if (param.args[1] instanceof Boolean && (Boolean) param.args[1]) {
                                hookMethod.setRule(Behavior.Rule.OAID);
                                return;
                            }
                        } else if (param.args.length == 6) {
                            if (param.args[2] instanceof Boolean && (Boolean) param.args[2]) {
                                hookMethod.setRule(Behavior.Rule.OAID);
                                return;
                            }
                        }
                    }
                    hookMethod.setRule(null);
                }).build());
        hookMethodList.add(new HookMethod.Builder().setClsName("com.bun.miitmdid.core.MdidSdkHelper").setMethodName("InitSdk")
                .setCategory(Behavior.Category.MSA_ID).setRule(Behavior.Rule.VAID)
                .setListener((hookMethod, param, lpp) -> {
                    if (param != null && param.args != null && param.args.length > 0) {
                        if (param.args.length == 3) {
                            if (param.args[1] instanceof Boolean && (Boolean) param.args[1]) {
                                hookMethod.setRule(Behavior.Rule.VAID);
                                return;
                            }
                        } else if (param.args.length == 6) {
                            if (param.args[3] instanceof Boolean && (Boolean) param.args[3]) {
                                hookMethod.setRule(Behavior.Rule.VAID);
                                return;
                            }
                        }
                    }
                    hookMethod.setRule(null);
                }).build());
        hookMethodList.add(new HookMethod.Builder().setClsName("com.bun.miitmdid.core.MdidSdkHelper").setMethodName("InitSdk")
                .setCategory(Behavior.Category.MSA_ID).setRule(Behavior.Rule.AAID)
                .setListener((hookMethod, param, lpp) -> {
                    if (param != null && param.args != null && param.args.length > 0) {
                        if (param.args.length == 3) {
                            if (param.args[1] instanceof Boolean && (Boolean) param.args[1]) {
                                hookMethod.setRule(Behavior.Rule.AAID);
                                return;
                            }
                        } else if (param.args.length == 6) {
                            if (param.args[4] instanceof Boolean && (Boolean) param.args[4]) {
                                hookMethod.setRule(Behavior.Rule.AAID);
                                return;
                            }
                        }
                    }
                    hookMethod.setRule(null);
                }).build());
    }

    private void addFile() {
        hookMethodList.add(new HookMethod.Builder().setCls(File.class).setHookMethodType(HookMethod.HookMethodType.CONSTRUCTOR)
                .setCategory(Behavior.Category.FILE).setRule(Behavior.Rule.FILE_INTERCEPT)
                .setListener((hookMethod, param, lpp) -> {
                    if (param != null && param.args != null && param.args.length > 0 && param.args[0] instanceof String) {
                        String filePath = param.args[0].toString();
                        if (!TextUtils.isEmpty(filePath)) {
                            Log.d(TAG, "filePath:" + filePath);
                            if (filePath.contains("/sys/class/net/wlan0/address") || filePath.contains("/sys/class/net/eth0/address")) {
                                hookMethod.setCategory(Behavior.Category.MAC);
                                hookMethod.setRule(Behavior.Rule.MAC);
                                return;
                            } else if (filePath.contains("/proc/") && filePath.contains("/cmdline")) {
                                hookMethod.setCategory(Behavior.Category.RUNNING_APP);
                                hookMethod.setRule(Behavior.Rule.GET_RUNNING_APP);
                                return;
                            } else if (filePath.contains("/proc/sys/kernel/random/boot_id")) {
                                hookMethod.setCategory(Behavior.Category.BOOT_ID);
                                hookMethod.setRule(Behavior.Rule.BOOT_ID);
                                return;
                            }
                        }
                    }
                    hookMethod.setRule(null);
                }).build());
    }

    public List<CheckItem> getCheckItemList(boolean isInitConfig, Set<String> checkItemSet) {
        this.isInitConfig = isInitConfig;
        this.checkItemSet = checkItemSet;

        List<CheckItem> checkItemList = new ArrayList<>();
        checkItemList.add(getCheckItem(Behavior.Category.MAC, Collections.singletonList(Behavior.Rule.MAC)));

        checkItemList.add(getCheckItem(Behavior.Category.SERIAL, Collections.singletonList(Behavior.Rule.SERIAL)));

        checkItemList.add(getCheckItem(Behavior.Category.ANDROID_ID, Collections.singletonList(Behavior.Rule.ANDROID_ID)));

        checkItemList.add(getCheckItem(Behavior.Category.IMSI, Collections.singletonList(Behavior.Rule.IMSI)));

        checkItemList.add(getCheckItem(Behavior.Category.IMEI, Collections.singletonList(Behavior.Rule.IMEI)));

        checkItemList.add(getCheckItem(Behavior.Category.MSA_ID, Arrays.asList(Behavior.Rule.OAID,
                Behavior.Rule.VAID,
                Behavior.Rule.AAID)));

        checkItemList.add(getCheckItem(Behavior.Category.BOOT_ID, Collections.singletonList(Behavior.Rule.BOOT_ID)));

        checkItemList.add(getCheckItem(Behavior.Category.APP_LIST, Collections.singletonList(Behavior.Rule.GET_APP_LIST)));

        checkItemList.add(getCheckItem(Behavior.Category.RUNNING_APP, Collections.singletonList(Behavior.Rule.GET_RUNNING_APP)));

        checkItemList.add(getCheckItem(Behavior.Category.APP_INFO, Collections.singletonList(Behavior.Rule.APP_INFO)));

        checkItemList.add(getCheckItem(Behavior.Category.PERMISSION, Arrays.asList(Behavior.Rule.CHECK_PERMISSION,
                Behavior.Rule.REQUEST_PERMISSION)));

        checkItemList.add(getCheckItem(Behavior.Category.LOCATE, Arrays.asList(Behavior.Rule.REQUEST_LOCATION,
                Behavior.Rule.CELL_LOCATION,
                Behavior.Rule.GET_CURRENT_LOCATION,
                Behavior.Rule.GET_LAST_KNOW_LOCATION,
                Behavior.Rule.LOCATION_INFO)));

        checkItemList.add(getCheckItem(Behavior.Category.SIM, Arrays.asList(Behavior.Rule.SIM_SERIAL_NUMBER,
                Behavior.Rule.SIM_OPERATOR,
                Behavior.Rule.SIM_OPERATOR_NAME,
                Behavior.Rule.SIM_COUNTRY_ISO,
                Behavior.Rule.SIM_NETWORK_COUNTRY_ISO,
                Behavior.Rule.SIM_NETWORK_OPERATOR,
                Behavior.Rule.SIM_NETWORK_OPERATOR_NAME,
                Behavior.Rule.SIM_STATE,
                Behavior.Rule.SIM_INFO)));

        checkItemList.add(getCheckItem(Behavior.Category.CELL_INFO, Collections.singletonList(Behavior.Rule.CELL_INFO)));

        checkItemList.add(getCheckItem(Behavior.Category.ACCOUNT, Arrays.asList(Behavior.Rule.ADD_ACCOUNT,
                Behavior.Rule.GET_ACCOUNT,
                Behavior.Rule.REMOVE_ACCOUNT)));

        checkItemList.add(getCheckItem(Behavior.Category.SENSOR, Arrays.asList(Behavior.Rule.GET_SENSOR,
                Behavior.Rule.LISTEN_SENSOR)));

        checkItemList.add(getCheckItem(Behavior.Category.WIFI, Arrays.asList(Behavior.Rule.SCAN_WIFI,
                Behavior.Rule.GET_SCAN_WIFI_RESULTS,
                Behavior.Rule.WIFI_INFO,
                Behavior.Rule.WIFI_IP,
                Behavior.Rule.WIFI_MAC,
                Behavior.Rule.WIFI_BSSID,
                Behavior.Rule.WIFI_SSID,
                Behavior.Rule.WIFI_RSSI)));

        checkItemList.add(getCheckItem(Behavior.Category.STORAGE, Collections.singletonList(Behavior.Rule.GET_EXTERNAL_STORAGE_DIRECTORY)));

        checkItemList.add(getCheckItem(Behavior.Category.INET_ADDRESS, Collections.singletonList(Behavior.Rule.INET_ADDRESS)));

        checkItemList.add(getCheckItem(Behavior.Category.NET_INFO, Arrays.asList(Behavior.Rule.NET_TYPE,
                Behavior.Rule.NET_TYPE_NAME,
                Behavior.Rule.NET_CONNECTED)));

        checkItemList.add(getCheckItem(Behavior.Category.BLUETOOTH, Arrays.asList(Behavior.Rule.SCAN_BLUETOOTH,
                Behavior.Rule.BLUETOOTH_INFO)));

        checkItemList.add(getCheckItem(Behavior.Category.CAMERA, Arrays.asList(Behavior.Rule.TAKE_PHOTO, Behavior.Rule.CAMERA_LIST)));

        checkItemList.add(getCheckItem(Behavior.Category.MEDIA, Arrays.asList(Behavior.Rule.MEDIA_AUDIO,
                Behavior.Rule.MEDIA_IMAGE,
                Behavior.Rule.MEDIA_VIDEO)));

        checkItemList.add(getCheckItem(Behavior.Category.BROWSER_BOOKMARKS, Collections.singletonList(Behavior.Rule.BROWSER_BOOKMARKS)));

        checkItemList.add(getCheckItem(Behavior.Category.PHONE_CALL_STATE, Collections.singletonList(Behavior.Rule.PHONE_CALL_STATE)));

        checkItemList.add(getCheckItem(Behavior.Category.PHONE_DATA, Collections.singletonList(Behavior.Rule.PHONE_DATA)));

        checkItemList.add(getCheckItem(Behavior.Category.PHONE_SERVICE_STATE, Collections.singletonList(Behavior.Rule.PHONE_SERVICE_STATE)));

        checkItemList.add(getCheckItem(Behavior.Category.SHELL, Collections.singletonList(Behavior.Rule.SHELL)));

        checkItemList.add(getCheckItem(Behavior.Category.SYSTEM_PROPERTIES, Collections.singletonList(Behavior.Rule.SYSTEM_PROPERTIES)));

        checkItemList.add(getCheckItem(Behavior.Category.BROADCAST, Arrays.asList(Behavior.Rule.RECEIVE_BROADCAST, Behavior.Rule.REGISTER_BROADCAST)));

        return checkItemList;
    }

    private CheckItem getCheckItem(String category, List<String> descList) {
        List<CheckItemChild> group = new ArrayList<>();
        for (String desc : descList) {
            group.add(new CheckItemChild(desc, isInitConfig || checkItemSet.contains(desc)));
        }
        return new CheckItem(category, group);
    }

}
