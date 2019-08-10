package com.beyond.note5.sync;

import android.util.Base64;

import com.beyond.note5.sync.exception.SaveException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class SyncUtils {

    public static String base64Encode(String source) {
        return Base64.encodeToString(source.getBytes(), Base64.DEFAULT);
    }

    public static String base64Decode(String source) {
        return new String(Base64.decode(source, Base64.DEFAULT));
    }

    public static String urlEncode(String source) {
        try {
            return URLEncoder.encode(source, "utf-8");
        } catch (UnsupportedEncodingException e) {
            return source;
        }
    }

    public static String urlDecode(String source) {
        try {
            return URLDecoder.decode(source, "utf-8");
        } catch (UnsupportedEncodingException e) {
            return source;
        }
    }

    @SuppressWarnings("unchecked")
    public static <V> void blockExecute(ExecutorService executorService,
                                        final ParamCallable<V, Void> callable,
                                        ParamCallable<V, Void> exceptionHandler,
                                        List<V> targetList) {
        blockExecute(executorService, callable, null, exceptionHandler, targetList);
    }

    @SuppressWarnings("unchecked")
    public static <V, S> void blockExecute(ExecutorService executorService,
                                           final ParamCallable<V, S> callable,
                                           Handler<V, S> handler,
                                           ParamCallable<V, Void> exceptionHandler,
                                           List<V> targetList) {
        List<Future<S>> futures = new ArrayList<>();

        for (final V object : targetList) {
            Future<S> future = executorService.submit(new Callable<S>() {
                public S call() throws Exception {
                    try {
                        return callable.call(object);
                    } catch (Exception e) {
                        BlockExecuteException exception = new BlockExecuteException();
                        exception.setObject(object);
                        exception.initCause(e);
                        throw exception;
                    }
                }
            });
            futures.add(future);
        }
        int index = 0;
        for (Future<S> future : futures) {
            try {
                S result = future.get();
                if (handler != null) {
                    handler.handle(targetList.get(index), result);
                }
            } catch (Exception e) {
                if (exceptionHandler == null) {
                    e.printStackTrace();
                    return;
                }
                if (e.getCause() instanceof BlockExecuteException) {
                    try {
                        exceptionHandler.call((V) ((BlockExecuteException) (e.getCause())).getObject());
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            } finally {
                index++;
            }
        }
    }

    public interface ParamCallable<V, S> {
        S call(V singleExecutor) throws Exception;
    }

    public interface Handler<V, S> {
        void handle(V param, S result) throws IOException, SaveException;
    }

    public static class BlockExecuteException extends Exception {

        private Object object;

        public Object getObject() {
            return object;
        }

        public void setObject(Object object) {
            this.object = object;
        }
    }

}
