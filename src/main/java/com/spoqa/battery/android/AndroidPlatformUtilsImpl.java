package com.spoqa.battery.android;

import android.webkit.MimeTypeMap;

import com.spoqa.battery.PlatformUtils;

import java.io.File;

public class AndroidPlatformUtilsImpl implements PlatformUtils.PlatformUtilsImpl {
    @Override
    public String getMimeType(File path) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(path.getAbsolutePath());
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }
        return type;
    }
}
