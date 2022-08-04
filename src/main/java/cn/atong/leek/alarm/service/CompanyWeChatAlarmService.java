package cn.atong.leek.alarm.service;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

/**
 * @program: alarm-spring-boot-starter
 * @description: 企业微信机器人 WebHook
 * @author: atong
 * @create: 2022-08-02 16:08
 */
@Slf4j
public class CompanyWeChatAlarmService {

    private final RestTemplate restTemplate;

    public CompanyWeChatAlarmService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
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
            String result = restTemplate.postForObject(alarmAddress, alarmMessage, String.class);
            log.info("alarm call wechat roobat response info [{}]", result);
            JSONObject resultObject = JSONObject.parseObject(result);
            Integer errcode = resultObject.getInteger("errcode");
            if (errcode != 0 && errcode != 45009) {
                log.warn("alarm call wechat roobat error [{}]", result);
            }
        } catch (Exception e) {
            log.warn("web alarm job error:", e);
        }
    }
}
