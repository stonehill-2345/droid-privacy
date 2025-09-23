package com.privacy2345.droidprivacy.hook;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.privacy2345.droidprivacy.constant.Constant;
import com.privacy2345.droidprivacy.util.PreferenceManager;
import com.privacy2345.droidprivacy.util.StringHelper;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dalvik.system.DexFile;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * Xposed Hook模块核心实现类<br>
 * 实现IXposedHookLoadPackage接口，在目标应用启动时进行方法拦截<br>
 * 主要功能：<br>
 * 1. 应用启动监听 - 监听目标应用的启动事件<br>
 * 2. Hook配置初始化 - 根据配置规则初始化方法拦截<br>
 * 3. 反检测机制 - 绕过应用对Xposed的检测<br>
 * 4. 隐私API拦截 - 拦截敏感API调用并记录详细信息<br>
 * 5. 数据收集上报 - 收集调用信息并上报到指定平台<br>
 * <p>
 * 工作原理：通过Xposed框架在应用运行时动态修改方法行为，实现对隐私相关API调用的监控和数据收集
 *
 * @author : zhongjy@2345.com
 */
public class HookModule implements IXposedHookLoadPackage {
    public static Context context;
    public static boolean localCheckFileInput;
    public static boolean uploadData = true;

    private static final String TAG = Constant.COMMON_TAG + "_HookModule";
    private static final List<String> NOT_SUPPORT_APP = Collections.singletonList(Constant.PackageName.SELF);
    private Set<String> hookRulesSet;
    private String hookBrand;
    private boolean overloadFilter;
    private Set<String> allClassNames = null;

