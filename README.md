# CookieParse
 一个Cookie解析器（用法类似Gson）。

## 说明

这个东西完全出于临时起意而写的，后来稍微加了点注释就发上来了，不喜勿喷。

重做了文件结构，现在是正常的Idea工程了。

## 用法

和Gson很像。首先，构造一个类来说明cookie的字段信息：

```Java
public class MyCookie{
	private String key1;
	private int key2;
	private double key3;
}
```

注意只支持`String`、`int`、`double`、`float`四种数据类型的解析。对于Cookie生成，会使用`toString()`，因而支持所有类型。

然后在获得Cookie后，使用`new CookieParser().fromCookie("获得的Cookie",MyCookie.class)`。

对于不应被涉及的字段，使用`@NoSerializeOrDeserialize`。

### 改变字段名字

使用注解`@SerializedName`。注意该注解和Gson注解同名，用法也几乎一样，因此在同一文件中使用会比较麻烦。

### 使用方法进行解析

如果对解析结果有特殊要求，可以使用一个方法进行解析和生成。

```java
public class MethodCookie{
	@NoSerializeOrDeserialize//这里如果不使用此注解，则生成Cookie时内容可能重复出现。
	private URL link;
	
	@SerializeMethod("link")
	public void setLink(String cookieValue){
		link = new URL(cookieValue);//仅作演示，忽略异常
	}
	
	@DeserializeMethod("link")
	public String getLink(){
		return link.toString();
	}
}
```

需注意上述方法的参数不可改变，也不可改为静态，但可以是私有方法。

如果对上述操作的安全性有顾虑，或者单纯只是希望解析时跳过方法检索以加快速度，可以在类上加注解`@NoMethod`以让解析器忽略所有方法。

### 特殊字符

Cookie中不能使用空格、`[]`、`()`、`@`等字符。默认情况下，`CookieParser`会自动将这些字符转义。对于接受的Cookie，则会还原转义字符。

若要改变默认的特殊字符处理方式，使用一个`TextEncoder`接口的实现：

```Java
new CookieParser()
	.setEncoder(new TextEncoder(){
		@Override
		public String escape(String original){
			//TODO:Process the original text
		}
		
		@Override
		public String unescape(String cookieValue){
			//TODO:Process the cookie value
		}
	}).fromCookie("Cookie",Cookie.class);
```

在`CookieParser`下有一个默认的子类作为默认的处理。

如果不处理特殊字符，使用`new CookieParser().dontProcessEncoding()`。

## License

[Anti-996](https://github.com/996icu/996.ICU/blob/master/LICENSE)