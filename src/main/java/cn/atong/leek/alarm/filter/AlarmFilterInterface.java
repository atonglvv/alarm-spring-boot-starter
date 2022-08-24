package cn.atong.leek.alarm.filter;

import java.util.HashSet;
import java.util.Set;

/**
 * @program: alarm-spring-boot-starter
 * @description:
 * @author: atong
 * @create: 2022-08-02 15:06
 */
public interface AlarmFilterInterface {

    /**
     * 排除包内的错误日志获取异常报警
     * @return HashSet
     */
    default Set<String> exclusionPackage() {
        return new HashSet<>();
    }

    /**
     * 排除包含某String的错误日志获取异常报警
     * @return HashSet
     */
    default Set<String> exclusionString() {
        return new HashSet<>();
    }

    /**
     * 排除Throwable的错误日志获取异常报警
     * @return HashSet
     */
    default Set<String> exclusionThrowable() {
        return new HashSet<>();
    }
}
