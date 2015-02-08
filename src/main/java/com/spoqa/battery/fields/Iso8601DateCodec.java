package com.spoqa.battery.fields;

import com.spoqa.battery.FieldCodec;
import com.spoqa.battery.exceptions.DeserializationException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Iso8601DateCodec implements FieldCodec<Date> {

    private SimpleDateFormat mDateTimeFormatWithMsec;
    private SimpleDateFormat mDateTimeFormat;
    private SimpleDateFormat mDateFormat;

    public Iso8601DateCodec() {
        mDateTimeFormatWithMsec = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ", Locale.getDefault());
        mDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault());
        mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    }

    @Override
    public Class getType() {
        return Date.class;
    }

    @Override
    public Date decode(String s) throws DeserializationException {
        s = s.replaceAll(":(\\d\\d)$", "$1");

        try {
            return mDateTimeFormatWithMsec.parse(s);
        } catch (ParseException e) {
            try {
                return mDateTimeFormat.parse(s);
            } catch (ParseException e1) {
                try {
                    return mDateFormat.parse(s);
                } catch (ParseException e2) {
                    throw new DeserializationException(e);
                }
            }
        }
    }

    @Override
    public String encode(Date object) {
        if (object.getHours() == 0 && object.getMinutes() == 0 && object.getSeconds() == 0)
            return mDateFormat.format(object);
        else
            return mDateTimeFormatWithMsec.format(object);
    }
}
