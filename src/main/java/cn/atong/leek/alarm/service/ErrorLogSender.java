package cn.atong.leek.alarm.service;

import cn.atong.leek.alarm.context.LogContext;
import cn.atong.leek.alarm.dto.ErrorLogDto;
import cn.atong.leek.alarm.filter.ErrorLogFilterInterface;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @program: alarm-spring-boot-starter
 * @description:
 * @author: atong
 * @create: 2022-08-02 14:56
 */
@Slf4j
public class ErrorLogSender {
    @Value("${alarmAddress:null}")
    private String alarmAddress;
    @Value("${spring.profiles.active}")
    private String active;
    @Value("${spring.application.name}")
    private String applicationName;
    @Autowired
    private ApplicationContext applicationContext;
    // todo ConcurrentHashSet
    private Set<String> exclusionPackageSet = new HashSet<>();
    private Set<String> exclusionStringSet = new HashSet();
    private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    @Autowired
    private CompanyWeChatAlarmService companyWeChatAlarmService;

    public boolean addExclusionStringSet(String exclusionString) {
        return this.exclusionStringSet.add(exclusionString);
    }

    public boolean removeExclusionStringSet(String exclusionString) {
        return this.exclusionStringSet.remove(exclusionString);
    }

    @PostConstruct
    public void init() {
        try {
            ErrorLogFilterInterface bean = applicationContext.getBean(ErrorLogFilterInterface.class);
            if (bean.exclusionPackage() != null) {
                exclusionPackageSet.addAll(bean.exclusionPackage());
            }
            if (bean.exclusionString() != null) {
                exclusionStringSet.addAll(bean.exclusionString());
            }
            log.info("init exclusion param: {} {}", JSONObject.toJSON(exclusionPackageSet), JSONObject.toJSON(exclusionStringSet));
        } catch (BeansException e) {
            log.info("init exclusion BeansException: {}", e.getMessage());
        } catch (Exception e) {
            log.info("init exclusion error: {}", e.getMessage());
        }

        singleThreadExecutor.execute(() -> {
            while(true) {
                try {
                    ErrorLogDto dto = LogContext.logBlockingQueue.take();
                    if (!"null".equals(alarmAddress)) {
                        String packageString = dto.getPackageString();
                        for (String exclusionPackage : exclusionPackageSet) {
                            if (packageString.contains(exclusionPackage)) {
                                throw new RuntimeException("过滤报警消息-package");
                            }
                        }
                        for (String exclusionString : exclusionStringSet) {
                            if (dto.getMessage().contains(exclusionString)) {
                                throw new RuntimeException("过滤报警消息-message");
                            }
                        }
                        String argsList = JSONObject.toJSONString(dto.getArgumentArray());
                        for (String exclusionString : exclusionStringSet) {
                            if (argsList.contains(exclusionString)) {
                                throw new RuntimeException("过滤报警消息-argsList");
                            }
                        }
                        String errorLog = active + " 环境" + applicationName + "项目 TraceId: " + dto.getMdc() + " ip: " + dto.getIp() + " Exception: " + dto.getMessage() + " argsList: " + argsList;
                        log.info("ErrorLogSender send error log [{}]", errorLog);
                        companyWeChatAlarmService.sendAlarm(errorLog, alarmAddress);
                    }
                } catch (Exception e) {
                    log.info(e.getMessage());
                }
            }
        });
    }
}
