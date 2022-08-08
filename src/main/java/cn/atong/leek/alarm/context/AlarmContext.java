package cn.atong.leek.alarm.context;

import cn.atong.leek.alarm.dto.AlarmDto;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @program: alarm-spring-boot-starter
 * @description:
 * @author: atong
 * @create: 2022-08-02 11:35
 */
public class AlarmContext {
    public static BlockingQueue<AlarmDto> logBlockingQueue = new ArrayBlockingQueue<>(1000);
}
