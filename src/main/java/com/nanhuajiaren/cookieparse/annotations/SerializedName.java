package com.nanhuajiaren.cookieparse.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
 * Change the name used in cookie parse.
 * Likes the annotation with same name in Gson.
 * Note that using both in a same file will cause confuse.
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.FIELD})
public @interface SerializedName{
    String value();
}
