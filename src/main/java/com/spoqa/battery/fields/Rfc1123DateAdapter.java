/**
 * Copyright (c) 2014-2015 Spoqa, All Rights Reserved.
 */

package com.spoqa.battery.fields;

import com.spoqa.battery.TypeAdapter;
import com.spoqa.battery.exceptions.DeserializationException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Rfc1123DateAdapter implements TypeAdapter<Date> {

    private SimpleDateFormat mDateFormat;
    private SimpleDateFormat mDateWithMicrosecFormat;

    public Rfc1123DateAdapter() {
        mDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        mDateWithMicrosecFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss.SSS z", Locale.US);
    }

    public Rfc1123DateAdapter(TimeZone timezone) {
        this();
        mDateFormat.setTimeZone(timezone);
        mDateWithMicrosecFormat.setTimeZone(timezone);
    }

    @Override
    public Class getType() {
        return Date.class;
    }

    @Override
    public Date decode(String s) throws DeserializationException {
        try {
            return mDateFormat.parse(s);
        } catch (ParseException e) {
            try {
                return mDateWithMicrosecFormat.parse(s);
            } catch (ParseException e2) {
                //return new Date();
                throw new DeserializationException(e2);
            }
        }
    }

    @Override
    public String encode(Date object) {
        return mDateFormat.format(object);
    }
}
