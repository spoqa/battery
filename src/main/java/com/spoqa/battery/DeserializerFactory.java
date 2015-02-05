/**
 * Copyright (c) 2014 Park Joon-Kyu, All Rights Reserved.
 */

package com.spoqa.battery;

import com.spoqa.battery.codecs.JsonCodec;
import com.spoqa.battery.exceptions.DeserializationException;
import com.spoqa.battery.exceptions.RpcException;

import java.util.HashMap;
import java.util.Map;

public final class DeserializerFactory {
    private static final String TAG = "DeserializerFactory";

    private static Map<String, ResponseDeserializer> sDeserializerMap;

    static {
        sDeserializerMap = new HashMap<String, ResponseDeserializer>();
        registerDeserializer(new JsonCodec());
    }

    private static void registerDeserializer(ResponseDeserializer deserializer) {
        sDeserializerMap.put(deserializer.deserializationContentType(), deserializer);
    }

    public static void deserialize(String mime, String input, Object object,
                                   FieldNameTransformer transformer) throws DeserializationException {
        if (Config.DEBUG_DUMP_RESPONSE) {
            Logger.debug(TAG, "Mime: " + mime);
            Logger.debug(TAG, "Response: " + input);
        }

        if (!sDeserializerMap.containsKey(mime)) {
            RpcException e = new RpcException(String.format("No deserializer associated with MIME type %1$s", mime));
            throw new DeserializationException(e);
        }

        sDeserializerMap.get(mime).deserializeObject(input, object, transformer);
    }

}
