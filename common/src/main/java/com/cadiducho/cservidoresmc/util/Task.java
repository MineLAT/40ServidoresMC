package com.cadiducho.cservidoresmc.util;

import java.util.concurrent.TimeUnit;

public class Task {

    private static Scheduler scheduler = null;

    Task() {
    }

    public static Scheduler getScheduler() {
        return scheduler;
    }

    public static void setScheduler(Scheduler scheduler) {
        if (Task.scheduler == null) {
            Task.scheduler = scheduler;
        }
    }

    public static void sync(Runnable runnable) {
        scheduler.sync(runnable);
    }

    public static void syncLater(Runnable runnable, long delay) {
        syncLater(runnable, delay, TimeUnit.SECONDS);
    }

    public static void syncLater(Runnable runnable, long delay, TimeUnit unit) {
        scheduler.syncLater(runnable, delay, unit);
    }

    public static void syncTimer(Runnable runnable, long delay, long period) {
        syncTimer(runnable, delay, period, TimeUnit.SECONDS);
    }

    public static void syncTimer(Runnable runnable, long delay, long period, TimeUnit unit) {
        scheduler.syncTimer(runnable, delay, period, unit);
    }

    public static void async(Runnable runnable) {
        scheduler.async(runnable);
    }

    public static void asyncLater(Runnable runnable, long delay) {
        asyncLater(runnable, delay, TimeUnit.SECONDS);
    }

    public static void asyncLater(Runnable runnable, long delay, TimeUnit unit) {
        scheduler.asyncLater(runnable, delay, unit);
    }

    public static void asyncTimer(Runnable runnable, long delay, long period) {
        asyncTimer(runnable, delay, period, TimeUnit.SECONDS);
    }

    public static void asyncTimer(Runnable runnable, long delay, long period, TimeUnit unit) {
        scheduler.asyncTimer(runnable, delay, period, unit);
    }

    public interface Scheduler {
        default void sync(Runnable runnable) {
            async(runnable);
        }

        default void syncLater(Runnable runnable, long delay, TimeUnit unit) {
            asyncLater(runnable, delay, unit);
        }

        default void syncTimer(Runnable runnable, long delay, long period, TimeUnit unit) {
            asyncTimer(runnable, delay, period, unit);
        }

        void async(Runnable runnable);

        void asyncLater(Runnable runnable, long delay, TimeUnit unit);

        void asyncTimer(Runnable runnable, long delay, long period, TimeUnit unit);
    }
}
