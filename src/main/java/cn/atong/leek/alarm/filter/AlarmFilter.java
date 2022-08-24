package cn.atong.leek.alarm.filter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import cn.atong.leek.alarm.context.AlarmContext;
import cn.atong.leek.alarm.dto.AlarmDto;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

/**
 * @program: alarm-spring-boot-starter
 * @description:
 * @author: atong
 * @create: 2022-08-02 11:19
 */
@Slf4j
public class AlarmFilter extends Filter<ILoggingEvent> {

    private static String ip = "";
    private static final Set<String> EXCLUSION_THROWABLE_SET = new HashSet<>();

    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        try{
            AlarmFilterInterface bean = applicationContext.getBean(AlarmFilterInterface.class);
            if (bean.exclusionThrowable() != null) {
                EXCLUSION_THROWABLE_SET.addAll(bean.exclusionThrowable());
            }
        } catch (BeansException e) {
            log.info("init exclusion BeansException: {}", e.getMessage());
        } catch (Exception e) {
            log.info("init exclusion error: {}", e.getMessage());
        }
    }

    @Override
    public FilterReply decide(ILoggingEvent event) {
        try {
            AlarmDto dto = new AlarmDto();
            // 只拦截 ERROR 级别的 log
            if (Level.ERROR.equals(event.getLevel())) {
                IThrowableProxy iThrowableProxy = event.getThrowableProxy();
                StringBuilder sb = new StringBuilder();
                if (iThrowableProxy instanceof ThrowableProxy) {
                    // Throwable 不止 error message, 还需要打印 throwableMsg...
                    ThrowableProxy throwableProxy = (ThrowableProxy)iThrowableProxy;
                    Throwable throwable = throwableProxy.getThrowable();
                    String throwableClassName = throwable.getClass().getName();
                    for (String exclusionThrowable : EXCLUSION_THROWABLE_SET) {
                        if (throwableClassName.contains(exclusionThrowable)) {
                            return FilterReply.ACCEPT;
                        }
                    }
                    dto.setThrowableClassName(throwableClassName);
                    String throwableMsg = throwable.getMessage();
                    StackTraceElementProxy[] stackTraceElementProxy = iThrowableProxy.getStackTraceElementProxyArray();
                    sb.append(event.getMessage()).append("\n");
                    if (throwableMsg != null && throwableMsg.length() > 0) {
                        sb.append(throwableClassName).append(": ");
                        sb.append(throwableMsg).append("\n");
                    }
                    // 行数
                    int lineNum = 0;
                    for (StackTraceElementProxy proxy : stackTraceElementProxy) {
                        if (lineNum >= 20) {
                            break;
                        }
                        sb.append(proxy.getSTEAsString()).append("\n");
                        ++lineNum;
                    }
                } else {
                    // 项目中打印的 error log
                    sb.append(event.getMessage());
                }
                String errorMessage = sb.toString();
                if (errorMessage.length() == 0) {
                    return FilterReply.ACCEPT;
                }
                dto.setMessage(errorMessage);
                dto.setMdc(MDC.get("traceId"));
                if (ip == null || ip.length() == 0) {
                    InetAddress ip4 = Inet4Address.getLocalHost();
                    ip = ip4.getHostAddress();
                }
                dto.setIp(ip);
                Object[] argumentArray = event.getArgumentArray();
                dto.setArgumentArray(argumentArray);
                String loggerName = event.getLoggerName();
                dto.setPackageString(loggerName);
                AlarmContext.logBlockingQueue.offer(dto);
            }
        } catch (Exception e) {
            try {
                AlarmDto dto = new AlarmDto();
                dto.setMessage("ErrorLogFilter error:" + e.getMessage());
                AlarmContext.logBlockingQueue.offer(dto);
            } catch (Exception exception) {
                log.info("ErrorLogFilter exception");
            }
        }
        return FilterReply.ACCEPT;
    }
}
