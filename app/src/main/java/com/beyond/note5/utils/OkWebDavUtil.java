package com.beyond.note5.utils;

import android.util.Log;

import com.beyond.note5.sync.webdav.BasicAuthenticator;
import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.SardineException;
import com.thegrizzlylabs.sardineandroid.impl.handler.OkHttpSardine2;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static com.beyond.note5.view.LoginActivity.DAV_LOGIN_PASSWORD;
import static com.beyond.note5.view.LoginActivity.DAV_LOGIN_USERNAME;

public class OkWebDavUtil {

    private final static Map<String,Boolean> IS_DIR_EXIST = new ConcurrentHashMap<>();

    public static OkHttpClient getClient() {
        return OkHttpClientHolder.INSTANCE;
    }

    public static List<String> listAllFile(Sardine sardine, String rootUrl) {
        return null;
    }

    private static class OkHttpClientHolder {
        static final OkHttpClient INSTANCE = new OkHttpClient.Builder()
                .connectTimeout(10000, TimeUnit.MILLISECONDS)
                .readTimeout(10000, TimeUnit.MILLISECONDS)
                .authenticator(new BasicAuthenticator(PreferenceUtil.getString(DAV_LOGIN_USERNAME),
                        PreferenceUtil.getString(DAV_LOGIN_PASSWORD)))
//                .authenticator(new BasicAuthenticator("806784568@qq.com",""))
                .build();
    }

    public static boolean upload(String url, String content) {
        return upload(getClient(), url, content);
    }

