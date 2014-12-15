package com.spoqa.battery.codecs;

import com.spoqa.battery.CodecUtils;
import com.spoqa.battery.FieldNameTransformer;
import com.spoqa.battery.Logger;
import com.spoqa.battery.RequestSerializer;
import com.spoqa.battery.annotations.RequestBody;
import com.spoqa.battery.exceptions.SerializationException;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

public class UrlEncodedFormEncoder implements RequestSerializer {
    private static final String TAG = "UrlEncodedFormEncoder";

    private static final String MIME_TYPE = "application/x-www-form-urlencoded; charset=utf-8";

    public UrlEncodedFormEncoder() {

    }

    @Override
    public byte[] serializeObject(Object o, FieldNameTransformer transformer) throws SerializationException {
        StringBuilder sb = new StringBuilder();

        List<Field> fields = CodecUtils.getAnnotatedFields(RequestBody.class, o.getClass());
        boolean first = true;
        for (Field f : fields) {
            RequestBody annotation = f.getAnnotation(RequestBody.class);
            Class type = f.getType();
            String localName = f.getName();
            String foreignName;
            if (annotation.fieldName().length() > 0)
                foreignName = annotation.fieldName();
            else
                foreignName = transformer.localToRemote(localName);
            String value = "";

            try {
                Object element = f.get(o);

                if (CodecUtils.isString(type))
                    value = (String) element;
                else if (CodecUtils.isFloat(type))
                    value = ((Float) element).toString();
                else if (CodecUtils.isDouble(type))
                    value = ((Double) element).toString();
                else if (CodecUtils.isBoolean(type))
                    value = ((Boolean) element).toString();
                else if (CodecUtils.isDateTime(type))
                    value = CodecUtils.toIso8601((Date) element);
                else if (CodecUtils.isInteger(type))
                    value = ((Integer) element).toString();
                else if (CodecUtils.isLong(type))
                    value = ((Long) element).toString();
                else
                    Logger.warn(TAG, String.format("Field %1$s is not serializable", type.getName()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                continue;
            }

            try {
                if (!first)
                    sb.append('&');

                first = false;

                sb.append(URLEncoder.encode(foreignName, "utf-8"));
                sb.append('=');
                sb.append(URLEncoder.encode(value, "utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        try {
            return sb.toString().getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    @Override
    public String serializationContentType() {
        return MIME_TYPE;
    }
}
