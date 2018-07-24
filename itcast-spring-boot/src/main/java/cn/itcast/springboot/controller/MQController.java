package cn.itcast.springboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/mq")
@RestController
public class MQController {

    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;

    @GetMapping("/send")
    public String sendMapMsg(){
        Map<String, Object> map = new HashMap<>();
        map.put("id", 123);
        map.put("name", "黑马");
        //参数1:模式（队列）,参数2：发送的信息（可以任何类型，接收的时候也是有该类型即可）
        jmsMessagingTemplate.convertAndSend("spring.boot.map.queue", map);
        return "发送消息完成。";
    }

    @GetMapping("/sendSms")
    public String sendSmsMsg(){
        Map<String, Object> map = new HashMap<>();
        map.put("mobile", "13144066269");
        map.put("signName", "黑马");
        map.put("templateCode", "SMS_125018593");
        map.put("templateParam", "{\"code\":654321}");
        //参数1:模式（队列）,参数2：发送的信息（可以任何类型，接收的时候也是有该类型即可）
        jmsMessagingTemplate.convertAndSend("itcast_sms_queue", map);
        return "发送sms消息完成。";
    }
}
