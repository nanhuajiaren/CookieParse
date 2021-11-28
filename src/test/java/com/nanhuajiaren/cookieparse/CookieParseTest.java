package com.nanhuajiaren.cookieparse;

import com.nanhuajiaren.cookieparse.testdatastructure.TestDataStructure;
import org.junit.jupiter.api.Test;

public class CookieParseTest {
    @Test
    public void parseTest() throws Exception{
        CookieParser parser = new CookieParser();
        TestDataStructure testDataStructure = parser.fromCookie("a=hello;b=123;c=test;d=name_test",TestDataStructure.class);
        System.out.println(testDataStructure.a);
        System.out.println(testDataStructure.b);
        System.out.println(testDataStructure.c);
    }

    @Test
    public void toCookieTest() throws Exception{
        CookieParser parser = new CookieParser();
        TestDataStructure testDataStructure = new TestDataStructure();
        testDataStructure.a = "hi";
        testDataStructure.b = 456;
        testDataStructure.c = "你好;[]";
        System.out.println(parser.toCookie(testDataStructure));
    }
}
