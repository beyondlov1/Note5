package com.beyond.note5.presenter;

import com.beyond.note5.utils.HtmlUtil;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author: beyond
 * @date: 2019/7/19
 */
public class NotePresenterImplTest {

    @Test
    public void getTitleFromUrlTest(){
        try {
            String title = getTitleFromUrl("https://www.liaoxuefeng.com/wiki/897692888725344/923057403198272");
            System.out.println(title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getTitleFromUrl(String url) throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();
        if (StringUtils.isBlank(url)) {
            return null;
        }

        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .addHeader("User-Agent", "PostmanRuntime/7.15.0")
                .build();
        Call call = okHttpClient.newCall(request);
        Response response = call.execute();
        if (!response.isSuccessful()) {
            return null;
        }
        if (response.body() == null) {
            return null;
        }
        String titleFromHtml = HtmlUtil.getTitleFromHtml(response.body().string());
        if (StringUtils.isNotBlank(titleFromHtml)) {
            return titleFromHtml;
        }
        return null;
    }

}