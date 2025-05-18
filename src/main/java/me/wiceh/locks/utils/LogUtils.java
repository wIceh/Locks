package me.wiceh.locks.utils;

import me.wiceh.locks.Locks;

import java.util.logging.Level;

public class LogUtils {

    public static void logError(Throwable t, String msg) {
        Locks.getInstance().getLogger().log(Level.SEVERE, msg, t);
    }

    public static void logInfo(String msg) {
        Locks.getInstance().getLogger().log(Level.INFO, msg);
    }
}
