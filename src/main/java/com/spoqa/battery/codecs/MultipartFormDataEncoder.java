/**
 * Copyright (c) 2014-2015 Spoqa, All Rights Reserved.
 */

package com.spoqa.battery.codecs;

import com.spoqa.battery.CodecUtils;
import com.spoqa.battery.FieldNameTranslator;
import com.spoqa.battery.Logger;
import com.spoqa.battery.PlatformUtils;
import com.spoqa.battery.RequestSerializer;
import com.spoqa.battery.annotations.RequestBody;
import com.spoqa.battery.exceptions.SerializationException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

public class MultipartFormDataEncoder implements RequestSerializer {

    private static final String MIME_TYPE = "multipart/form-data";
    private static final String TAG = "MultipartFormDataEncoder";

    private String mBoundary;
    private ByteArrayOutputStream mOutputStream;

    public MultipartFormDataEncoder() {
        mBoundary = String.format("----BatteryMultipart%1$s", UUID.randomUUID().toString());
    }

    @Override
    public byte[] serializeObject(Object o, FieldNameTranslator translator) throws SerializationException {
        mOutputStream = new ByteArrayOutputStream();

        List<Field> fields = CodecUtils.getAnnotatedFields(null, RequestBody.class, o.getClass());
        for (Field f : fields) {
            RequestBody annotation = f.getAnnotation(RequestBody.class);
            Class type = f.getType();
            String localName = f.getName();
            String foreignName;
            if (annotation.value().length() > 0)
                foreignName = annotation.value();
            else
                foreignName = translator.localToRemote(localName);
            String value = "";

            try {
                Object element = f.get(o);

                if (element == null) {
                    continue;
                } else if (CodecUtils.isString(type)) {
                    addPart(foreignName, (String) element);
                } else if (CodecUtils.isFloat(type)) {
                    addPart(foreignName, Float.toString((Float) element));
                } else if (CodecUtils.isDouble(type)) {
                    addPart(foreignName, Double.toString((Double) element));
                } else if (CodecUtils.isBoolean(type)) {
                    addPart(foreignName, Boolean.toString((Boolean) element));
                } else if (CodecUtils.isInteger(type)) {
                    addPart(foreignName, Integer.toString((Integer) element));
                } else if (CodecUtils.isLong(type)) {
                    addPart(foreignName, Long.toString((Long) element));
                } else if (type.isEnum()) {
                    addPart(foreignName, element.toString());
                } else if (CodecUtils.isList(type)) {
                    int i = 0;
                    for (Object innerElement : (List<Object>) element) {
                        String nameWithIndex = String.format("%1$s[%2$d]", foreignName, i++);

                        if (innerElement instanceof File) {
                            File file = (File) innerElement;
                            try {
                                addPart(nameWithIndex, new FileInputStream(file), file.getAbsolutePath());
                            } catch (FileNotFoundException e) {
                                Logger.warn(TAG, String.format("Could not find file %1$s", file.getAbsolutePath()));
                            }
                        } else if (innerElement instanceof InputStream) {
                            addPart(nameWithIndex, (InputStream) innerElement, null);
                        } else {
                            addPart(nameWithIndex, innerElement.toString());
                        }
                    }
                } else if (element instanceof InputStream) {
                    addPart(foreignName, (InputStream) element, null);
                } else if (element instanceof File) {
                    try {
                        File file = (File) element;
                        addPart(foreignName, new FileInputStream(file), file.getName());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Logger.warn(TAG, String.format("Field %1$s is not serializable: %2$s", type.getName(), e.toString()));
                    }
                } else {
                    Logger.warn(TAG, String.format("Field %1$s is not serializable", type.getName()));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                continue;
            }
        }

        try {
            String end = String.format("--%1$s--\r\n", mBoundary);
            mOutputStream.write(end.getBytes("utf-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mOutputStream.toByteArray();
    }

    private void addPart(String fieldName, String formData) {
        String header = "--%1$s\r\nContent-Disposition: form-data; name=\"%2$s\"\r\n\r\n";

        try {
            mOutputStream.write(String.format(header, mBoundary, fieldName).getBytes("utf-8"));
            mOutputStream.write(formData.getBytes("utf-8"));
            mOutputStream.write("\r\n\r\n".getBytes("utf-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addPart(String fieldName, InputStream stream, String fileName) {
        byte[] buffer = new byte[16384];

        if (fileName == null)
            fileName = "";

        String mimeType = null;
        if (fileName.length() > 0)
            mimeType = PlatformUtils.getMimeType(new File(fileName));

        if (mimeType == null)
            mimeType = "application/octet-stream";

        String header;
        if (fileName.length() > 0) {
            header = String.format("--%1$s\r\nContent-Disposition: form-data; name=\"%2$s\"; filename=\"%3$s\"\r\n" +
                    "Content-Type: %4$s\r\n\r\n", mBoundary, fieldName, fileName, mimeType);
        } else {
            header = String.format("--%1$s\r\nContent-Disposition: form-data; name=\"%2$s\"\r\n" +
                    "Content-Type: %3$s\r\n\r\n", mBoundary, fieldName, mimeType);
        }

        try {
            mOutputStream.write(header.getBytes("utf-8"));
            int read;
            while ((read = stream.read(buffer)) > 0)
                mOutputStream.write(buffer, 0, read);
            mOutputStream.write("\r\n\r\n".getBytes("utf-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String serializationContentType() {
        return String.format("%1$s; boundary=%2$s; charset=utf-8", MIME_TYPE, mBoundary);
    }

    @Override
    public boolean supportsCompositeType() {
        return false;
    }
}
