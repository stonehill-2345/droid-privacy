package com.privacy2345.droidprivacy.hook;

import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.privacy2345.droidprivacy.constant.Behavior;
import com.privacy2345.droidprivacy.constant.Constant;
import com.privacy2345.droidprivacy.model.ApiCallRecord;
import com.privacy2345.droidprivacy.output.DataDispatchManager;
import com.privacy2345.droidprivacy.util.CallerIdentificationUtil;
import com.privacy2345.droidprivacy.util.StringHelper;
import com.privacy2345.droidprivacy.util.SystemBroadcastUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Hook方法配置模型类<br>
 * 定义需要拦截的方法的完整信息，包括类名、方法名、参数类型等<br>
 * 支持多种Hook类型：普通方法、构造方法、抽象方法、静态字段访问<br>
 * 主要功能：<br>
 * 1. 方法定义 - 完整描述需要Hook的方法信息<br>
 * 2. 类型支持 - 支持各种Java方法类型的Hook<br>
 * 3. 建造者模式 - 使用Builder模式简化复杂对象的创建<br>
 * 4. Hook执行 - 提供实际执行Hook操作的逻辑<br>
 * 5. 数据收集 - 在方法调用时收集详细的调用信息<br>
 * <p/>
 * 使用场景：在HookModule中作为配置对象，定义需要监控的隐私API
 *
 * @author : zhongjy@2345.com
 */
public class HookMethod {

    public enum HookMethodType {
        ABSTRACT, CONSTRUCTOR, NORMAL, STATIC_FIELD
    }

    private static final String TAG = Constant.COMMON_TAG;
    private static final Map<String, Integer> invokeSerialNumberMap = new HashMap<>();
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    private String ClassName;
    private String methodName;
    private String fieldName;
    private String category;
    private String rule;
    private HookMethodType hookMethodType;
    private HookedMethodListener listener;

    public String getClassName() {
        return ClassName;
    }

