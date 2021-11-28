package com.nanhuajiaren.cookieparse.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/*
 * The annotation for a deserialize method
 * @see See {@link CookieParser} for more detail.
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.METHOD})
public @interface DeserializeMethod {
    String value();
}
