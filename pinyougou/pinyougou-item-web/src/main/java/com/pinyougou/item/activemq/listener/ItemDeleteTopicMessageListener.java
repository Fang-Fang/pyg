package com.pinyougou.item.activemq.listener;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.listener.adapter.AbstractAdaptableMessageListener;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import java.io.File;

/**
 * 如果是删除的话，则需要将商品spu id集合发送到mq，详情系统接收并根据每个spu id到指定路径删除html页面。
 */
public class ItemDeleteTopicMessageListener extends AbstractAdaptableMessageListener {

    @Value("${ITEM_HTML_PATH}")
    private String ITEM_HTML_PATH;

    @Override
    public void onMessage(Message message, Session session) throws JMSException {
        //1、接收消息
        ObjectMessage objectMessage = (ObjectMessage) message;
        Long[] ids = (Long[]) objectMessage.getObject();
        //2、生成具体的html
        if (ids != null && ids.length > 0) {
            for (Long goodsId : ids) {
                File file = new File(ITEM_HTML_PATH + goodsId + ".html");
                if (file.exists()) {
                    file.delete();
                }
            }
        }
    }
}
