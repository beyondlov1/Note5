package com.beyond.note5.utils;

import com.beyond.note5.sync.webdav.CommonTest;
import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class OkWebDavUtilTest {

    @Test
    public void mkRemoteDir() throws IOException, ExecutionException, InterruptedException {
        Sardine sardine = new OkHttpSardine();
        sardine.setCredentials(CommonTest.getUsername(),CommonTest.getPassword());

        ExecutorService executorService = CommonTest.getExecutorService();

        List<Future<Object>> futures = new ArrayList<>();
        for (int i = 0; i < 10; i++) {

            Future<Object> future = executorService.submit(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    OkWebDavUtil.mkRemoteDir(sardine, CommonTest.getRootUrl(), "/test/yes/");
                    return null;
                }
            });
            futures.add(future);
        }

        for (Future<Object> future : futures) {
            future.get();
        }
    }
}