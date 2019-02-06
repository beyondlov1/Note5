package com.beyond.note5.utils;

import com.beyond.note5.MyApplication;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * @author: beyond
 * @date: 2019/2/3
 */

public class ResourceUtil {
    public static String getAssetFile(String relativePath) throws IOException {
        InputStream inputStream = MyApplication.getInstance().getAssets().open(relativePath);
        return IOUtils.toString(inputStream, Charset.forName("UTF-8"));
    }
}
