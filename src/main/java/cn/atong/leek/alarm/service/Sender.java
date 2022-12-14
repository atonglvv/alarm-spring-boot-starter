package cn.atong.leek.alarm.service;

import cn.atong.leek.alarm.context.AlarmContext;
import cn.atong.leek.alarm.dto.AlarmDto;
import cn.atong.leek.alarm.factory.AlarmFactory;
import cn.atong.leek.alarm.filter.AlarmFilterInterface;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @program: alarm-spring-boot-starter
 * @description:
 * @author: atong
 * @create: 2022-08-02 14:56
 */
@Slf4j
public class Sender {

    @Value("${alarm.address:null}")
    private String alarmAddress;
    /** 报警介质 钉钉 企信 */
    @Value("${alarm.mode:dingding}")
    private String alarmMode;
    @Value("${spring.profiles.active}")
    private String active;
    @Value("${spring.application.name}")
    private String applicationName;
    @Autowired
    private ApplicationContext applicationContext;
    /** todo ConcurrentHashSet */
    private final Set<String> exclusionPackageSet = new HashSet<>();
    private final Set<String> exclusionStringSet = new HashSet<>();

    private final ThreadPoolExecutor singleThreadExecutor = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());

    public boolean addExclusionStringSet(String exclusionString) {
        return this.exclusionStringSet.add(exclusionString);
    }

    public boolean removeExclusionStringSet(String exclusionString) {
        return this.exclusionStringSet.remove(exclusionString);
    }

    @PostConstruct
    public void init() {
        try {
            AlarmFilterInterface bean = applicationContext.getBean(AlarmFilterInterface.class);
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
            // noinspection InfiniteLoopStatement
            while(true) {
                try {
                    AlarmDto dto = AlarmContext.logBlockingQueue.take();
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
                        String errorLog = active + " 环境," + applicationName + "项目, TraceId: " + dto.getMdc() +
                                " ip: " + dto.getIp() + "\n" +
                                " Exception: " + dto.getMessage() + "\n" +
                                " argsList: " + argsList;
                        log.info("ErrorLogSender send error log [{}]", errorLog);
                        AlarmStartegy strategy = AlarmFactory.getStrategy(alarmMode);
                        strategy.sendAlarm(errorLog, alarmAddress);
                    }
                } catch (Exception e) {
                    log.info(e.getMessage());
                }
            }
        });
    }
}
