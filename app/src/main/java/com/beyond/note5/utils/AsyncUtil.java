package com.beyond.note5.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class AsyncUtil {

    public static <S> List<S> computeAllAsyn(ExecutorService executorService, List<Callable<List<S>>> callables) throws ExecutionException, InterruptedException {
        List<S> result = new ArrayList<>();

        List<Future<List<S>>> futures = new ArrayList<>();

        for (Callable<List<S>> callable : callables) {
            Future<List<S>> future = executorService.submit(callable);
            futures.add(future);
        }

        for (Future<List<S>> future : futures) {
            List<S> list;
            list = future.get();
            if (list != null) {
                result.addAll(list);
            }
        }
        return result;
    }

    public static <S> List<S> computeAsyn(ExecutorService executorService, List<Callable<S>> callables) throws ExecutionException, InterruptedException {
        List<S> result = new ArrayList<>();

        List<Future<S>> futures = new ArrayList<>();

        for (Callable<S> callable : callables) {
            Future<S> future = executorService.submit(callable);
            futures.add(future);
        }

        for (Future<S> future : futures) {
            S s = future.get();
            if (s != null) {
                result.add(s);
            }
        }
        return result;
    }
}
