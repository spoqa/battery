/**
 * Copyright (c) 2014-2015 Spoqa, All Rights Reserved.
 */

package com.spoqa.battery;

public final class Logger {

    static public interface LoggerImpl {
        abstract void debug(String tag, String msg);
        abstract void error(String tag, String msg);
        abstract void info(String tag, String msg);
        abstract void verbose(String tag, String msg);
        abstract void warn(String tag, String msg);
    }

    static private LoggerImpl sCurrentLogger;

    static public void registerLogger(LoggerImpl logger) {
        sCurrentLogger = logger;
    }

    static public void unregisterLogger() {
        sCurrentLogger = null;
    }

    static public void debug(String tag, String msg) {
        if (sCurrentLogger != null)
            sCurrentLogger.debug(tag, msg);
    }

    static public void error(String tag, String msg) {
        if (sCurrentLogger != null)
            sCurrentLogger.error(tag, msg);
    }

    static public void info(String tag, String msg) {
        if (sCurrentLogger != null)
            sCurrentLogger.info(tag, msg);
    }

    static public void verbose(String tag, String msg) {
        if (sCurrentLogger != null)
            sCurrentLogger.verbose(tag, msg);
    }

    static public void warn(String tag, String msg) {
        if (sCurrentLogger != null)
            sCurrentLogger.warn(tag, msg);
    }
}
