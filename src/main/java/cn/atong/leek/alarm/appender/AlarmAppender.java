package cn.atong.leek.alarm.appender;

import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Map;

import cn.atong.leek.alarm.context.AlarmContext;
import cn.atong.leek.alarm.dto.AlarmDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.slf4j.MDC;

/**
 * @program: alarm-spring-boot-starter
 * @description: log4j appender
 * @author: atong
 * @create: 2022-08-08 16:45
 */
@Slf4j
@Plugin(
        name = "AlarmAppender",
        category = Node.CATEGORY,
        elementType = "appender",
        printObject = true
)
public class AlarmAppender extends AbstractAppender {

    private static String ip = "";

    public AlarmAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions);
    }

    @Override
    public void append(LogEvent event) {
        try {
            if (Level.ERROR.equals(event.getLevel())) {
                ThrowableProxy throwableProxy = event.getThrownProxy();
                StringBuilder sb = new StringBuilder();
                if (throwableProxy != null) {
                    Throwable throwable = throwableProxy.getThrowable();
                    String throwableMsg = throwable.getMessage();
                    StackTraceElement[] stackTraceElementProxy = throwableProxy.getStackTrace();
                    sb.append(event.getMessage()).append("\n");
                    if (throwableMsg != null && throwableMsg.length() > 0) {
                        sb.append(throwable.getClass().getName()).append(": ");
                        sb.append(throwableMsg).append("\n");
                    }
                    // 行数
                    int lineNum = 0;
                    for (StackTraceElement stackTraceElement : stackTraceElementProxy) {
                        if (lineNum >= 20) {
                            break;
                        }
                        sb.append("\t at ");
                        sb.append(stackTraceElement.toString()).append("\n");
                        ++lineNum;
                    }
                } else {
                    sb.append(event.getMessage());
                }
                String errorMessage = sb.toString();
                if (errorMessage.length() == 0) {
                    return;
                }
                AlarmDto dto = new AlarmDto();
                dto.setMessage(errorMessage);
                dto.setMdc(MDC.get("traceId"));
                if (ip == null || ip.length() == 0) {
                    InetAddress ip4 = Inet4Address.getLocalHost();
                    ip = ip4.getHostAddress();
                }
                dto.setIp(ip);
                ReadOnlyStringMap contextData = event.getContextData();
                Map<String, String> map = contextData.toMap();
                Collection<String> values = map.values();
                Object[] argumentArray = values.toArray();
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
    }

    @PluginFactory
    public static AlarmAppender createAppender(@PluginAttribute("name") String name, @PluginElement("Filter") Filter filter, @PluginElement("Layout") Layout<? extends Serializable> layout, @PluginAttribute("ignoreExceptions") boolean ignoreExceptions) {
        return new AlarmAppender(name, filter, layout, ignoreExceptions);
    }
}

