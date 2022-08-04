package cn.atong.leek.alarm.service;

/**
 * @program: alarm-spring-boot-starter
 * @description: 报警策略
 * @author: atong
 * @create: 2022-08-04 16:42
 */
public interface AlarmStartegy {
    /**
     * 发送错误日志
     * @param message 消息
     * @param alarmAddress 报警webhook地址
     */
    void sendAlarm(String message, String alarmAddress);
}
