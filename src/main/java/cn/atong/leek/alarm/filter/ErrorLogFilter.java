package cn.atong.leek.alarm.filter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import cn.atong.leek.alarm.context.LogContext;
import cn.atong.leek.alarm.dto.ErrorLogDto;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.net.Inet4Address;
import java.net.InetAddress;

/**
 * @program: alarm-spring-boot-starter
 * @description:
 * @author: atong
 * @create: 2022-08-02 11:19
 */
@Slf4j
public class ErrorLogFilter extends Filter<ILoggingEvent> {

    private static String ip = "";

    @Override
    public FilterReply decide(ILoggingEvent event) {
        try {
            if (Level.ERROR.equals(event.getLevel())) {
                IThrowableProxy iThrowableProxy = event.getThrowableProxy();
                StringBuilder sb = new StringBuilder();
                if (iThrowableProxy instanceof ThrowableProxy) {
                    ThrowableProxy throwableProxy = (ThrowableProxy)iThrowableProxy;
                    Throwable throwable = throwableProxy.getThrowable();
                    String throwableMsg = throwable.getMessage();
                    StackTraceElementProxy[] stackTraceElementProxy = iThrowableProxy.getStackTraceElementProxyArray();
                    sb.append(event.getMessage()).append("\n");
                    if (throwableMsg != null && throwableMsg.length() > 0) {
                        sb.append(throwable.getClass().getName()).append(": ");
                        sb.append(throwableMsg).append("\n");
                    }
                    // 行数
                    int lineNum = 0;
                    int stackTraceElementProxyLength = stackTraceElementProxy.length;

                    for (StackTraceElementProxy proxy : stackTraceElementProxy) {
                        if (lineNum >= 20) {
                            break;
                        }
                        sb.append(proxy.getSTEAsString()).append("\n");
                        ++lineNum;
                    }
                } else {
                    sb.append(event.getMessage());
                }
                String errorMessage = sb.toString();
                if (errorMessage.length() == 0) {
                    return FilterReply.ACCEPT;
                }
                ErrorLogDto dto = new ErrorLogDto();
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
                LogContext.logBlockingQueue.offer(dto);
            }
        } catch (Exception e) {
            try {
                ErrorLogDto dto = new ErrorLogDto();
                dto.setMessage("ErrorLogFilter error:" + e.getMessage());
                LogContext.logBlockingQueue.offer(dto);
            } catch (Exception exception) {
                log.info("ErrorLogFilter exception");
            }
        }
        return FilterReply.ACCEPT;
    }
}
