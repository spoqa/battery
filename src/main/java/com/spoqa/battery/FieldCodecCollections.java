package com.spoqa.battery;

import com.spoqa.battery.fields.Iso8601DateCodec;

import java.util.HashMap;
import java.util.Map;

public final class FieldCodecCollections {

    private static Map<Class, FieldCodec> sFieldCodecs;

    static {
        sFieldCodecs = new HashMap<Class, FieldCodec>();

        register(new Iso8601DateCodec());
    }

    public static void register(FieldCodec codec) {
        sFieldCodecs.put(codec.getType(), codec);
    }

    public static FieldCodec query(Class type) {
        if (!sFieldCodecs.containsKey(type))
            return null;

        return sFieldCodecs.get(type);
    }

    public static boolean contains(Class type) {
        return sFieldCodecs.containsKey(type);
    }
}
