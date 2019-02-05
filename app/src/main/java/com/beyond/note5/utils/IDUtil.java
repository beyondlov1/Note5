package com.beyond.note5.utils;

import java.util.UUID;

/**
 * Created by beyond on 2019/2/1.
 */

public class IDUtil {
    public static String uuid(){
        return UUID.randomUUID().toString().replace("-", "");
    }
}
