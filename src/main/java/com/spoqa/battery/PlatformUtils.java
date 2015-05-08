package com.spoqa.battery;

import java.io.File;

public final class PlatformUtils {

    private static final String TAG = "PlatformUtils";

    public static interface PlatformUtilsImpl {
        abstract String getMimeType(File path);
    }

    private static PlatformUtilsImpl sCurrentImpl;

    public static void registerPlatformUtils(PlatformUtilsImpl impl) {
        sCurrentImpl = impl;
    }

    static public void unregisterPlatformUtils() {
        sCurrentImpl = null;
    }

    static public String getMimeType(File path) {
        if (sCurrentImpl != null) {
            return sCurrentImpl.getMimeType(path);
        } else {
            Logger.warn(TAG, "No PlaformUtilsImpl installed");
            return null;
        }
    }
}
