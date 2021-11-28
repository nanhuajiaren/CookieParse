package com.nanhuajiaren.cookieparse;

import org.jetbrains.annotations.NotNull;

/*
 * The interface that can be used to change the default escape behavior.
 */
public interface TextEncoder {
    /*
     * Used to encode the text into a safe text for cookie.
     * Named after the javascript method in front end.
     * @param original The original text.
     * @return The value to use in cookie.
     */
    String escape(@NotNull String original);
    /*
     * Used to decode the value in a cookie string.
     * Named after the javascript method in front end.
     * @param The cookie value.
     * @return Processed text.
     */
    String unescape(@NotNull String encoded);
}
