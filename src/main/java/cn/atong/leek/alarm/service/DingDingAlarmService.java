package cn.atong.leek.alarm.service;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

/**
 * @program: alarm-spring-boot-starter
 * @description:
 * @author: atong
 * @create: 2022-08-03 11:09
 */
@Slf4j
public class DingDingAlarmService {
    private static final Integer CONNECTIONTIMEOUT = 10000;
    private static final Integer READTIMEOUT = 10000;
    private final RestTemplate restTemplate;

    public DingDingAlarmService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECTIONTIMEOUT);
        factory.setReadTimeout(READTIMEOUT);
        restTemplate = new RestTemplate(factory);
        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
    }

    public void sendAlarm(String message, String alarmAddress) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("msgtype", "text");
            JSONObject contentJson = new JSONObject();
            if (message.length() > 4500) {
                message = message.substring(0, 4500);
                message = message + "@@@@@@@太长了，截断了";
            }
            contentJson.put("content", message);
            jsonObject.put("text", contentJson);
            String alarmMessage = jsonObject.toJSONString();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> formEntity = new HttpEntity<String>(alarmMessage, headers);

            String result = restTemplate.postForObject(alarmAddress, formEntity, String.class);
            log.info("alarm call dingding roobat response info [{}]", result);
            JSONObject resultObject = JSONObject.parseObject(result);
            Integer errcode = resultObject.getInteger("errcode");
            if (errcode != 0 && errcode != 45009) {
                log.warn("alarm call dingding roobat error [{}]", result);
            }
        } catch (Exception e) {
            log.warn("web alarm job error:", e);
        }
    }
}
