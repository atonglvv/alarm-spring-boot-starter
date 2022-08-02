package cn.atong.leek.alarm.config;

import cn.atong.leek.alarm.service.CompanyWeChatAlarmService;
import cn.atong.leek.alarm.service.ErrorLogSender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @program: alarm-spring-boot-starter
 * @description:
 * @author: atong
 * @create: 2022-08-02 14:34
 */
@Configuration
public class ErrorLogConfigure {

    @Bean
    public CompanyWeChatAlarmService companyWeChatAlarmService() {
        return new CompanyWeChatAlarmService();
    }

    @Bean
    public ErrorLogSender errorLogSender() {
        return new ErrorLogSender();
    }
}
