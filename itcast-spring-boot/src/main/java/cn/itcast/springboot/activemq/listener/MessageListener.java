package cn.itcast.springboot.activemq.listener;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MessageListener {

    /**
     * 表示监听队列名称为spring.boot.map.queue的消息
     * @param msgMap 接收到的消息
     */
    @JmsListener(destination = "spring.boot.map.queue")
    public void receiveMsg(Map<String, Object> msgMap){
        System.out.println("收到的消息为：" + msgMap);
    }
}
