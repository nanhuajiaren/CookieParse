package com.nanhuajiaren.cookieparse.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
 * Use on a class that contains no serialize method or deserialize method.
 * Though these method themselves are marked through {@link SerializeMethod} and {@link DeserializeMethod}, this annotation will let the parser skip method check and speed up a little.
 * Or you can use it for safety concerns.
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.TYPE})
public @interface NoMethod {
}
