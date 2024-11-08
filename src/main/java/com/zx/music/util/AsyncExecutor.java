package com.zx.music.util;

import cn.hutool.core.thread.RejectPolicy;

import java.util.concurrent.*;
import java.util.function.Supplier;

public class AsyncExecutor {

    public static final ExecutorService EXECUTOR = new ThreadPoolExecutor(12, 12,
            60, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(128), RejectPolicy.BLOCK.getValue());

    public static void execute(Runnable runnable) {
        EXECUTOR.execute(runnable);
    }
    public static <T> CompletableFuture<T> supply(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, EXECUTOR);
    }
}
