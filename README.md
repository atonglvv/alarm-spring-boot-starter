# alarm-spring-boot-starter

[Quick start](https://gitcode.net/u011552171/alarm-spring-boot-starter#quick-start) | [FAQs](https://gitcode.net/u011552171/alarm-spring-boot-starter#faqs) | [Report an issue](https://gitcode.net/u011552171/alarm-spring-boot-starter/-/issues/new)

alarm-spring-boot-starter will help you to View error logs from compony-wechat or DingDing.  

JDK compatibility:  1.8  

## Quick-start
#### Maven
        <dependency>
            <groupId>cn.atong.leek</groupId>
            <artifactId>alarm-spring-boot-starter</artifactId>
            <version>1.0.1</version>
        </dependency>
#### application.yml
```yaml
alarm:
  address: [dingding or compony-wechat reboot webHook]
spring:
  application:
    name: [your app name]
  profiles:
    active: [for example dev]
```
#### logback
指定 logback appender 的 Filter，如下：
```xml
    <!--控制日志输出位置的Appender-->
    <appender name="console" class="ch.qos.logback.core.ConsoleAppender">
        <filter class="cn.atong.leek.alarm.filter.ErrorLogFilter"/>
        <!--日志消息格式的配置-->
        <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%t] %C.%M:%L - [traceId:%X{traceId}] [businessId:%X{businessId}] %m%n </pattern>
        </encoder>
    </appender>
```

## FAQs