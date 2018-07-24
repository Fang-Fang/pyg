package cn.itcast.sms.activemq.listener;

import cn.itcast.sms.util.SmsUtil;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SMSMessageListener {

    @Autowired
    private SmsUtil smsUtil;

    /**
     * 监听队列名称为itcast_sms的消息
     *
     * @param map 消息
     */
    @JmsListener(destination = "itcast_sms_queue")
    public void receiveSmsMsg(Map<String, String> map) {
        try {
            SendSmsResponse response = smsUtil.sendSms(map.get("mobile"), map.get("signName"),
                    map.get("templateCode"), map.get("templateParam"));
            System.out.println("短信接口返回的数据----------------");
            System.out.println("Code=" + response.getCode());
            System.out.println("Message=" + response.getMessage());
            System.out.println("RequestId=" + response.getRequestId());
            System.out.println("BizId=" + response.getBizId());
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }
}
