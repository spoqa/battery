/**
 * Copyright (c) 2014-2015 Spoqa, All Rights Reserved.
 */

package com.spoqa.battery;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpRequest {
    static final private String TAG = "HttpRequest";

    public static final class Methods {
        public static final int GET = 1;
        public static final int POST = 2;
        public static final int PUT = 3;
        public static final int DELETE = 4;
    }

    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";

    private int mMethod;
    private String mUri;
    private Map<String, String> mHeaders;
    private Map<String, Object> mParams;
    private byte[] mRequestBody;
    private FieldNameTranslator mFieldNameTranslator;
    private Object mRequestObject;

    public HttpRequest(String uri) {
        mMethod = Methods.GET;
        mUri = uri;
        mHeaders = new HashMap<String, String>();
        mParams = new HashMap<String, Object>();
    }

    public HttpRequest(int method, String uri) {
        mMethod = method;
        mUri = uri;
        mHeaders = new HashMap<String, String>();
        mParams = new HashMap<String, Object>();
    }

    public void setRequestBody(byte[] body) {
        mRequestBody = body;
    }

    public void setRequestBody(InputStream inputStream) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[8192];

        try {
            while ((nRead = inputStream.read(data, 0, data.length)) != 1)
                buffer.write(data, 0, nRead);
            buffer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mRequestBody = buffer.toByteArray();
    }

    public void setRequestObject(Object requestObject) {
        mRequestObject = requestObject;
    }

    public void putHeader(String key, String value) {
        mHeaders.put(key, value);
    }

    public void putParameter(String key, Object value) {
        mParams.put(key, value);
    }

    public void putParameters(Map<String, Object> params) {
        for (String key : params.keySet())
            mParams.put(key, params.get(key));
    }

    public void removeHeader(String key) {
        mHeaders.remove(key);
    }

    public void removeParameter(String key) {
        if (mParams.containsKey(key))
            mParams.remove(key);
    }

    public void setNameTranslator(FieldNameTranslator fieldNameTranslator) {
        mFieldNameTranslator = fieldNameTranslator;
    }

    public byte[] getRequestBody() {
        return mRequestBody;
    }

    public Map<String, String> getHeaders() {
        return mHeaders;
    }

    public int getMethod() {
        return mMethod;
    }

    public FieldNameTranslator getFieldNameTranslator() {
        return mFieldNameTranslator;
    }

    public String getUri() {
        StringBuilder sb = new StringBuilder();
        sb.append(mUri);

        char delimiter;
        if (mUri.contains("?"))
            delimiter = '&';
        else
            delimiter = '?';

        for (String key : mParams.keySet()) {
            Object value = mParams.get(key);

            if (value instanceof List && value != null) {
                for (Object innerValue : (List<Object>) value) {
                    if (appendQueryString(sb, delimiter, key, innerValue))
                        delimiter = '&';
                }
            } else {
                if (appendQueryString(sb, delimiter, key, value))
                    delimiter = '&';
            }
        }

        String output = sb.toString();
        Logger.debug(TAG, "built uri: " + output);

        return output;
    }

    private boolean appendQueryString(StringBuilder sb, char delimiter, String key, Object value) {
        if (value == null)
            return false;

        try {
            sb.append(String.format("%1$c%2$s=%3$s", delimiter, key,
                    URLEncoder.encode(value.toString(), "utf-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public Object getRequestObject() {
        return mRequestObject;
    }

}
