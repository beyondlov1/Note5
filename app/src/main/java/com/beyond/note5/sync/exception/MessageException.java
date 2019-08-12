package com.beyond.note5.sync.exception;

import com.thegrizzlylabs.sardineandroid.impl.SardineException;

import org.apache.commons.lang3.StringUtils;

import java.net.UnknownHostException;

/**
 * @author: beyond
 * @date: 2019/7/28
 */

public class MessageException extends Exception {
    private Exception exception;

    private String msg;

    public MessageException(String msg) {
        this.msg = msg;
    }
    public MessageException(Exception exception) {
        this.exception = exception;
    }

    @Override
    public String getMessage() {
        if (StringUtils.isNotBlank(msg)){
            return msg;
        }
        if (exception instanceof SaveException){
            return "保存失败["+((SaveException) exception).getKey()+"]";
        }
        if (exception instanceof ConnectException){
            return "连接失败["+((ConnectException) exception).getKey()+"]";
        }
        if (exception instanceof SardineException){
            if (((SardineException) exception).getStatusCode() == 503){
                return "服务器错误";
            }
        }
        if (exception instanceof UnknownHostException){
            return "网络错误";
        }
        return "未知错误";
    }
}
