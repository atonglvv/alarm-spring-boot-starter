package cn.atong.leek.alarm.config;

import cn.atong.leek.alarm.service.CompanyWeChatAlarmService;
import cn.atong.leek.alarm.service.DingDingAlarmService;
import cn.atong.leek.alarm.service.ErrorLogSender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

/**
 * @program: alarm-spring-boot-starter
 * @description:
 * @author: atong
 * @create: 2022-08-02 14:34
 */
@Configuration
public class AlarmConfigure {

    private static final Integer CONNECTIONTIMEOUT = 10000;
    private static final Integer READTIMEOUT = 10000;

    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECTIONTIMEOUT);
        factory.setReadTimeout(READTIMEOUT);
        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return restTemplate;
    }

    @Bean
    public ErrorLogSender errorLogSender() {
        return new ErrorLogSender();
    }

    @Bean
    public CompanyWeChatAlarmService companyWeChatAlarmService() {
        return new CompanyWeChatAlarmService(restTemplate());
    }

    @Bean
    public DingDingAlarmService dingDingAlarmService() {
        return new DingDingAlarmService(restTemplate());
    }
}
