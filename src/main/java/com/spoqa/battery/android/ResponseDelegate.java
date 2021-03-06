/**
 * Copyright (c) 2014-2015 Spoqa, All Rights Reserved.
 */

package com.spoqa.battery.android;

public class ResponseDelegate {
    private String mData;
    private String mContentType;

    public ResponseDelegate(String data, String contentType) {
        mData = data;
        mContentType = contentType;
    }

    public String data() {
        return mData;
    }

    public String contentType() {
        return mContentType;
    }
}
