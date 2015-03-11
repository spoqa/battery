/**
 * Copyright (c) 2014 Park Joon-Kyu, All Rights Reserved.
 */

package com.spoqa.battery.annotations;

import com.spoqa.battery.HttpRequest;
import com.spoqa.battery.codecs.UrlEncodedFormEncoder;
import com.spoqa.battery.transformers.CamelCaseTransformer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify callee's HTTP request metadata
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RpcObject {
    static final class NULL {}

    public int method() default HttpRequest.Methods.GET;
    public String uri();
    public Class requestSerializer() default NULL.class;
    public Class localName() default NULL.class;
    public Class remoteName() default NULL.class;
    public Class context() default NULL.class;
    public String expectedContentType() default "";
}