    public void setClassName(String className) {
        ClassName = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public HookMethodType getHookMethodType() {
        return hookMethodType;
    }

    public void setHookMethodType(HookMethodType hookMethodType) {
        this.hookMethodType = hookMethodType;
    }

    public HookedMethodListener getListener() {
        return listener;
    }

    public void setListener(HookedMethodListener listener) {
        this.listener = listener;
    }

    private HookMethod() {

    }

    public XC_MethodHook getMethodHook(XC_LoadPackage.LoadPackageParam lpp, Class finalHookClass, Set<String> checkItemSet, boolean overloadFilter) {

        return new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                long timeMills = System.currentTimeMillis();
                if (listener != null) {
                    listener.beforeHookedMethod(HookMethod.this, param, lpp);
                    if (checkItemSet != null && !checkItemSet.contains(rule)) {
                        return;
                    }
                }
                if (hookMethodType == HookMethodType.STATIC_FIELD) {
                    if (param == null || param.args == null || param.args.length == 0 || !TextUtils.equals((String) param.args[0], fieldName)) {
                        return;
                    }
                }
                // 堆栈信息
                StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
                StringBuilder stackInfoBuilder = new StringBuilder();
                for (int i = 0; i < stackTraceElements.length; i++) {
                    String elementString = stackTraceElements[i].toString();
                    // 过滤无效堆栈日志
                    if (i >= 6 && !(elementString.contains("com.privacy2345.droidprivacy.hook.HookMethod$1.beforeHookedMethod")
                            || elementString.contains("dalvik.system.VMStack.getThreadStackTrace(Native Method)")
                            || elementString.contains("java.lang.Thread.getStackTrace")
                            || elementString.contains("LegacyApiSupport.handleBefore")
                            || elementString.contains("J.callback")
                            || elementString.contains("LSPHooker_.isAvailable"))) {
                        stackInfoBuilder.append("           ").append(elementString).append("\n");
                    }
                }
                String stackInfo = stackInfoBuilder.toString();
                String invokerMethod = finalHookClass.getName();
                if (hookMethodType == HookMethodType.STATIC_FIELD) {
                    invokerMethod += "." + fieldName;
                } else if (hookMethodType == HookMethodType.CONSTRUCTOR) {
                    invokerMethod += "#构造方法";
                } else {
                    invokerMethod += "#" + param.method.getName();
                }

                boolean isOverloadInvoke = false;
                // 重载调用判定
                if (overloadFilter) {
                    ApiCallRecord last = DataDispatchManager.getInstance().getLastInvokerRecord(lpp.packageName);
                    if (last != null && TextUtils.equals(category, last.invokerCategory) && TextUtils.equals(rule, last.invokerRule)) {
                        long time = timeMills - last.timeMills;
                        if (time < 25) {
                            if (stackInfo.contains(last.invokerStack)) {
                                isOverloadInvoke = true;
                            }
                        } else {
                            Log.e(TAG + "_" + lpp.packageName, "触发方法：" + invokerMethod + "，重载调用，过滤失败，时间间隔：" + time + "毫秒");
                        }
                    }
                }

                StringBuilder builder = new StringBuilder();
                String timestamp = dateFormat.format(new Date(timeMills));
                String invokerDesc = CallerIdentificationUtil.getInvokerDesc(stackInfo);

                Integer value = invokeSerialNumberMap.get(lpp.packageName);
                int serialNumber = (value != null) ? value : 0;
                serialNumber++;

                builder.append("[*]触发序号：").append(serialNumber);
                builder.append("\n[*]触发时间：").append(timestamp);
                builder.append("\n[*]行为主体：").append(invokerDesc);
                if (!TextUtils.isEmpty(getCategory())) {
                    builder.append("\n[*]行为分类：").append(getCategory());
                }
                if (!TextUtils.isEmpty(getRule())) {
                    builder.append("\n[*]行为描述：").append(getRule());
                }
                if (isOverloadInvoke) {
                    builder.append("\n[*]重载调用提示！！！");
                }
                builder.append("\n[*]触发进程：").append(lpp.processName);
                builder.append("\n[*]触发方法：").append(invokerMethod);
                StringBuilder invokerMethodArgs = new StringBuilder();
                if (param.args != null && param.args.length > 0) {
                    builder.append("\n[*]方法参数:");
                    int i = 0;
                    String argString;
                    Pair<Boolean, String> paramPair;
                    for (Object arg : param.args) {
                        paramPair = getParam(arg);
                        if (!paramPair.first) {
                            return;
                        }
                        argString = paramPair.second;
                        builder.append("\n      参数").append(i++).append("：").append(argString);
                        invokerMethodArgs.append("参数").append(i).append("：").append(argString).append("\n");
                    }
                }
                builder.append("\n[*]调用堆栈：");
                builder.append("\n").append(stackInfo);

                ApiCallRecord apiCallRecord = new ApiCallRecord();
                apiCallRecord.serialNumber = String.valueOf(serialNumber);
                apiCallRecord.timeMills = timeMills;
                apiCallRecord.timestamp = timestamp;
                apiCallRecord.invokerName = invokerDesc;
                apiCallRecord.invokerCategory = getCategory();
                apiCallRecord.invokerRule = getRule();
                apiCallRecord.invokerProcess = lpp.processName;
                apiCallRecord.invokerMethod = invokerMethod;
                apiCallRecord.invokerMethodArgs = invokerMethodArgs.toString();
                apiCallRecord.invokerStack = stackInfo;
                if (isOverloadInvoke) {
                    Log.e(TAG + "_" + lpp.packageName, "触发方法：" + invokerMethod + "，重载调用，过滤");
                } else {
                    Log.d(TAG + "_" + lpp.packageName, builder.toString());
                    invokeSerialNumberMap.put(lpp.packageName, serialNumber);
                    DataDispatchManager.getInstance().write(lpp.packageName, Collections.singletonList(apiCallRecord));
                }
            }
        };
    }

    private Pair<Boolean, String> getParam(Object arg) {
        if (!TextUtils.isEmpty(getRule())) {
            switch (getRule()) {
                case Behavior.Rule.REGISTER_BROADCAST:
                case Behavior.Rule.RECEIVE_BROADCAST:
                    String broadcastAction = SystemBroadcastUtil.getSystemBroadcastAction(arg);
                    if (TextUtils.isEmpty(broadcastAction)) {
                        return new Pair<>(false, null);
                    } else {
                        return new Pair<>(true, broadcastAction);
                    }
            }
        }
        return new Pair<>(true, StringHelper.convertToString(arg));
    }

    public static final class Builder {
        private Class cls;
        private String clsName;
        private String methodName;
        private String fieldName;
        private String category;
        private String rule;
        private HookMethodType hookMethodType = HookMethodType.NORMAL;
        private HookedMethodListener listener;

        public Builder setCls(Class cls) {
            this.cls = cls;
            return this;
        }

        public Builder setClsName(String clsName) {
            this.clsName = clsName;
            return this;
        }

        public Builder setMethodName(String methodName) {
            this.methodName = methodName;
            return this;
        }

        public Builder setFieldName(String fieldName) {
            this.fieldName = fieldName;
            return this;
        }

        public Builder setCategory(String category) {
            this.category = category;
            return this;
        }

        public Builder setRule(String rule) {
            this.rule = rule;
            return this;
        }

        public Builder setListener(HookedMethodListener listener) {
            this.listener = listener;
            return this;
        }

        public Builder setHookMethodType(HookMethodType hookMethodType) {
            this.hookMethodType = hookMethodType;
            return this;
        }

        public HookMethod build() {
            HookMethod hookMethod = new HookMethod();
            hookMethod.setClassName(TextUtils.isEmpty(clsName) ? (cls == null ? null : cls.getName()) : clsName);
            if (TextUtils.isEmpty(hookMethod.getClassName())) {
                throw new RuntimeException("类名不能为空");
            }
            if (hookMethodType == HookMethodType.STATIC_FIELD) {
                if (TextUtils.isEmpty(fieldName)) {
                    throw new RuntimeException("属性不能为空");
                }
                hookMethod.setFieldName(fieldName);
            } else if (hookMethodType == HookMethodType.NORMAL || hookMethodType == HookMethodType.ABSTRACT) {
                if (TextUtils.isEmpty(methodName)) {
                    throw new RuntimeException("方法不能为空");
                }
                hookMethod.setMethodName(methodName);
            }
            hookMethod.setHookMethodType(hookMethodType);
            hookMethod.setCategory(category);
            hookMethod.setRule(rule);
            hookMethod.setListener(listener);
            return hookMethod;
        }
    }

    public interface HookedMethodListener {
        void beforeHookedMethod(HookMethod hookMethod, XC_MethodHook.MethodHookParam param, XC_LoadPackage.LoadPackageParam lpp);
    }

}

