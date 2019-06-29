package com.beyond.note5.sync.datasource.impl;

import com.beyond.note5.sync.webdav.CommonTest;
import com.beyond.note5.sync.webdav.client.DavClient;
import com.beyond.note5.utils.OkWebDavUtil;

import org.junit.Test;

import java.io.IOException;

public class DefaultDavFileSourceTest {

    private DavClient client = CommonTest.getClient();

    @Test
    public void upload() throws IOException {
        client.upload("/home/beyond/桌面/油猴插件 脚本.7z",OkWebDavUtil.concat(CommonTest.getRootUrl(),"files/test.zip"));
    }

    @Test
    public void download() throws IOException {
        client.download(OkWebDavUtil.concat(CommonTest.getRootUrl(),"files/test.zip"),"/home/beyond/桌面/test.zip");
    }
}