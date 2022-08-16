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

    default Set<String> exclusionPackage() {
        return new HashSet<>();
    }

    default Set<String> exclusionString() {
        return new HashSet<>();
    }

    default Set<String> exclusionThrowable() {
        return new HashSet<>();
    }
}
