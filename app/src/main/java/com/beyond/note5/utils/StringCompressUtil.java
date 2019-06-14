package com.beyond.note5.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class StringCompressUtil {
    public static String compress(String str){
        if (str == null||str.length() == 0){
            return str;
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(out)){
            gzipOutputStream.write(str.getBytes());
            gzipOutputStream.finish();  // important！！！！！
            return out.toString("ISO-8859-1");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }

    public  static String unCompress(String str){
        if (str == null||str.length() == 0){
            return str;
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes("ISO-8859-1"));
             GZIPInputStream gzipIn = new GZIPInputStream(in) ){

            byte[] bytes = new byte[1024];
            int len;
            while ((len = gzipIn.read(bytes))>=0){
                out.write(bytes,0,len);
            }
            return out.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }
}
