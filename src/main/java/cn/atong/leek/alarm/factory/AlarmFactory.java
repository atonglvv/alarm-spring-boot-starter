package cn.atong.leek.alarm.factory;

import cn.atong.leek.alarm.service.AlarmStartegy;
import cn.atong.leek.alarm.service.CompanyWeChatAlarmService;
import cn.atong.leek.alarm.service.DingDingAlarmService;

import java.util.HashMap;
import java.util.Map;

/**
 * @program: alarm-spring-boot-starter
 * @description:
 * @author: atong
 * @create: 2022-08-04 16:58
 */
public class AlarmFactory {

    private static final Map<String, AlarmStartegy> STRATEGIES = new HashMap<>();

    static {
        STRATEGIES.put("dingding", new DingDingAlarmService());
        STRATEGIES.put("companyWeChat", new CompanyWeChatAlarmService());
    }

    public static AlarmStartegy getStrategy(String mode) {
        return STRATEGIES.get(mode);
    }
}