    public static boolean upload(OkHttpClient client, String url, String content) {
        Response response = null;
        Response mkResponse = null;
        try {
            mkResponse = OkWebDavUtil.mkRemoteDir(client, url);

            if (!mkResponse.isSuccessful()) {
                throw new RuntimeException("创建文件夹失败");
            }

            final Request request = new Request.Builder()
                    .url(url)
                    .method("PUT", RequestBody.create(MediaType.get("application/x-www-form-urlencoded"), content.getBytes()))
                    .build();
            response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new RuntimeException("上传失败");
            } else {
                return true;
            }
        } catch (Exception e) {
            Log.e("dav", "request fail");
            throw new RuntimeException("上传失败");
        } finally {
            if (mkResponse != null) {
                mkResponse.close();
            }
            if (response != null) {
                response.close();
            }
        }
    }

    public static String download(String url) {
        Request.Builder requestBuilder = new Request.Builder();
        Request get = requestBuilder
                .url(url)
                .method("GET", null)
                .build();
        try (Response response = getClient().newCall(get).execute()) {
            if (response.isSuccessful()) {
                assert response.body() != null;
                return response.body().string();
            }
        } catch (IOException e) {
            throw new RuntimeException("获取远程文件失败", e);
        }
        return null;
    }

    public static String download(OkHttpClient client, String url) {
        Request.Builder requestBuilder = new Request.Builder();
        Request get = requestBuilder
                .url(url)
                .method("GET", null)
                .build();
        try (Response response = client.newCall(get).execute()) {
            if (response.isSuccessful()) {
                assert response.body() != null;
                return response.body().string();
            }
        } catch (IOException e) {
            throw new RuntimeException("获取远程文件失败", e);
        }
        return null;
    }

    public static boolean isFileExist(String url) {
        return isFileExist(getClient(), url);
    }

    public static boolean isFileExist(OkHttpClient client, String url) {
        final Request request = new Request.Builder()
                .url(url)
                .method("GET", null)
                .build();
        Call call = client.newCall(request);
        try (Response response = call.execute()) {
            return response.isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void deleteFile(String url) throws IOException {
        deleteFile(getClient(), url);
    }

    public static void deleteFile(OkHttpClient client, String url) throws IOException {
        final Request request = new Request.Builder()
                .url(url)
                .method("DELETE", null)
                .build();
        Call call = client.newCall(request);
        try (Response response = call.execute()) {
            if (!response.isSuccessful()) {
                if (isFileExist(url)) {
                    throw new RuntimeException("远程文件删除失败");
                }
            }
        }
    }

    public static Response requestForResponse(Request request) throws IOException {
        return requestForResponse(getClient(), request);
    }

    public static Response requestForResponse(OkHttpClient client, Request request) throws IOException {
        Call call = client.newCall(request);
        return call.execute();
    }

    public static String requestForString(Request request, Callback<String, String> callback) throws IOException {
        return requestForString(getClient(), request, callback);
    }

    public static String requestForString(OkHttpClient okHttpClient, Request request, Callback<String, String> callback) throws IOException {
        String result = null;
        Call call = okHttpClient.newCall(request);
        try (Response response = call.execute()) {
            if (response.isSuccessful()) {
                ResponseBody body = response.body();
                result = callback.onSuccess(body != null ? body.string() : null);
            } else {
                callback.onFail();
            }
        }
        return result;
    }

    public static Response mkRemoteDir(String url) throws IOException {
        return mkRemoteDir(getClient(), url);
    }

    public static Response mkRemoteDir(OkHttpClient client, String url) throws IOException {
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
            Response response = mkRemoteDir(client, parentUrl);
            response.close();
        }
        return requestForResponse(client, mkcol);
    }

    public static void mkRemoteDir(Sardine client, String root, String path) throws IOException {
        mkRemoteDir(client,concat(root,path));
    }

    /**
     * dirUrl要以/结尾
     * @param client
     * @param dirUrl
     * @throws IOException
     */
    public static void mkRemoteDir(Sardine client, String dirUrl) throws IOException {
        if (IS_DIR_EXIST.get(dirUrl)!=null&& IS_DIR_EXIST.get(dirUrl)){
            return;
        }
        //获取文件夹路径
        String parentUrl = StringUtils.substringBeforeLast(dirUrl, "/");
        String root = "https://" + URI.create(dirUrl).getHost();
        if (!OkWebDavUtil.urlEquals(parentUrl,root)) {
             mkRemoteDir(client, parentUrl);
        }
        try {
            client.createDirectory(dirUrl);
            IS_DIR_EXIST.put(dirUrl,true);
        }catch (SardineException e){
            //ignore
        }finally {
            if (client instanceof OkHttpSardine2){
                ((OkHttpSardine2) client).closeCurrentResponse();
            }
        }
    }

    private static boolean urlEquals(String url1, String url2) {
        return  StringUtils.equalsIgnoreCase(
                url1.replace("/", ""),
                url2.replace("/", ""));
    }

    public static boolean isAvailable(String url) {
        OkHttpClient client = getClient();
        //获取文件夹路径
        int index = StringUtils.lastIndexOf(url, "/");
        String parentUrl = StringUtils.substring(url, 0, index);

        Request.Builder requestBuilder = new Request.Builder();
        Request mkcol = requestBuilder
                .url(parentUrl)
                .method("MKCOL", null)
                .build();

        try (Response response = client.newCall(mkcol).execute()) {
            return response.isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isAvailable(String url, String username, String password) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10000, TimeUnit.MILLISECONDS)
                .readTimeout(10000, TimeUnit.MILLISECONDS)
                .authenticator(new BasicAuthenticator(username,
                        password))
                .build();
        //获取文件夹路径
        int index = StringUtils.lastIndexOf(url, "/");
        String parentUrl = StringUtils.substring(url, 0, index);

        Request.Builder requestBuilder = new Request.Builder();
        Request mkcol = requestBuilder
                .url(parentUrl)
                .method("MKCOL", null)
                .build();

        try (Response response = client.newCall(mkcol).execute()) {
            return response.isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String concat(String root, String path) {
        String url = null;
        if ((!root.endsWith("/") && path.startsWith("/"))
                || root.endsWith("/") && !path.startsWith("/")) {
            url = root + path;
        } else if (root.endsWith("/") && path.startsWith("/")) {
            url = StringUtils.substringBeforeLast(root, "/") + path;
        } else if (!root.endsWith("/") && !path.startsWith("/")) {
            url = root + "/" + path;
        }
        return url;
    }

    public interface Callback<S, T> {
        S onSuccess(T t);

        void onFail();
    }

    public static void main(String[] args) throws IOException {
        String url = "https://dav.jianguoyun.com/dav/NoteCloud2/test.txt";
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10000, TimeUnit.MILLISECONDS)
                .readTimeout(10000, TimeUnit.MILLISECONDS)
                .authenticator(new BasicAuthenticator("",
                        ""))
                .build();
        upload(client, url, "我们a");
    }
}