    @Override
    public void handleLoadPackage(LoadPackageParam lpp) {
        XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                if (!NOT_SUPPORT_APP.contains(lpp.packageName)) {
                    context = (Context) param.args[0];
                    initHookConfig(context);
                    closeAntiXposed();
                    hookWithPackageName(context, lpp);
                }
            }
        });
    }

    /**
     * 获取检测配置
     */
    private void initHookConfig(Context context) {
        Log.d(TAG, "获取检测配置");
        if (context != null) {
            PreferenceManager preferenceManager = PreferenceManager.getInstance(context);
            String checkItemString = preferenceManager.getString(Constant.Cache.CHECK_ITEM, null);
            hookRulesSet = StringHelper.splitToSet(checkItemString, "、", hookRulesSet);
            hookBrand = preferenceManager.getString(Constant.Cache.MOCK_BRAND, Build.BRAND);
            overloadFilter = preferenceManager.getBoolean(Constant.Cache.OVERLOAD_FILTER, true);
            localCheckFileInput = preferenceManager.getBoolean(Constant.Cache.LOCAL_CHECK_FILE_INPUT, true);
        } else {
            Log.e(TAG, "获取上下文失败");
        }
    }

    /**
     * 绕过Xposed检测，防止某些项目关闭xposed模块
     */
    private void closeAntiXposed() {
        Log.d(TAG, "绕过Xposed检测");
        // 绕过jar Class检测
        try {
            XposedHelpers.findAndHookMethod(ClassLoader.class, "loadClass", String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (param.args != null && param.args[0] != null && param.args[0].toString().startsWith("de.robv.android.xposed.")) {
                        // 改成一个不存在的类
                        param.args[0] = "de.robv.android.xposed.hello";
                    }
                    super.beforeHookedMethod(param);
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
            Log.d(TAG, "绕过Xposed检测error:" + e.getMessage());
        }
    }

    private void hookWithPackageName(Context context, @NonNull LoadPackageParam lpp) {
        if (hookRulesSet == null || hookRulesSet.isEmpty()) {
            Log.e(TAG, "检测规则为空1");
            return;
        }
        List<HookMethod> methodList = HookConfigManager.getInstance().getConfig(new HashSet<>(hookRulesSet));
        if (methodList == null || methodList.isEmpty()) {
            Log.e(TAG, "检测规则为空2");
            return;
        }
        Toast.makeText(context, "开始检测" + lpp.packageName, Toast.LENGTH_SHORT).show();

        if (!TextUtils.isEmpty(hookBrand) && !TextUtils.equals(hookBrand, Build.BRAND)) {
            XposedHelpers.setStaticObjectField(android.os.Build.class, "BRAND", hookBrand);
            XposedHelpers.setStaticObjectField(android.os.Build.class, "MANUFACTURER", hookBrand);
            Log.d(TAG, "开始hook，修改手机品牌为" + hookBrand);
        }

        Log.d(TAG, "开始hook，包名： " + lpp.packageName + ", 进程：" + lpp.processName + ", 类加载器：" + lpp.classLoader.toString());

        String hookClassName;
        for (HookMethod hookMethod : methodList) {
            hookClassName = hookMethod.getClassName();
            Class<?> clazz = findClass(hookClassName, lpp.classLoader);
            if (clazz != null) {
                if (hookMethod.getHookMethodType() == HookMethod.HookMethodType.CONSTRUCTOR) {
                    hookConstructorMethod(lpp, clazz, hookMethod);
                } else if (hookMethod.getHookMethodType() == HookMethod.HookMethodType.ABSTRACT) {
                    hookAbstractMethod(lpp, clazz, hookMethod);
                } else if (hookMethod.getHookMethodType() == HookMethod.HookMethodType.NORMAL) {
                    hookNormalMethod(lpp, clazz, hookMethod, overloadFilter);
                }
            }
        }
    }

    private Class findClass(String className, ClassLoader classLoader) {
        try {
            return XposedHelpers.findClassIfExists(className, classLoader);
        } catch (Throwable e) {
            e.printStackTrace();
            Log.e(TAG, "hook失败，没找到类： " + className);
        }
        return null;
    }

    private void hookConstructorMethod(LoadPackageParam lpp, Class targetClass, HookMethod hookMethod) {
        try {
            XposedBridge.hookAllConstructors(targetClass, hookMethod.getMethodHook(lpp, targetClass, hookRulesSet, overloadFilter));
            Log.d(TAG, "hook成功：" + hookMethod.getClassName() + "#构造");
        } catch (Throwable e) {
            e.printStackTrace();
            Log.e(TAG, "hook失败：" + hookMethod.getClassName() + "#构造");
        }
    }

    private void hookAbstractMethod(LoadPackageParam lpp, Class targetClass, HookMethod hookMethod) {
        Set<String> classNames = getAllClassNames(lpp);
        if (classNames == null || classNames.isEmpty()) {
            return;
        }
        for (String className : classNames) {
            try {
                Class<?> currentClass = lpp.classLoader.loadClass(className);
                // 跳过抽象类和接口
                if (Modifier.isAbstract(currentClass.getModifiers()) || currentClass.isInterface()) {
                    continue;
                }
                // 判断是否是目标类子类
                if (targetClass.isAssignableFrom(currentClass)) {
                    hookNormalMethod(lpp, currentClass, hookMethod, false);
                }
            } catch (Throwable t) {
                // 某些类无法加载，直接跳过
                Log.e(TAG, "hook失败：" + targetClass.getName() + "的子类" + className + "#" + hookMethod.getMethodName() + "，原因：" + t.getMessage());
            }
        }
    }

    private void hookNormalMethod(LoadPackageParam lpp, Class targetClass, HookMethod hookMethod, boolean overloadFilter) {
        try {
            XposedBridge.hookAllMethods(targetClass, hookMethod.getMethodName(), hookMethod.getMethodHook(lpp, targetClass, hookRulesSet, overloadFilter));
            Log.d(TAG, "hook成功：" + targetClass.getName() + "#" + hookMethod.getMethodName());
        } catch (Throwable e) {
            e.printStackTrace();
            Log.e(TAG, "hook失败：" + targetClass.getName() + "#" + hookMethod.getMethodName());
        }
    }

    public Set<String> getAllClassNames(LoadPackageParam lpp) {
        if (allClassNames != null && !allClassNames.isEmpty()) {
            return allClassNames;
        }
        if (allClassNames == null) {
            allClassNames = new HashSet<>();
        }
        try {
            // 获取 pathList 字段
            Object pathList = XposedHelpers.getObjectField(lpp.classLoader, "pathList");

            // 获取 dexElements 字段
            Object[] dexElements = (Object[]) XposedHelpers.getObjectField(pathList, "dexElements");

            for (Object element : dexElements) {
                // 获取每个 dexElement 的 dexFile
                DexFile dexFile = (DexFile) XposedHelpers.getObjectField(element, "dexFile");
                if (dexFile != null) {
                    Enumeration<String> entries = dexFile.entries();
                    while (entries.hasMoreElements()) {
                        allClassNames.add(entries.nextElement());
                    }
                }
            }
        } catch (Throwable e) {
            Log.e(TAG, "获取所有 dex 类名失败: " + Log.getStackTraceString(e));
        }
        return allClassNames;
    }

    public Set<String> getMainDexClassNames(LoadPackageParam lpp) {
        if (allClassNames != null && !allClassNames.isEmpty()) {
            return allClassNames;
        }
        if (allClassNames == null) {
            allClassNames = new HashSet<>();
        }
        try {
            String apkPath = lpp.appInfo.sourceDir; // APK 路径
            DexFile dexFile = new DexFile(apkPath);
            Enumeration<String> entries = dexFile.entries();
            while (entries.hasMoreElements()) {
                String className = entries.nextElement();
                allClassNames.add(className);
            }
        } catch (Throwable e) {
            Log.e(TAG, "获取所有 dex 类名失败: " + Log.getStackTraceString(e));
        }
        return allClassNames;
    }

}


