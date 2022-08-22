package cn.atong.leek.alarm.dto;

import lombok.Data;

/**
 * @program: alarm-spring-boot-starter
 * @description:
 * @author: atong
 * @create: 2022-08-02 11:33
 */
@Data
public class AlarmDto {
    private String message;
    private String mdc;
    private String ip;
    private Object[] argumentArray;
    private String packageString;
    private String throwableClassName;
}
