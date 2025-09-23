package com.privacy2345.droidprivacy.util;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * HTTP请求工具类
 * 基于OkHttp提供GET和POST请求功能
 * 主要功能：
 * 1. GET请求 - 发送HTTP GET请求
 * 2. POST请求 - 发送JSON格式的HTTP POST请求
 * 3. 异步回调 - 支持异步请求和结果回调
 *
 * @author : zhongjy@2345.com
 */
public class HttpUtil {

    private static final OkHttpClient client = new OkHttpClient();

    public static void get(String url, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }

    public static void post(String url, String jsonData, Callback callback) {
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonData, mediaType);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }
}