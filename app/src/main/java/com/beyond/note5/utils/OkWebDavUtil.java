package com.beyond.note5.utils;

import android.util.Log;

import com.beyond.note5.sync.datasource.dav.BasicAuthenticator;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class OkWebDavUtil {

    public static OkHttpClient getClient(){
        return OkHttpClientHolder.INSTANCE;
    }

    public static boolean upload(String url,String content){
        Response response = null;
        Response mkResponse = null;
        try{
            mkResponse = OkWebDavUtil.mkRemoteDir(url);

            if (!mkResponse.isSuccessful()){
                throw new RuntimeException("创建文件夹失败");
            }

            final Request request = new Request.Builder()
                    .url(url)
                    .method("PUT",RequestBody.create(MediaType.get("application/x-www-form-urlencoded"),content.getBytes()))
                    .build();
            response = OkWebDavUtil.requestForResponse(request);
            if (!response.isSuccessful()){
                throw new RuntimeException("上传失败");
            }else {
                return true;
            }
        }catch (Exception e){
            Log.e("dav","request fail");
            throw new RuntimeException("上传失败");
        }finally {
            if (mkResponse != null) {
                mkResponse.close();
            }
            if (response != null) {
                response.close();
            }
        }
    }

    public static boolean isFileExist(String url) {
        final Request request = new Request.Builder()
                .url(url)
                .method("GET",null)
                .build();
        Call call = getClient().newCall(request);
        try (Response response = call.execute()) {
            return response.isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static void deleteFile(String url) throws IOException {
        final Request request = new Request.Builder()
                .url(url)
                .method("DELETE",null)
                .build();
        Call call = getClient().newCall(request);
        try (Response response = call.execute()) {
            if (!response.isSuccessful()){
                if (isFileExist(url)){
                    throw new RuntimeException("远程文件删除失败");
                }
            }
        }
    }

    private static class OkHttpClientHolder {
        static final OkHttpClient INSTANCE = new OkHttpClient.Builder()
                .connectTimeout(10000,TimeUnit.MILLISECONDS)
                .readTimeout(10000, TimeUnit.MILLISECONDS)
                .authenticator(new BasicAuthenticator("xxxx", "xxxx"))
                .build();
    }

    public static Response requestForResponse(Request request) throws IOException {
        Call call = getClient().newCall(request);
        return call.execute();
    }

    public static String requestForString(Request request,Callback<String,String> callback) throws IOException {
        return requestForString(request,getClient(),callback);
    }

    public static String requestForString(Request request,OkHttpClient okHttpClient,Callback<String,String> callback) throws IOException {
        String result = null;
        Call call = okHttpClient.newCall(request);
        try (Response response = call.execute()) {
            if (response.isSuccessful()){
                ResponseBody body = response.body();
                result = callback.onSuccess(body != null ? body.string() : null);
            }else {
                callback.onFail();
            }
        }
        return result;
    }

    public static Response mkRemoteDir(String url) throws IOException {
        //获取文件夹路径
        int index = StringUtils.lastIndexOf(url, "/");
        String parentUrl = StringUtils.substring(url, 0, index);

        Request.Builder requestBuilder = new Request.Builder();
        Request mkcol = requestBuilder
                .url(parentUrl)
                .method("MKCOL", null)
                .build();
        String root = "https://" + URI.create(url).getHost();
        if (!StringUtils.equalsIgnoreCase(parentUrl, root)) {
            mkRemoteDir(parentUrl);
        }
        return requestForResponse(mkcol);
    }

    public static String getContentFromResponse(Response response ) throws IOException {
        ResponseBody body = response.body();
        return body != null ? body.string() : null;
    }

    public interface Callback<S,T>{
        S onSuccess(T t);
        void onFail();
    }
}
