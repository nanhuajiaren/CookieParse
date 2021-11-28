package com.nanhuajiaren.cookieparse;

import com.nanhuajiaren.cookieparse.annotations.DeserializeMethod;
import com.nanhuajiaren.cookieparse.annotations.NoMethod;
import com.nanhuajiaren.cookieparse.annotations.SerializeMethod;
import com.nanhuajiaren.cookieparse.annotations.SerializedName;
import com.nanhuajiaren.cookieparse.annotations.NoSerializeOrDeserialize;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Parse or make a cookie string.
 * @author nanhuajiaren
 */
public class CookieParser{
    private static final String SEPARATOR = ";";
    private static final String OPERATOR = "=";
    private TextEncoder encoder = new DefaultTextEncoder();

    public CookieParser(){}

    /*
     * Parse a cookie string.
     * To write the Class {@code classOfT},use code like Gson.
     * If a certain field requires a name different from its field name, use {@link SerializedName}.
     * if a certain field shouldn't be used, use {@link NoSerializeOrDeserialize}.
     * If a certain field requires special serialize way, use {@link SerializeMethod} on a method.
     *
     * If serializing is completed through a method, then:
     * 1. The method should require and only require a {@code String} parameter.
     * 2. The method should not be static.
     * 3. Use {@code @SerializeMethod("serializeName")} to specify the key name.
     * 4. The return value of the method will be ignored.
     * 5. A {@code @NoSerializeOrDeserialize} is recommended on the real field.
     *
     * @param cookie The cookie to parse (must contain at least on key-value pair).
     * @param classOfT The type to fit into.
     * @return A certain object,in type of given parameter {@code classOfT}.
     * @exception CookieSyntaxException When {@code cookie} is null, empty or contains invalid syntax.
     * @exception CookieReflectException When a reflection exception is thrown.
     */
    public <T> T fromCookie(@NotNull String cookie,@NotNull Class<T> classOfT) throws CookieSyntaxException,CookieReflectException{
        boolean noMethod = classOfT.isAnnotationPresent(NoMethod.class);
        Constructor<T> constructor;
        try {
            constructor = classOfT.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new CookieReflectException(
                    "Can't find a constructor of " + classOfT.getName() + " without parameter.",e);
        }
        constructor.setAccessible(true);
        T obj;
        try {
            obj = constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new CookieReflectException("Can't construct a instance of " + classOfT.getName() + " with constructor.",e);
        }
        Map<String,String> map = fromCookie(cookie);
        Iterator<String> iterator = map.keySet().iterator();
        Field[] allFields = classOfT.getFields();
        Map<String,Field> fieldMap = new HashMap<>();
        for(Field field : allFields){
            if(field.isAnnotationPresent(NoSerializeOrDeserialize.class)){
                continue;
            }
            if(field.isAnnotationPresent(SerializedName.class)){
                SerializedName serializedName = field.getAnnotation(SerializedName.class);
                fieldMap.put(serializedName.value(),field);
            }else{
                fieldMap.put(field.getName(),field);
            }
        }
        Map<String, Method> methodMap = new HashMap<>();
        if(!noMethod) {
            Method[] allMethods = classOfT.getDeclaredMethods();
            for (Method method : allMethods) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (method.isAnnotationPresent(SerializeMethod.class)
                        && parameterTypes.length == 1
                        && parameterTypes[0].equals(String.class)
                        && isDynamic(method)) {
                    SerializeMethod serializedName = method.getAnnotation(SerializeMethod.class);
                    methodMap.put(serializedName.value(), method);
                }
            }
        }
        while(iterator.hasNext()){
            String key = iterator.next();
            String value = map.get(key);
            Field field = fieldMap.get(key);
            if((!noMethod) && methodMap.containsKey(key)){
                try {
                    methodMap.get(key).invoke(obj,value);
                    continue;
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new CookieReflectException("Can't invoke init method " + methodMap.get(key).getName() + "() .",e);
                }
            }
            if(field != null){
                field.setAccessible(true);
                Class<?> fieldType = field.getType();
                if(fieldType.equals(String.class)){
                    try {
                        field.set(obj,value);
                    } catch (IllegalAccessException e) {
                        throw new CookieReflectException(
                                "Illegal access to field " + field.getName()
                                        + " of Class " + classOfT.getName()
                                        + " while parsing.",e);
                    }
                }else {
                    String fieldTypeName = fieldType.getName();
                    if(fieldType.equals(Integer.class) || fieldTypeName.equals("int")){
                        try {
                            field.set(obj,Integer.parseInt(value));
                        } catch (IllegalAccessException e) {
                            throw new CookieReflectException(
                                    "Illegal access to field " + field.getName()
                                            + " of Class " + classOfT.getName()
                                            + " while parsing.",e);
                        } catch (NumberFormatException e){
                            throw new CookieSyntaxException(
                                    "Expected integer but can't parse integer for value \"" + value
                                            + "\" while parsing key \"" + key + "\".");
                        }
                    }
                    else if(fieldType.equals(Float.class) || fieldTypeName.equals("float")){
                        try {
                            field.set(obj,Float.parseFloat(value));
                        } catch (IllegalAccessException e) {
                            throw new CookieReflectException(
                                    "Illegal access to field " + field.getName()
                                            + " of Class " + classOfT.getName()
                                            + " while parsing.",e);
                        } catch (NumberFormatException e){
                            throw new CookieSyntaxException(
                                    "Expected float but can't parse float for value \"" + value
                                            + "\" while parsing key \"" + key + "\".");
                        }
                    }else if(fieldType.equals(Double.class) || fieldTypeName.equals("double")){
                        try {
                            field.set(obj,Double.parseDouble(value));
                        } catch (IllegalAccessException e) {
                            throw new CookieReflectException(
                                    "Illegal access to field " + field.getName()
                                            + " of Class " + classOfT.getName()
                                            + " while parsing.",e);
                        } catch (NumberFormatException e){
                            throw new CookieSyntaxException(
                                    "Expected double but can't parse double for value \"" + value
                                            + "\" while parsing key \"" + key + "\".");
                        }
                    }
                }
            }
        }
        return obj;
    }

    /*
     * Parse a cookie string.
     * @param cookie The cookie string. Empty string will lead to an exception.
     * @return A {@Link HashMap}. Note that the key in this map is not parsed, while the cookie value will be parsed.
     * @exception When {@code cookie} is null, empty or contains invalid syntax.
     */
    public Map<String,String> fromCookie(@NotNull String cookie) throws CookieSyntaxException{
        if(cookie.trim().equals("")){
            throw new CookieSyntaxException("No input to process.");
        }
        Map<String,String> returnValue = new HashMap<>();
        String[] pairs = cookie.split(SEPARATOR);
        for(int i = 0;i < pairs.length;i ++){
            String pair = pairs[i].trim();
            if(pair.equals("")){
                continue;
            }
            String[] p = pair.split(OPERATOR);
            if(p.length != 2){
                throw new CookieSyntaxException(
                        "Too " + (p.length > 2 ? "many" : "less")
                                + " '" + OPERATOR + "' in key-value pair " + i +".");
            }
            returnValue.put(p[0],encoder.unescape(p[1]));
        }
        return returnValue;
    }

    /*
     * Transform a key map into a cookie string.
     * @param cookieMap A key-value map of the cookie. Empty map or null will turn into an empty string.
     * @return Processed cookie. The key won't be encoded but the value will be encoded to follow cookie rules.
     */
    public String toCookie(Map<String,String> cookieMap){
        if(cookieMap == null){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<String,String>> iterator = cookieMap.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String,String> entry = iterator.next();
            sb.append(entry.getKey());
            sb.append('=');
            sb.append(encoder.escape(entry.getValue()));
            if(iterator.hasNext()){
                sb.append("; ");
            }
        }
        return sb.toString();
    }

    /*
     * Make a cookie with an object.
     * If a certain field requires a name different from its field name, use {@link SerializedName}.
     * if a certain field shouldn't be used, use {@link NoSerializeOrDeserialize}.
     * If a certain field requires special serialize way, use {@link SerializeMethod} on a method.
     *
     * If deserializing is completed through a method, then:
     * 1. The method should require no parameter.
     * 2. The method should not be static.
     * 3. Use {@code @DeserializeMethod("serializeName")} to specify the key name.
     * 4. The return value of the method will be the cookie content. Under default settings, no character processing is necessary.
     * 5. A {@code @NoSerializeOrDeserialize} is strongly recommended on the real field.(or the field will occur twice).
     *
     * @param object The object to deserialize. Can be {@code null}, and will get empty string in this case.
     * @return The cookie.
     * @exception CookieReflectException When an exception is thrown during reflect operation.
     */
    public String toCookie(Object object) throws CookieReflectException{
        if(object == null){
            return "";
        }
        Map<String,String> map = new HashMap<>();
        Field[] allFields = object.getClass().getDeclaredFields();
        for(Field field : allFields){
            if(field.isAnnotationPresent(NoSerializeOrDeserialize.class)){
                continue;
            }
            field.setAccessible(true);
            String key;
            if(field.isAnnotationPresent(SerializedName.class)){
                key = field.getAnnotation(SerializedName.class).value();
            }else{
                key = field.getName();
            }
            String value;
            try {
                Object o = field.get(object);
                if(o == null){
                    continue;
                }
                value = o.toString();
            } catch (IllegalAccessException e) {
                throw new CookieReflectException(
                        "Can't access field \"" + field.getName() + "\" while deserializing.",e);
            }
            map.put(key,value);
        }
        if(!object.getClass().isAnnotationPresent(NoMethod.class)){
            Method[] allMethods = object.getClass().getDeclaredMethods();
            for(Method method : allMethods){
                Class<?>[] parameterTypes = method.getParameterTypes();
                if(method.isAnnotationPresent(DeserializeMethod.class)
                        && parameterTypes.length == 0
                        && isDynamic(method)
                        && method.getReturnType().equals(String.class)){
                    method.setAccessible(true);
                    String key = method.getAnnotation(DeserializeMethod.class).value();
                    String value;
                    try {
                        value = (String) method.invoke(object);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new CookieReflectException("Can't invoke deserialize method.",e);
                    }
                    if(value == null){
                        continue;
                    }
                    map.put(key,value);
                }
            }
        }
        return toCookie(map);
    }

    private static boolean isDynamic(Method method){
        return !Modifier.isStatic(method.getModifiers());
    }

    /*
     * Set the {@link TextEncoder} of current instance.
     * @see {@link TextEncoder}
     * @return Current instance (for link up convenience).
     */
    public CookieParser setEncoder(@NotNull TextEncoder encoder) {
        this.encoder = encoder;
        return this;
    }

    /*
     * Set the current instance not to process character escaping.
     * @return Current instance (for link up convenience).
     */
    public CookieParser dontProcessEncoding(){
        this.encoder = new NoProcess();
        return this;
    }

    /*
     * The default {@link TextEncoder} the parser use.
     * Escape policy: Will escape following character: (space) [ ] ( ) = , \ " ? @ : ;
     */
    public static class DefaultTextEncoder implements TextEncoder{

        @Override
        public String escape(@NotNull String original) {
            StringBuilder sb = new StringBuilder();
            for(char c : original.toCharArray()){
                if(needToEncode(c)){
                    sb.append("%");
                    if(c > 127) {
                        sb.append("u");
                    }
                    sb.append(Integer.toHexString(c));
                }else{
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        private static boolean needToEncode(char c){
            if(c > 127){
                return true;
            }
            return " []()=,\"/?@:;".indexOf(c) != -1;
        }

        @Override
        public String unescape(@NotNull String encoded) {
            return parseAscii(parseUnicode(encoded));
        }

        private static String parseUnicode(String encoded){
            Pattern pattern = Pattern.compile("%u([0-9a-fA-F]{4})");
            return parsePattern(encoded, pattern);
        }

        private static String parseAscii(String encoded){
            Pattern pattern = Pattern.compile("%([0-9a-fA-F]{2})");
            return parsePattern(encoded, pattern);
        }

        private static String parsePattern(String encoded, Pattern pattern) {
            Matcher matcher = pattern.matcher(encoded);
            StringBuilder sb = new StringBuilder(encoded.length());
            while(matcher.find()){
                String data = matcher.group(1);
                char ch = (char) Integer.parseInt(data,16);
                String replace = String.valueOf(ch);
                matcher.appendReplacement(sb,replace);
            }
            matcher.appendTail(sb);
            return sb.toString();
        }
    }

    private static class NoProcess implements TextEncoder{

        @Override
        public String escape(@NotNull String original) {
            return original;
        }

        @Override
        public String unescape(@NotNull String encoded) {
            return encoded;
        }
    }
}