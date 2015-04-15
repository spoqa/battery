package com.spoqa.battery.codecs;

import com.spoqa.battery.CodecUtils;
import com.spoqa.battery.FieldNameTranslator;
import com.spoqa.battery.Logger;
import com.spoqa.battery.RequestSerializer;
import com.spoqa.battery.annotations.RequestBody;
import com.spoqa.battery.exceptions.SerializationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.List;

public class UrlEncodedFormEncoder implements RequestSerializer {
    private static final String TAG = "UrlEncodedFormEncoder";

    private static final String MIME_TYPE = "application/x-www-form-urlencoded; charset=utf-8";

    public UrlEncodedFormEncoder() {

    }

    @Override
    public byte[] serializeObject(Object o, FieldNameTranslator translator) throws SerializationException {
        StringBuilder sb = new StringBuilder();

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
                    append(sb, foreignName, (String) element);
                } else if (CodecUtils.isFloat(type)) {
                    append(sb, foreignName, Float.toString((Float) element));
                } else if (CodecUtils.isDouble(type)) {
                    append(sb, foreignName, Double.toString((Double) element));
                } else if (CodecUtils.isBoolean(type)) {
                    append(sb, foreignName, Boolean.toString((Boolean) element));
                } else if (CodecUtils.isInteger(type)) {
                    append(sb, foreignName, Integer.toString((Integer) element));
                } else if (CodecUtils.isLong(type)) {
                    append(sb, foreignName, Long.toString((Long) element));
                } else if (type.isEnum()) {
                    append(sb, foreignName, element.toString());
                } else if (CodecUtils.isList(type)) {
                    for (Object innerElement : (List<Object>) element)
                        append(sb, foreignName, innerElement.toString());
                } else if (element instanceof InputStream) {
                    Logger.warn(TAG, "Could not attach byte stream");
                } else {
                    Logger.warn(TAG, String.format("Field %1$s is not serializable", type.getName()));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                continue;
            }
        }

        try {
            return sb.toString().getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    private void append(StringBuilder sb, String key, String value) {
        if (sb.length() != 0)
            sb.append('&');

        try {
            sb.append(URLEncoder.encode(key, "utf-8"));
            sb.append('=');
            sb.append(URLEncoder.encode(value, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String serializationContentType() {
        return MIME_TYPE;
    }

    @Override
    public boolean supportsCompositeType() {
        return false;
    }
}
